package consulo.spring.impl.module.extension.ui;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.facet.XmlSpringFileSet;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.application.concurrent.coroutine.ReadLock;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.spring.impl.module.extension.SpringMutableModuleExtension;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.Component;
import consulo.ui.MessageBoxes;
import consulo.ui.Tree;
import consulo.ui.TreeExecutor;
import consulo.ui.TreeModel;
import consulo.ui.TreeNode;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.toolbar.AddAction;
import consulo.ui.ex.toolbar.EditAction;
import consulo.ui.ex.toolbar.RemoveAction;
import consulo.ui.ex.toolbar.ToolbarDecoratorBuilderFactory;
import consulo.ui.ex.tree.ApplicationTreeExecutorFactory;
import consulo.ui.image.Image;
import consulo.util.concurrent.coroutine.Coroutine;
import consulo.util.concurrent.coroutine.CoroutineScope;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.pointer.VirtualFilePointer;
import consulo.xml.language.psi.XmlFile;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Dmitry Avdeev
 */
public final class SpringModuleExtensionPanel {
    private final SpringMutableModuleExtension myExtension;
    private final Project myProject;
    private final List<SpringFileSet> myFileSets = new ArrayList<>();

    private volatile List<SpringFileSet> myFileSetsSnapshot = List.of();

    private @Nullable Tree<Object> myTree;

    private SpringModuleExtensionPanel(SpringMutableModuleExtension extension) {
        myExtension = extension;
        myProject = extension.getProject();
    }

    @RequiredUIAccess
    public static Component create(SpringMutableModuleExtension extension, Disposable uiDisposable) {
        return new SpringModuleExtensionPanel(extension).createComponent(uiDisposable);
    }

    @RequiredUIAccess
    @SuppressWarnings("deprecation")
    private Component createComponent(Disposable uiDisposable) {
        myFileSets.addAll(myExtension.getFileSets());
        myFileSetsSnapshot = List.copyOf(myFileSets);

        TreeExecutor executor = Application.get()
            .getInstance(ApplicationTreeExecutorFactory.class)
            .forBackgroundThreadWithReadAction(uiDisposable);

        Tree<Object> tree = Tree.create(new FileSetsModel(), executor);
        Disposer.register(uiDisposable, tree.destroyHook());
        myTree = tree;

        Component component = ToolbarDecoratorBuilderFactory.getInstance()
            .create(tree)
            .addOrReplaceAction(new AddFileSetAction())
            .addOrReplaceAction(new RemoveFileSetAction())
            .addOrReplaceAction(new EditFileSetAction())
            .build();

        UIAccess uiAccess = UIAccess.current();
        tree.expandAll().whenCompleteAsync((ignored, error) -> select(tree, value -> true), uiAccess);

        loadProvidedFileSets(uiDisposable);
        return component;
    }

    @RequiredUIAccess
    private void loadProvidedFileSets(Disposable uiDisposable) {
        if (DumbService.isDumb(myProject)) {
            return;
        }

        UIAccess uiAccess = UIAccess.current();
        CoroutineScope scope = CoroutineScope.of(myProject.coroutineContext());
        Disposer.register(uiDisposable, scope::cancel);

        SpringManager springManager = SpringManager.getInstance(myProject);
        Coroutine.first(ReadLock.<Object, List<SpringFileSet>>apply(ignored -> springManager.getProvidedModels(myExtension)))
            .runAsync(scope, null)
            .toFuture()
            .whenCompleteAsync((fileSets, error) -> {
                if (error == null && fileSets != null && !myProject.isDisposed() && !Disposer.isDisposed(uiDisposable)) {
                    addProvidedFileSets(fileSets);
                }
            }, uiAccess);
    }

    @RequiredUIAccess
    private void addProvidedFileSets(List<SpringFileSet> fileSets) {
        boolean added = false;
        for (SpringFileSet fileSet : fileSets) {
            if (findById(myFileSets, fileSet.getId()) == null) {
                myFileSets.add(fileSet);
                added = true;
            }
        }

        if (added) {
            refresh(null);
        }
    }

    @RequiredUIAccess
    private void addFileSet() {
        SpringFileSet fileSet = new XmlSpringFileSet(
            SpringFileSet.getUniqueId(myFileSets),
            SpringFileSet.getUniqueName(SpringLocalize.defaultFilesetName().get(), myFileSets),
            myExtension
        ) {
            @Override
            public boolean isNew() {
                return true;
            }
        };

        UIAccess uiAccess = UIAccess.current();
        SpringFileSetEditorDialogDescriptor.showAsync(myTree, myExtension.getModule(), fileSet, getVisibleFileSets())
            .whenCompleteAsync((edited, error) -> {
                if (error != null || edited == null) {
                    return;
                }

                Disposer.register(myExtension, edited);
                myFileSets.add(edited);
                applyAndRefresh(edited);
            }, uiAccess);
    }

