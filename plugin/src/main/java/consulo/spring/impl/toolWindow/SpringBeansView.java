package consulo.spring.impl.toolWindow;

import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.component.messagebus.MessageBusConnection;
import consulo.dataContext.DataSink;
import consulo.dataContext.UiDataProvider;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.language.editor.PlatformDataKeys;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiModificationTrackerListener;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.event.ModuleRootEvent;
import consulo.module.content.layer.event.ModuleRootListener;
import consulo.navigation.Navigatable;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.project.event.DumbModeListener;
import consulo.project.ui.wm.ToolWindowManager;
import consulo.project.ui.wm.ToolWindowManagerListener;
import consulo.spring.impl.beans.SpringBeanDependencies;
import consulo.spring.impl.beans.SpringBeanInfo;
import consulo.spring.impl.beans.SpringBeanKind;
import consulo.spring.impl.beans.SpringBeansCollector;
import consulo.spring.impl.beans.SpringContextInfo;
import consulo.spring.impl.beans.SpringModuleBeans;
import consulo.spring.impl.beans.SpringProfileInfo;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.Space;
import consulo.ui.TextAttribute;
import consulo.ui.TextItemPresentation;
import consulo.ui.Tree;
import consulo.ui.TreeModel;
import consulo.ui.TreeNode;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.TreeExpander;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.ActionToolbar;
import consulo.ui.ex.action.ActionToolbarFactory;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.action.IdeActions;
import consulo.ui.ex.toolWindow.ToolWindow;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.ScrollableLayout;
import consulo.ui.layout.SplitLayoutPosition;
import consulo.ui.layout.TwoComponentSplitLayout;
import consulo.ui.util.FormBuilder;
import consulo.util.dataholder.Key;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * The Spring tool window: modules with their profiles and contexts, and the beans of each context grouped by kind,
 * with the selected node described below the tree.
 *
 * @author VISTALL
 * @since 2026-10-22
 */
public final class SpringBeansView implements Disposable, UiDataProvider {
    public static final Key<SpringBeansView> KEY = Key.create(SpringBeansView.class);

    private static final long REFRESH_DELAY_MILLISECONDS = 500;

    /**
     * A module row. Tree rows are matched by value after a rebuild, so they hold keys rather than snapshots.
     */
    private record ModuleNode(String moduleName) {
    }

    private record ProfilesNode(String moduleName) {
    }

    private record KindNode(String moduleName, String contextId, SpringBeanKind kind) {
    }

    private final Project myProject;
    private final Tree<Object> myTree;
    private final DockLayout myRoot;
    private final DockLayout myDetails;
    private final ActionToolbar myToolbar;

    private final AtomicBoolean myRefreshScheduled = new AtomicBoolean();

    private List<SpringModuleBeans> mySnapshot = List.of();
    private volatile boolean myDirty = true;
    private volatile boolean myDisposed;
    private int myDetailsRequest;

    @RequiredUIAccess
    public SpringBeansView(Project project) {
        myProject = project;

        myTree = Tree.create(new BeansTreeModel());
        myTree.setSpeedSearchConverter(node -> speedSearchText(node.getValue()));
        myTree.addSelectListener(event -> showDetails(event.getValue() == null ? null : event.getValue().getValue()));
        Disposer.register(this, myTree.destroyHook());

        DefaultActionGroup toolbarGroup = new DefaultActionGroup();
        toolbarGroup.add(new RefreshAction());
        toolbarGroup.addSeparator();
        ActionManager actionManager = ActionManager.getInstance();
        toolbarGroup.add(actionManager.getAction(IdeActions.ACTION_EXPAND_ALL));
        toolbarGroup.add(actionManager.getAction(IdeActions.ACTION_COLLAPSE_ALL));
        myToolbar = ActionToolbarFactory.getInstance().createActionToolbar("SpringBeansView", toolbarGroup, ActionToolbar.Style.HORIZONTAL);

        myDetails = DockLayout.create(Space.NONE);
        myDetails.paddingBuilder().horizontalSet(Space.MEDIUM).verticalSet(Space.SMALL).apply();
        showDetails(null);

        TwoComponentSplitLayout split = TwoComponentSplitLayout.create(SplitLayoutPosition.VERTICAL);
        split.setProportion(65);
        split.setFirstComponent(ScrollableLayout.create(myTree));
        split.setSecondComponent(ScrollableLayout.create(myDetails));

        myRoot = DockLayout.create(Space.NONE);
        myRoot.top(myToolbar.getUIComponent());
        myRoot.center(split);
        myRoot.putUserData(UiDataProvider.KEY, this);
        myToolbar.setTargetUIComponent(myRoot);

        subscribe();
        reload();
    }

