package consulo.spring.impl.module.extension.ui;

import com.intellij.spring.impl.ide.facet.SpringConfigsSearcher;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.facet.XmlSpringFileSet;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.application.concurrent.coroutine.ReadLock;
import consulo.application.progress.ProgressBuilderFactory;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.fileChooser.FileChooser;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.CheckBoxTree;
import consulo.ui.CheckBoxTreePolicy;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.TextBox;
import consulo.ui.TextItemPresentation;
import consulo.ui.TreeModel;
import consulo.ui.TreeNode;
import consulo.ui.UIAccess;
import consulo.ui.WidthAndHeight;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.dialog.Dialog;
import consulo.ui.ex.dialog.DialogDescriptor;
import consulo.ui.ex.dialog.DialogService;
import consulo.ui.ex.dialog.DialogValue;
import consulo.ui.ex.toolbar.AddAction;
import consulo.ui.ex.toolbar.ToolbarDecoratorBuilderFactory;
import consulo.ui.util.FormBuilder;
import consulo.util.collection.MultiMap;
import consulo.util.concurrent.coroutine.Coroutine;
import consulo.util.concurrent.coroutine.CoroutineScope;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.StandardFileSystems;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFilePresentation;
import consulo.virtualFileSystem.pointer.VirtualFilePointer;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * @author Dmitry Avdeev
 */
public final class SpringFileSetEditorDialogDescriptor extends DialogDescriptor {
    private static final String DIMENSION_SERVICE_KEY = "spring file set editor";

    private static final CheckBoxTreePolicy CHECK_POLICY = new CheckBoxTreePolicy(true, true, false, true);

    private final Project myProject;
    private final SpringFileSet myOriginalSet;
    private final List<SpringFileSet> myParentCandidates;
    private final Map<Object, List<VirtualFile>> myGroups;
    private final List<VirtualFile> myLooseFiles;
    private final Set<VirtualFile> myInitiallyCheckedFiles;
    private final Set<String> myOriginalUrls;
    private final Map<Object, SpringConfigFilePresentation> myPresentations;

    private @Nullable TextBox myNameBox;
    private @Nullable ComboBox<Object> myParentBox;
    private @Nullable CheckBoxTree<Object> myTree;
    private @Nullable Disposable myUIDisposable;
    private @Nullable SpringFileSet myEditedFileSet;

    private SpringFileSetEditorDialogDescriptor(
        Project project,
        SpringFileSet fileSet,
        List<SpringFileSet> parentCandidates,
        SpringConfigFilesSearchResult searchResult
    ) {
        super(SpringLocalize.configFilesetEditorTitle());
        myProject = project;
        myOriginalSet = fileSet;
        myParentCandidates = parentCandidates;
        myGroups = searchResult.groups();
        myLooseFiles = new ArrayList<>(searchResult.looseFiles());
        myInitiallyCheckedFiles = searchResult.checkedFiles();
        myOriginalUrls = toUrls(searchResult.checkedFiles());
        myPresentations = new HashMap<>(searchResult.presentations());
    }

    @RequiredUIAccess
    public static CompletableFuture<SpringFileSet> showAsync(
        @Nullable Component parent,
        Module module,
        SpringFileSet fileSet,
        Collection<SpringFileSet> allSets
    ) {
        Project project = module.getProject();
        UIAccess uiAccess = UIAccess.current();

        List<VirtualFilePointer> pointers = List.copyOf(fileSet.getFiles());
        List<SpringFileSet> parentCandidates = new ArrayList<>();
        for (SpringFileSet set : allSets) {
            if (!set.getId().equals(fileSet.getId())) {
                parentCandidates.add(set);
            }
        }

        return Application.get().getInstance(ProgressBuilderFactory.class)
            .newProgressBuilder(project, SpringLocalize.configFilesetSearchProgress())
            .cancelable()
            .modal()
            .execute(
                uiAccess,
                () -> Coroutine.first(ReadLock.<Object, SpringConfigFilesSearchResult>apply(ignored -> search(module, pointers)))
            )
            .thenComposeAsync(searchResult -> {
                SpringFileSetEditorDialogDescriptor descriptor =
                    new SpringFileSetEditorDialogDescriptor(project, fileSet, parentCandidates, searchResult);

                DialogService dialogService = Application.get().getInstance(DialogService.class);
                Dialog dialog = parent == null ? dialogService.build(descriptor) : dialogService.build(parent, descriptor);
                return dialog.showAsync().thenApply(value -> descriptor.getEditedFileSet());
            }, uiAccess);
    }