    @RequiredUIAccess
    private void editFileSet() {
        SpringFileSet fileSet = getSelectedFileSet();
        if (fileSet == null) {
            return;
        }

        UIAccess uiAccess = UIAccess.current();
        SpringFileSetEditorDialogDescriptor.showAsync(myTree, myExtension.getModule(), fileSet, getVisibleFileSets())
            .whenCompleteAsync((edited, error) -> {
                if (error != null || edited == null) {
                    return;
                }

                edited.setAutodetected(false);
                Disposer.register(myExtension, edited);

                int index = indexOf(fileSet);
                if (index >= 0) {
                    myFileSets.set(index, edited);
                }
                else {
                    myFileSets.add(edited);
                }
                applyAndRefresh(edited);
            }, uiAccess);
    }

    @RequiredUIAccess
    private void removeSelected() {
        List<Object> path = getSelectedPath();
        if (path.isEmpty() || !(path.get(0) instanceof SpringFileSet fileSet)) {
            return;
        }

        if (path.size() == 1) {
            confirmRemoveFileSet(fileSet);
            return;
        }

        int index = indexOf(fileSet);
        if (index < 0) {
            return;
        }

        SpringFileSet changed = fileSet.cloneTo(myExtension);
        Object child = path.get(1);
        if (child instanceof SpringFileSet dependency) {
            changed.removeDependency(dependency.getId());
        }
        else if (child instanceof VirtualFilePointer pointer) {
            changed.removeFile(pointer);
        }
        myFileSets.set(index, changed);
        applyAndRefresh(changed);
    }

    @RequiredUIAccess
    private void confirmRemoveFileSet(SpringFileSet fileSet) {
        Tree<Object> tree = myTree;
        if (tree == null) {
            return;
        }

        UIAccess uiAccess = UIAccess.current();
        MessageBoxes.yesNo()
            .title(SpringLocalize.configRemoveButtonTitle())
            .text(SpringLocalize.configRemoveButtonMessage(fileSet.getName()))
            .showAsync(tree)
            .whenCompleteAsync((yes, error) -> {
                if (error == null && Boolean.TRUE.equals(yes)) {
                    removeFileSet(fileSet);
                }
            }, uiAccess);
    }

    @RequiredUIAccess
    private void removeFileSet(SpringFileSet fileSet) {
        int index = indexOf(fileSet);
        if (index < 0) {
            return;
        }

        if (fileSet.isAutodetected()) {
            SpringFileSet removed = fileSet.cloneTo(myExtension);
            removed.setRemoved(true);
            myFileSets.set(index, removed);
        }
        else {
            myFileSets.remove(index);
        }

        for (int i = 0; i < myFileSets.size(); i++) {
            SpringFileSet other = myFileSets.get(i);
            if (other.getDependencies().contains(fileSet.getId())) {
                SpringFileSet changed = other.cloneTo(myExtension);
                changed.removeDependency(fileSet.getId());
                myFileSets.set(i, changed);
            }
        }
        applyAndRefresh(null);
    }

    @RequiredUIAccess
    @SuppressWarnings("deprecation")
    private void applyAndRefresh(@Nullable SpringFileSet toSelect) {
        Set<SpringFileSet> fileSets = myExtension.getFileSets();
        fileSets.clear();
        for (SpringFileSet fileSet : myFileSets) {
            if (!fileSet.isAutodetected() || fileSet.isRemoved()) {
                fileSets.add(fileSet);
            }
        }

        refresh(toSelect);
    }

    @RequiredUIAccess
    private void refresh(@Nullable SpringFileSet toSelect) {
        myFileSetsSnapshot = List.copyOf(myFileSets);

        Tree<Object> tree = myTree;
        if (tree == null) {
            return;
        }

        UIAccess uiAccess = UIAccess.current();
        tree.refreshAll().whenCompleteAsync((ignored, error) -> {
            tree.expandAll();
            if (toSelect != null) {
                select(tree, value -> value == toSelect);
            }
        }, uiAccess);
    }

    @RequiredUIAccess
    private static void select(Tree<Object> tree, Predicate<Object> predicate) {
        TreeNode<Object> root = tree.getRootNode();
        if (root == null) {
            return;
        }

        UIAccess uiAccess = UIAccess.current();
        root.findChild(predicate).whenCompleteAsync((node, error) -> {
            if (node != null) {
                tree.select(node);
            }
        }, uiAccess);
    }