    public Component getComponent() {
        return myRoot;
    }

    private void subscribe() {
        MessageBusConnection connection = myProject.getMessageBus().connect(this);
        connection.subscribe(PsiModificationTrackerListener.class, this::markDirty);
        connection.subscribe(ModuleRootListener.class, new ModuleRootListener() {
            @Override
            public void rootsChanged(ModuleRootEvent event) {
                markDirty();
            }
        });
        connection.subscribe(DumbModeListener.class, new DumbModeListener() {
            @Override
            public void exitDumbMode() {
                markDirty();
            }
        });
        connection.subscribe(ToolWindowManagerListener.class, new ToolWindowManagerListener() {
            @Override
            public void toolWindowShown(ToolWindow toolWindow) {
                if (SpringToolWindowFactory.ID.equals(toolWindow.getId())) {
                    scheduleRefresh();
                }
            }
        });
    }

    private void markDirty() {
        myDirty = true;
        scheduleRefresh();
    }

    private void scheduleRefresh() {
        if (myDisposed || !myDirty || !isToolWindowVisible() || !myRefreshScheduled.compareAndSet(false, true)) {
            return;
        }
        myProject.getUIAccess().getScheduler().schedule(() -> {
            myRefreshScheduled.set(false);
            if (!myDisposed && myDirty) {
                reload();
            }
        }, REFRESH_DELAY_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    private boolean isToolWindowVisible() {
        ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow(SpringToolWindowFactory.ID);
        return toolWindow != null && toolWindow.isVisible();
    }

    @RequiredUIAccess
    public void reload() {
        myDirty = false;
        ReadAction.nonBlocking(() -> SpringBeansCollector.collect(myProject))
            .inSmartMode(myProject)
            .expireWith(this)
            .coalesceBy(this)
            .finishOnUiThread(Application::getDefaultModalityState, this::applySnapshot)
            .submitDefault();
    }

    @RequiredUIAccess
    private void applySnapshot(List<SpringModuleBeans> snapshot) {
        List<List<Object>> expandedPaths = new ArrayList<>();
        for (List<TreeNode<Object>> path : myTree.getExpandedPaths()) {
            expandedPaths.add(valuesOf(path));
        }
        List<Object> selectedPath = valuesOf(myTree.getSelectedPath());
        boolean firstLoad = mySnapshot.isEmpty();

        mySnapshot = snapshot;

        UIAccess uiAccess = UIAccess.current();
        myTree.refreshAll().whenCompleteAsync((result, error) -> {
            if (firstLoad && expandedPaths.isEmpty()) {
                myTree.expandAll(1);
            }
            for (List<Object> path : expandedPaths) {
                expandPath(uiAccess, path, false);
            }
            if (!selectedPath.isEmpty()) {
                expandPath(uiAccess, selectedPath, true);
            }
        }, uiAccess);
    }

    private static List<Object> valuesOf(@Nullable List<TreeNode<Object>> nodes) {
        List<Object> values = new ArrayList<>();
        if (nodes != null) {
            for (TreeNode<Object> node : nodes) {
                Object value = node.getValue();
                if (value != null) {
                    values.add(value);
                }
            }
        }
        return values;
    }

    @RequiredUIAccess
    private void expandPath(UIAccess uiAccess, List<Object> path, boolean select) {
        TreeNode<Object> root = myTree.getRootNode();
        if (root != null && !path.isEmpty()) {
            expandStep(uiAccess, root, path, 0, select);
        }
    }

    private void expandStep(UIAccess uiAccess, TreeNode<Object> parent, List<Object> path, int index, boolean select) {
        Object expected = path.get(index);
        parent.findChild(value -> Objects.equals(value, expected)).whenComplete((child, error) -> {
            if (child == null) {
                return;
            }
            uiAccess.give(() -> {
                if (index == path.size() - 1) {
                    if (select) {
                        myTree.select(child);
                    }
                    else {
                        myTree.expand(child);
                    }
                    return;
                }
                myTree.expand(child).whenComplete((result, expandError) -> expandStep(uiAccess, child, path, index + 1, select));
            });
        });
    }

    private @Nullable SpringModuleBeans findModule(String moduleName) {
        for (SpringModuleBeans module : mySnapshot) {
            if (module.getModuleName().equals(moduleName)) {
                return module;
            }
        }
        return null;
    }

    private @Nullable SpringContextInfo findContext(String moduleName, String contextId) {
        SpringModuleBeans module = findModule(moduleName);
        if (module == null) {
            return null;
        }
        for (SpringContextInfo context : module.contexts()) {
            if (context.getId().equals(contextId)) {
                return context;
            }
        }
        return null;
    }

    private List<SpringBeanInfo> getBeans(KindNode node) {
        SpringContextInfo context = findContext(node.moduleName(), node.contextId());
        if (context == null) {
            return List.of();
        }
        List<SpringBeanInfo> beans = new ArrayList<>();
        for (SpringBeanInfo bean : context.getBeans()) {
            if (bean.getKind() == node.kind()) {
                beans.add(bean);
            }
        }
        beans.sort(Comparator.comparing(SpringBeanInfo::getName, String.CASE_INSENSITIVE_ORDER));
        return beans;
    }

    private static Map<SpringBeanKind, Integer> countByKind(SpringContextInfo context) {
        Map<SpringBeanKind, Integer> counts = new EnumMap<>(SpringBeanKind.class);
        for (SpringBeanInfo bean : context.getBeans()) {
            counts.merge(bean.getKind(), 1, Integer::sum);
        }
        return counts;
    }

    private final class BeansTreeModel implements TreeModel<Object> {
        @Override
        public void buildChildren(Function<Object, TreeNode<Object>> nodeFactory, @Nullable Object parentValue) {
            if (parentValue == null) {
                for (SpringModuleBeans module : mySnapshot) {
                    add(nodeFactory, new ModuleNode(module.getModuleName()), false);
                }
            }
            else if (parentValue instanceof ModuleNode moduleNode) {
                SpringModuleBeans module = findModule(moduleNode.moduleName());
                if (module == null) {
                    return;
                }
                add(nodeFactory, new ProfilesNode(module.getModuleName()), module.profiles().isEmpty());
                for (SpringContextInfo context : module.contexts()) {
                    add(nodeFactory, context, context.getBeans().isEmpty());
                }
            }
            else if (parentValue instanceof ProfilesNode profilesNode) {
                SpringModuleBeans module = findModule(profilesNode.moduleName());
                if (module != null) {
                    for (SpringProfileInfo profile : module.profiles()) {
                        add(nodeFactory, profile, true);
                    }
                }
            }
            else if (parentValue instanceof SpringContextInfo context) {
                for (SpringBeanKind kind : countByKind(context).keySet()) {
                    add(nodeFactory, new KindNode(context.getModuleName(), context.getId(), kind), false);
                }
            }
            else if (parentValue instanceof KindNode kindNode) {
                for (SpringBeanInfo bean : getBeans(kindNode)) {
                    add(nodeFactory, bean, true);
                }
            }
        }

        private void add(Function<Object, TreeNode<Object>> nodeFactory, Object value, boolean leaf) {
            TreeNode<Object> node = nodeFactory.apply(value);
            node.setLeaf(leaf);
            node.setRenderer(SpringBeansView.this::render);
        }

        @Override
        @RequiredUIAccess
        public boolean onDoubleClick(Tree<Object> tree, TreeNode<Object> node) {
            Object value = node.getValue();
            if (value instanceof SpringBeanInfo || value instanceof SpringProfileInfo) {
                navigate(value, true);
                return false;
            }
            return true;
        }
    }

    private void render(Object value, TextItemPresentation presentation) {
        if (value instanceof ModuleNode node) {
            SpringModuleBeans module = findModule(node.moduleName());
            presentation.withIcon(PlatformIconGroup.nodesModule());
            presentation.append(node.moduleName(), TextAttribute.REGULAR_BOLD);
            if (module != null) {
                presentation.append(LocalizeValue.of("  "), TextAttribute.GRAYED);
                presentation.append(SpringLocalize.beansCount(module.getBeanCount()), TextAttribute.GRAYED);
            }
        }
        else if (value instanceof ProfilesNode node) {
            SpringModuleBeans module = findModule(node.moduleName());
            presentation.withIcon(SpringImplIconGroup.springproperty());
            presentation.append(SpringLocalize.beansProfiles(), TextAttribute.REGULAR);
            if (module != null) {
                presentation.append(LocalizeValue.of("  "), TextAttribute.GRAYED);
                presentation.append(SpringLocalize.beansProfilesActive(String.join(", ", module.activeProfiles())), TextAttribute.GRAYED);
            }
        }
        else if (value instanceof SpringProfileInfo profile) {
            presentation.withIcon(SpringImplIconGroup.springproperty());
            presentation.append(profile.getName(), profile.isActive() ? TextAttribute.REGULAR_BOLD : TextAttribute.REGULAR);
            if (profile.isActive()) {
                presentation.append(LocalizeValue.of("  "), TextAttribute.GRAYED);
                presentation.append(SpringLocalize.beansProfileActiveMark(), TextAttribute.GRAYED);
            }
            if (profile.getBeanCount() > 0) {
                presentation.append(LocalizeValue.of("  "), TextAttribute.GRAYED);
                presentation.append(SpringLocalize.beansCount(profile.getBeanCount()), TextAttribute.GRAYED);
            }
        }
        else if (value instanceof SpringContextInfo context) {
            presentation.withIcon(context.getIcon());
            presentation.append(context.getName(), TextAttribute.REGULAR);
            presentation.append(LocalizeValue.of("  "), TextAttribute.GRAYED);
            presentation.append(SpringLocalize.beansCount(context.getBeans().size()), TextAttribute.GRAYED);
        }
        else if (value instanceof KindNode node) {
            presentation.withIcon(node.kind().getIcon());
            presentation.append(node.kind().getName(), TextAttribute.REGULAR);
            presentation.append(LocalizeValue.of("  "), TextAttribute.GRAYED);
            presentation.append(LocalizeValue.of(String.valueOf(getBeans(node).size())), TextAttribute.GRAYED);
        }
        else if (value instanceof SpringBeanInfo bean) {
            presentation.withIcon(bean.getKind().getIcon());
            presentation.append(bean.getName(), bean.isProfileActive() ? TextAttribute.REGULAR : TextAttribute.GRAYED);
            String type = bean.getShortTypeName();
            if (type != null && !type.equalsIgnoreCase(bean.getName())) {
                presentation.append(LocalizeValue.of("  " + type), TextAttribute.GRAY);
            }
            if (bean.isPrimary()) {
                presentation.append(LocalizeValue.of("  @Primary"), TextAttribute.GRAYED);
            }
            if (!bean.getProfiles().isEmpty()) {
                presentation.append(LocalizeValue.of("  [" + String.join(", ", bean.getProfiles()) + "]"), TextAttribute.GRAYED);
            }
        }
    }

    private static String speedSearchText(@Nullable Object value) {
        if (value instanceof ModuleNode node) {
            return node.moduleName();
        }
        if (value instanceof SpringBeanInfo bean) {
            return bean.getName();
        }
        if (value instanceof SpringContextInfo context) {
            return context.getName();
        }
        if (value instanceof SpringProfileInfo profile) {
            return profile.getName();
        }
        if (value instanceof KindNode node) {
            return node.kind().getName().get();
        }
        return "";
    }

    @RequiredUIAccess
    private void showDetails(@Nullable Object value) {
        int request = ++myDetailsRequest;
        if (value instanceof SpringBeanInfo bean) {
            myDetails.center(buildBeanDetails(bean, null));
            ReadAction.nonBlocking(() -> SpringBeanDependencies.collect(myProject, bean))
                .inSmartMode(myProject)
                .expireWith(this)
                .coalesceBy(this, SpringBeanDependencies.class)
                .finishOnUiThread(Application::getDefaultModalityState, dependencies -> {
                    if (request == myDetailsRequest) {
                        myDetails.center(buildBeanDetails(bean, dependencies));
                    }
                })
                .submitDefault();
        }
        else if (value instanceof SpringProfileInfo profile) {
            myDetails.center(buildProfileDetails(profile));
        }
        else if (value instanceof ModuleNode node && findModule(node.moduleName()) instanceof SpringModuleBeans module) {
            myDetails.center(buildModuleDetails(module));
        }
        else {
            myDetails.center(Label.create(SpringLocalize.beansDetailsEmpty()));
        }
    }

    @RequiredUIAccess
    private static Component buildBeanDetails(SpringBeanInfo bean, @Nullable List<SpringBeanDependencies.Dependency> dependencies) {
        FormBuilder builder = FormBuilder.create();
        builder.addLabeled(SpringLocalize.beansDetailsName(), text(bean.getName()));
        if (!bean.getAliases().isEmpty()) {
            builder.addLabeled(SpringLocalize.beansDetailsAliases(), text(String.join(", ", bean.getAliases())));
        }
        builder.addLabeled(SpringLocalize.beansDetailsType(), text(bean.getTypeName() == null ? "?" : bean.getTypeName()));
        builder.addLabeled(SpringLocalize.beansDetailsKind(), Label.create(bean.getKind().getName()));
        builder.addLabeled(SpringLocalize.beansDetailsScope(), text(bean.getScope()));
        if (bean.isPrimary()) {
            builder.addLabeled(SpringLocalize.beansDetailsPrimary(), Label.create(SpringLocalize.beansDetailsYes()));
        }
        if (bean.isLazy()) {
            builder.addLabeled(SpringLocalize.beansDetailsLazy(), Label.create(SpringLocalize.beansDetailsYes()));
        }
        if (!bean.getProfiles().isEmpty()) {
            LocalizeValue profiles = LocalizeValue.of(String.join(", ", bean.getProfiles()));
            builder.addLabeled(
                SpringLocalize.beansDetailsProfiles(),
                Label.create(bean.isProfileActive() ? profiles : SpringLocalize.beansDetailsProfilesInactive(profiles))
            );
        }
        for (String condition : bean.getConditions()) {
            builder.addLabeled(SpringLocalize.beansDetailsCondition(), text(condition));
        }
        if (bean.getLocation() != null) {
            builder.addLabeled(SpringLocalize.beansDetailsDeclaredIn(), text(bean.getLocation()));
        }
        builder.addLabeled(SpringLocalize.beansDetailsContext(), text(bean.getContextName()));
        builder.addLabeled(SpringLocalize.beansDetailsModule(), text(bean.getModuleName()));

        if (dependencies == null) {
            builder.addLabeled(SpringLocalize.beansDetailsDependencies(), Label.create(SpringLocalize.beansDetailsLoading()));
        }
        else if (dependencies.isEmpty()) {
            builder.addLabeled(SpringLocalize.beansDetailsDependencies(), Label.create(SpringLocalize.beansDetailsNone()));
        }
        else {
            for (SpringBeanDependencies.Dependency dependency : dependencies) {
                LocalizeValue candidates = dependency.candidates().isEmpty()
                    ? SpringLocalize.beansDetailsNoCandidates()
                    : LocalizeValue.of(String.join(", ", dependency.candidates()));
                Label label = Label.create(SpringLocalize.beansDetailsDependency(dependency.typeText(), candidates));
                builder.addLabeled(LocalizeValue.of(dependency.name()), label);
            }
        }
        return builder.build();
    }

    @RequiredUIAccess
    private static Component buildProfileDetails(SpringProfileInfo profile) {
        FormBuilder builder = FormBuilder.create();
        builder.addLabeled(SpringLocalize.beansDetailsName(), text(profile.getName()));
        builder.addLabeled(
            SpringLocalize.beansDetailsActive(),
            Label.create(profile.isActive() ? SpringLocalize.beansDetailsYes() : SpringLocalize.beansDetailsNo())
        );
        builder.addLabeled(SpringLocalize.beansDetailsBeans(), text(String.valueOf(profile.getBeanCount())));
        for (VirtualFile file : profile.getConfigFiles()) {
            builder.addLabeled(SpringLocalize.beansDetailsConfigFile(), text(file.getPresentableUrl()));
        }
        return builder.build();
    }

    @RequiredUIAccess
    private static Component buildModuleDetails(SpringModuleBeans module) {
        FormBuilder builder = FormBuilder.create();
        builder.addLabeled(SpringLocalize.beansDetailsModule(), text(module.getModuleName()));
        LocalizeValue active = LocalizeValue.of(String.join(", ", module.activeProfiles()));
        builder.addLabeled(
            SpringLocalize.beansDetailsActiveProfiles(),
            Label.create(module.activeProfilesExplicit() ? active : SpringLocalize.beansDetailsActiveProfilesDefault(active))
        );
        builder.addLabeled(SpringLocalize.beansDetailsContexts(), text(String.valueOf(module.contexts().size())));
        builder.addLabeled(SpringLocalize.beansDetailsBeans(), text(String.valueOf(module.getBeanCount())));
        return builder.build();
    }

    @RequiredUIAccess
    private static Label text(String value) {
        return Label.create(LocalizeValue.of(value));
    }

    @RequiredUIAccess
    private void navigate(Object value, boolean requestFocus) {
        ReadAction.nonBlocking(() -> findNavigatable(value))
            .inSmartMode(myProject)
            .expireWith(this)
            .finishOnUiThread(Application::getDefaultModalityState, navigatable -> {
                if (navigatable != null && navigatable.canNavigate()) {
                    navigatable.navigate(requestFocus);
                }
            })
            .submitDefault();
    }

    private @Nullable Navigatable findNavigatable(Object value) {
        if (value instanceof SpringBeanInfo bean) {
            PsiElement element = bean.getElement();
            return element != null && element.getNavigationElement() instanceof Navigatable navigatable ? navigatable : null;
        }
        if (value instanceof SpringProfileInfo profile && !profile.getConfigFiles().isEmpty()) {
            PsiFile file = PsiManager.getInstance(myProject).findFile(profile.getConfigFiles().get(0));
            return file instanceof Navigatable navigatable ? navigatable : null;
        }
        return null;
    }

    @Override
    public void uiDataSnapshot(DataSink sink) {
        sink.set(KEY, this);
        sink.set(PlatformDataKeys.TREE_EXPANDER, TreeExpander.of(myTree));

        TreeNode<Object> node = myTree.getSelectedNode();
        Object value = node == null ? null : node.getValue();
        if (value instanceof SpringBeanInfo || value instanceof SpringProfileInfo) {
            sink.set(Navigatable.KEY_OF_ARRAY, new Navigatable[]{new ValueNavigatable(value)});
        }
        if (value instanceof SpringBeanInfo bean) {
            sink.lazy(PsiElement.KEY, bean::getElement);
        }
    }

    @Override
    public void dispose() {
        myDisposed = true;
    }

    private final class ValueNavigatable implements Navigatable {
        private final Object myValue;

        private ValueNavigatable(Object value) {
            myValue = value;
        }

        @Override
        @RequiredUIAccess
        public void navigate(boolean requestFocus) {
            SpringBeansView.this.navigate(myValue, requestFocus);
        }

        @Override
        public boolean canNavigate() {
            return true;
        }

        @Override
        public boolean canNavigateToSource() {
            return true;
        }
    }

    private final class RefreshAction extends DumbAwareAction {
        private RefreshAction() {
            super(SpringLocalize.beansActionRefresh(), LocalizeValue.empty(), PlatformIconGroup.actionsRefresh());
        }

        @Override
        @RequiredUIAccess
        public void actionPerformed(AnActionEvent e) {
            reload();
        }
    }
}