    @Override
    public @Nullable String getDimensionServiceKey() {
        return DIMENSION_SERVICE_KEY;
    }

    @Override
    public WidthAndHeight getInitialSize() {
        return WidthAndHeight.ofFont(50, 30);
    }

    @RequiredUIAccess
    @Override
    public Component createCenterComponent(Disposable uiDisposable) {
        myUIDisposable = uiDisposable;

        TextBox nameBox = TextBox.create(myOriginalSet.getName());
        nameBox.addValueListener(event -> updateOkButtonState());
        myNameBox = nameBox;

        List<Object> parentItems = new ArrayList<>(myParentCandidates);
        parentItems.add(ObjectUtil.NULL);
        ComboBox<Object> parentBox = ComboBox.create(parentItems);
        parentBox.setTextRenderer(SpringFileSetEditorDialogDescriptor::getParentText);
        parentBox.setValue(findInitialParent(), false);
        parentBox.addValueListener(event -> updateOkButtonState());
        myParentBox = parentBox;

        CheckBoxTree<Object> tree = CheckBoxTree.create(new ConfigFilesModel());
        tree.setCheckPolicy(CHECK_POLICY);
        tree.setSpeedSearchConverter(node -> {
            Object value = node.getValue();
            SpringConfigFilePresentation presentation = value == null ? null : myPresentations.get(value);
            return presentation == null ? "" : presentation.name();
        });
        Disposer.register(uiDisposable, tree.destroyHook());
        for (VirtualFile file : myInitiallyCheckedFiles) {
            tree.setChecked(file, true);
        }
        tree.addCheckListener(event -> updateOkButtonState());
        tree.expandAll();
        myTree = tree;

        Component filesComponent = ToolbarDecoratorBuilderFactory.getInstance()
            .create(tree)
            .addOrReplaceAction(new LocateFileAction())
            .build();

        FormBuilder builder = FormBuilder.create();
        builder.addLabeled(SpringLocalize.configInputFileset(), nameBox);
        builder.addLabeled(SpringLocalize.configInputParentFileset(), parentBox);
        builder.setBottom(filesComponent);
        return builder.build();
    }

    @RequiredUIAccess
    @Override
    public @Nullable Component getPreferredFocusedComponent() {
        return myNameBox;
    }

    @Override
    public boolean doUpdateOkButtonState() {
        if (myOriginalSet.isNew()) {
            return true;
        }

        return !getName().equals(myOriginalSet.getName())
            || !getDependencies().equals(myOriginalSet.getDependencies())
            || !toUrls(getCheckedFiles()).equals(myOriginalUrls);
    }

    @Override
    public void onHandleValue(AnAction action, @Nullable DialogValue value) {
        if (isDefaultAction(action)) {
            myEditedFileSet = createEditedFileSet();
        }
    }

    private SpringFileSet getEditedFileSet() {
        SpringFileSet fileSet = myEditedFileSet;
        if (fileSet == null) {
            throw new IllegalStateException("Dialog closed without OK");
        }
        return fileSet;
    }

    private SpringFileSet createEditedFileSet() {
        SpringFileSet fileSet = new XmlSpringFileSet(myOriginalSet);
        fileSet.setName(getName());
        fileSet.setDependencies(getDependencies());

        List<VirtualFile> checkedFiles = getCheckedFiles();
        Set<String> checkedUrls = toUrls(checkedFiles);
        Set<String> keptUrls = new HashSet<>();
        for (VirtualFilePointer pointer : List.copyOf(fileSet.getFiles())) {
            String url = pointer.getUrl();
            if (checkedUrls.contains(url)) {
                keptUrls.add(url);
            }
            else {
                fileSet.removeFile(pointer);
            }
        }

        for (VirtualFile file : checkedFiles) {
            if (!keptUrls.contains(file.getUrl())) {
                fileSet.addFile(file);
            }
        }
        return fileSet;
    }

    private String getName() {
        TextBox nameBox = myNameBox;
        return nameBox == null ? myOriginalSet.getName() : StringUtil.notNullize(nameBox.getValue());
    }

    private List<String> getDependencies() {
        ComboBox<Object> parentBox = myParentBox;
        if (parentBox == null) {
            return myOriginalSet.getDependencies();
        }
        return parentBox.getValue() instanceof SpringFileSet parent ? List.of(parent.getId()) : List.of();
    }