    @RequiredUIAccess
    private List<Object> getSelectedPath() {
        Tree<Object> tree = myTree;
        if (tree == null) {
            return List.of();
        }

        List<Object> values = new ArrayList<>();
        for (TreeNode<Object> node : tree.getSelectedPath()) {
            Object value = node.getValue();
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    @RequiredUIAccess
    private @Nullable SpringFileSet getSelectedFileSet() {
        List<Object> path = getSelectedPath();
        return !path.isEmpty() && path.get(0) instanceof SpringFileSet fileSet ? fileSet : null;
    }

    private List<SpringFileSet> getVisibleFileSets() {
        List<SpringFileSet> fileSets = new ArrayList<>();
        for (SpringFileSet fileSet : myFileSets) {
            if (!fileSet.isRemoved()) {
                fileSets.add(fileSet);
            }
        }
        return fileSets;
    }

    private int indexOf(SpringFileSet fileSet) {
        for (int i = 0; i < myFileSets.size(); i++) {
            if (myFileSets.get(i) == fileSet) {
                return i;
            }
        }
        return -1;
    }

    private static @Nullable SpringFileSet findById(List<SpringFileSet> fileSets, String id) {
        for (SpringFileSet fileSet : fileSets) {
            if (fileSet.getId().equals(id)) {
                return fileSet;
            }
        }
        return null;
    }

    @RequiredReadAction
    private SpringConfigFilePresentation presentConfigFile(VirtualFilePointer pointer) {
        String name = pointer.getFileName();
        String location = pointer.getPresentableUrl();

        LocalizeValue problem = LocalizeValue.empty();
        VirtualFile file = pointer.getFile();
        if (file == null) {
            problem = SpringLocalize.configFileNotFound();
        }
        else if (!DumbService.isDumb(myProject)) {
            PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
            if (!(psiFile instanceof XmlFile xmlFile) || !SpringManager.getInstance(myProject).isSpringBeans(xmlFile)) {
                problem = SpringLocalize.configFileIsNotSpring();
            }
        }
        return new SpringConfigFilePresentation(SpringImplIconGroup.springconfig(), name, location, problem);
    }

    private class FileSetsModel implements TreeModel<Object> {
        @Override
        @RequiredReadAction
        public void buildChildren(Function<Object, TreeNode<Object>> nodeFactory, @Nullable Object parentValue) {
            List<SpringFileSet> fileSets = myFileSetsSnapshot;
            if (parentValue == null) {
                for (SpringFileSet fileSet : fileSets) {
                    if (!fileSet.isRemoved()) {
                        addFileSet(nodeFactory, fileSet);
                    }
                }
            }
            else if (parentValue instanceof SpringFileSet fileSet) {
                for (String dependencyId : fileSet.getDependencies()) {
                    SpringFileSet dependency = findById(fileSets, dependencyId);
                    if (dependency != null) {
                        addDependency(nodeFactory, dependency);
                    }
                }
                for (VirtualFilePointer pointer : fileSet.getFiles()) {
                    addConfigFile(nodeFactory, pointer);
                }
            }
        }

        private void addFileSet(Function<Object, TreeNode<Object>> nodeFactory, SpringFileSet fileSet) {
            Image icon = fileSet.getIcon();
            LocalizeValue name = LocalizeValue.of(fileSet.getName());
            LocalizeValue text = fileSet.isAutodetected()
                ? LocalizeValue.join(name, LocalizeValue.space(), SpringLocalize.configFilesetAutodetected())
                : name;

            TreeNode<Object> node = nodeFactory.apply(fileSet);
            node.setLeaf(false);
            node.setRenderer((value, presentation) -> {
                presentation.withIcon(icon);
                presentation.append(text);
            });
        }

        private void addDependency(Function<Object, TreeNode<Object>> nodeFactory, SpringFileSet dependency) {
            LocalizeValue text = LocalizeValue.of(dependency.getName());

            TreeNode<Object> node = nodeFactory.apply(dependency);
            node.setLeaf(true);
            node.setRenderer((value, presentation) -> {
                presentation.withIcon(PlatformIconGroup.vcsArrow_right());
                presentation.append(text);
            });
        }

        @RequiredReadAction
        private void addConfigFile(Function<Object, TreeNode<Object>> nodeFactory, VirtualFilePointer pointer) {
            SpringConfigFilePresentation filePresentation = presentConfigFile(pointer);

            TreeNode<Object> node = nodeFactory.apply(pointer);
            node.setLeaf(true);
            node.setRenderer((value, presentation) -> filePresentation.render(presentation));
        }
    }

    private class AddFileSetAction extends AddAction<Object> {
        private AddFileSetAction() {
            getTemplatePresentation().setText(SpringLocalize.configAddSetButton());
        }

        @Override
        @RequiredUIAccess
        protected void doAdd(AnActionEvent e) {
            addFileSet();
        }
    }

    private class RemoveFileSetAction extends RemoveAction<Object> {
        @Override
        @RequiredUIAccess
        public void updateAtUI(AnActionEvent e) {
            e.getPresentation().setEnabled(getSelectedFileSet() != null);
        }

        @Override
        @RequiredUIAccess
        public void actionPerformed(AnActionEvent e) {
            removeSelected();
        }
    }

    private class EditFileSetAction extends EditAction<Object> {
        private EditFileSetAction() {
            getTemplatePresentation().setText(SpringLocalize.configEditButton());
        }

        @Override
        @RequiredUIAccess
        public void updateAtUI(AnActionEvent e) {
            e.getPresentation().setEnabled(getSelectedFileSet() != null);
        }

        @Override
        @RequiredUIAccess
        public void actionPerformed(AnActionEvent e) {
            editFileSet();
        }
    }
}