    private List<VirtualFile> getCheckedFiles() {
        CheckBoxTree<Object> tree = myTree;
        if (tree == null) {
            return List.copyOf(myInitiallyCheckedFiles);
        }

        List<VirtualFile> files = new ArrayList<>();
        for (Object value : tree.getCheckedValues()) {
            if (value instanceof VirtualFile file && !myGroups.containsKey(file)) {
                files.add(file);
            }
        }
        return files;
    }

    private Object findInitialParent() {
        List<String> dependencies = myOriginalSet.getDependencies();
        if (!dependencies.isEmpty()) {
            String dependency = dependencies.get(0);
            for (SpringFileSet candidate : myParentCandidates) {
                if (candidate.getId().equals(dependency)) {
                    return candidate;
                }
            }
        }
        return ObjectUtil.NULL;
    }

    private static LocalizeValue getParentText(@Nullable Object value) {
        if (value instanceof SpringFileSet fileSet) {
            return fileSet.isNew() ? SpringLocalize.filesetNew() : LocalizeValue.of(fileSet.getName());
        }
        return SpringLocalize.filesetNone();
    }

    private static Set<String> toUrls(Collection<VirtualFile> files) {
        Set<String> urls = new HashSet<>();
        for (VirtualFile file : files) {
            urls.add(file.getUrl());
        }
        return urls;
    }

    @RequiredUIAccess
    private void locateFiles() {
        Disposable uiDisposable = myUIDisposable;
        if (uiDisposable == null) {
            return;
        }

        CoroutineScope scope = CoroutineScope.of(myProject.coroutineContext());
        Disposer.register(uiDisposable, scope::cancel);

        UIAccess uiAccess = UIAccess.current();
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, true, false, true, true);
        FileChooser.chooseFiles(descriptor, myProject, null)
            .thenCompose(files -> {
                List<VirtualFile> chosen = List.of(files);
                return Coroutine
                    .first(ReadLock.<Object, Map<VirtualFile, SpringConfigFilePresentation>>apply(ignored -> presentFiles(chosen)))
                    .runAsync(scope, null)
                    .toFuture();
            })
            .whenCompleteAsync((presentations, error) -> {
                if (error == null && presentations != null && !presentations.isEmpty()) {
                    addLocatedFiles(presentations);
                }
            }, uiAccess);
    }

    @RequiredUIAccess
    private void addLocatedFiles(Map<VirtualFile, SpringConfigFilePresentation> presentations) {
        CheckBoxTree<Object> tree = myTree;
        if (tree == null) {
            return;
        }

        boolean added = false;
        for (Map.Entry<VirtualFile, SpringConfigFilePresentation> entry : presentations.entrySet()) {
            VirtualFile file = entry.getKey();
            if (!isShown(file)) {
                myLooseFiles.add(file);
                myPresentations.put(file, entry.getValue());
                added = true;
            }
            tree.setChecked(file, true);
        }

        if (added) {
            UIAccess uiAccess = UIAccess.current();
            tree.refreshAll().whenCompleteAsync((ignored, error) -> tree.expandAll(), uiAccess);
        }
        updateOkButtonState();
    }

    private boolean isShown(VirtualFile file) {
        if (myLooseFiles.contains(file)) {
            return true;
        }
        for (List<VirtualFile> files : myGroups.values()) {
            if (files.contains(file)) {
                return true;
            }
        }
        return false;
    }

    private void render(Object value, TextItemPresentation presentation) {
        SpringConfigFilePresentation filePresentation = myPresentations.get(value);
        if (filePresentation != null) {
            filePresentation.render(presentation);
        }
    }

    @RequiredReadAction
    private static SpringConfigFilesSearchResult search(Module module, List<VirtualFilePointer> pointers) {
        Project project = module.getProject();

        Map<Object, List<VirtualFile>> groups = new LinkedHashMap<>();
        Map<Object, SpringConfigFilePresentation> presentations = new HashMap<>();
        Set<VirtualFile> found = new HashSet<>();

        if (!DumbService.isDumb(project)) {
            SpringConfigsSearcher searcher = new SpringConfigsSearcher(module);
            searcher.search();

            MultiMap<Module, PsiFile> filesByModules = searcher.getFilesByModules();
            List<Module> modules = new ArrayList<>(filesByModules.keySet());
            modules.sort(Comparator.comparing(Module::getName));
            for (Module fileModule : modules) {
                presentations.put(fileModule, new SpringConfigFilePresentation(PlatformIconGroup.nodesModule(), fileModule.getName(), ""));
                groups.put(fileModule, collectFiles(filesByModules.get(fileModule), presentations, found));
            }

            PsiManager psiManager = PsiManager.getInstance(project);
            MultiMap<VirtualFile, PsiFile> filesByArchives = searcher.getJars();
            List<VirtualFile> archives = new ArrayList<>(filesByArchives.keySet());
            archives.sort(Comparator.comparing(VirtualFile::getName));
            for (VirtualFile archive : archives) {
                PsiFile archivePsiFile = psiManager.findFile(archive);
                if (archivePsiFile == null) {
                    continue;
                }

                presentations.put(archive, presentPsiFile(archivePsiFile, archive));
                groups.put(archive, collectFiles(filesByArchives.get(archive), presentations, found));
            }
        }

        Set<VirtualFile> checkedFiles = new LinkedHashSet<>();
        List<VirtualFile> looseFiles = new ArrayList<>();
        for (VirtualFilePointer pointer : pointers) {
            VirtualFile file = pointer.getFile();
            if (file == null) {
                continue;
            }

            checkedFiles.add(file);
            if (found.add(file)) {
                looseFiles.add(file);
                presentations.put(file, presentFile(file));
            }
        }

        return new SpringConfigFilesSearchResult(groups, looseFiles, checkedFiles, presentations);
    }

    @RequiredReadAction
    private static List<VirtualFile> collectFiles(
        Collection<PsiFile> psiFiles,
        Map<Object, SpringConfigFilePresentation> presentations,
        Set<VirtualFile> found
    ) {
        List<PsiFile> sorted = new ArrayList<>(psiFiles);
        sorted.sort(Comparator.comparing(PsiFile::getName));

        List<VirtualFile> files = new ArrayList<>();
        for (PsiFile psiFile : sorted) {
            VirtualFile file = psiFile.getVirtualFile();
            if (file == null || !found.add(file)) {
                continue;
            }

            files.add(file);
            presentations.put(file, presentPsiFile(psiFile, file));
        }
        return files;
    }

    @RequiredReadAction
    private static Map<VirtualFile, SpringConfigFilePresentation> presentFiles(List<VirtualFile> files) {
        Map<VirtualFile, SpringConfigFilePresentation> presentations = new LinkedHashMap<>();
        for (VirtualFile file : files) {
            presentations.put(file, presentFile(file));
        }
        return presentations;
    }

    @RequiredReadAction
    private static SpringConfigFilePresentation presentPsiFile(PsiFile psiFile, VirtualFile file) {
        return new SpringConfigFilePresentation(IconDescriptorUpdaters.getIcon(psiFile, 0), psiFile.getName(), getLocation(file));
    }

    private static SpringConfigFilePresentation presentFile(VirtualFile file) {
        return new SpringConfigFilePresentation(VirtualFilePresentation.getIcon(file), file.getName(), getLocation(file));
    }

    private static String getLocation(VirtualFile file) {
        String path = file.getPath();
        int index = path.indexOf(StandardFileSystems.JAR_SEPARATOR);
        return index >= 0 ? path.substring(index + StandardFileSystems.JAR_SEPARATOR.length()) : path;
    }

    private class ConfigFilesModel implements TreeModel<Object> {
        @Override
        public void buildChildren(Function<Object, TreeNode<Object>> nodeFactory, @Nullable Object parentValue) {
            if (parentValue == null) {
                for (Object group : myGroups.keySet()) {
                    add(nodeFactory, group, false);
                }
                for (VirtualFile file : myLooseFiles) {
                    add(nodeFactory, file, true);
                }
                return;
            }

            for (VirtualFile file : myGroups.getOrDefault(parentValue, List.of())) {
                add(nodeFactory, file, true);
            }
        }

        private void add(Function<Object, TreeNode<Object>> nodeFactory, Object value, boolean leaf) {
            TreeNode<Object> node = nodeFactory.apply(value);
            node.setLeaf(leaf);
            node.setRenderer(SpringFileSetEditorDialogDescriptor.this::render);
        }
    }

    private class LocateFileAction extends AddAction<Object> {
        private LocateFileAction() {
            getTemplatePresentation().setText(SpringLocalize.configLocateButton());
        }

        @Override
        @RequiredUIAccess
        protected void doAdd(AnActionEvent e) {
            locateFiles();
        }
    }
}
