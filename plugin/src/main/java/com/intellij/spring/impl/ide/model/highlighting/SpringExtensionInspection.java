package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.facet.FileSetEditor;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.facet.XmlSpringFileSet;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.WriteAction;
import consulo.application.util.function.Processor;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataManager;
import consulo.language.editor.DaemonCodeAnalyzer;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.inspection.InspectionToolState;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.ProjectRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.project.Project;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import consulo.spring.impl.module.extension.SpringMutableModuleExtension;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.popup.BaseListPopupStep;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.ui.ex.popup.PopupStep;
import consulo.util.lang.ref.Ref;
import consulo.virtualFileSystem.VirtualFile;
import consulo.xml.dom.DomFileElement;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import consulo.xml.dom.editor.DomElementAnnotationsManager;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Dmitry Avdeev
 */
@ExtensionImpl
public class SpringExtensionInspection extends SpringBeanInspectionBase<SpringExtensionInspectionState> {
    @Nonnull
    @Override
    public InspectionToolState<?> createStateProvider() {
        return new SpringExtensionInspectionState();
    }

    @Override
    public void checkFileElement(
        DomFileElement<Beans> domFileElement,
        DomElementAnnotationHolder holder,
        SpringExtensionInspectionState state
    ) {
        consulo.module.Module module = domFileElement.getModule();
        if (module == null) {
            return;
        }
        final VirtualFile virtualFile = domFileElement.getFile().getVirtualFile();
        if (virtualFile == null) {
            return;
        }
        ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(module.getProject()).getFileIndex();
        if (!projectFileIndex.isInSourceContent(virtualFile) ||
            (!state.checkTestFiles && projectFileIndex.isInTestSourceContent(virtualFile))) {
            return;
        }
        final Ref<SpringModuleExtension> moduleExtensionRef = new Ref<SpringModuleExtension>();
        boolean notFound = ModuleUtilCore.visitMeAndDependentModules(
            module,
            new Processor<Module>() {
                @Override
                public boolean process(consulo.module.Module module) {
                    SpringModuleExtension facet = SpringModuleExtension.getInstance(module);
                    if (facet != null) {
                        moduleExtensionRef.set(facet);
                        Set<SpringFileSet> sets = SpringManager.getInstance(module.getProject()).getAllSets(facet);
                        for (SpringFileSet fileSet : sets) {
                            if (fileSet.hasFile(virtualFile)) {
                                return false;
                            }
                        }
                    }
                    return true;
                }
            }
        );
        if (!notFound) {
            return;
        }
        SpringModuleExtension moduleExtension = moduleExtensionRef.get();
        if (moduleExtension == null) {
            holder.createProblem(
                domFileElement,
                HighlightSeverity.WARNING,
                SpringLocalize.springFacetNotConfiguredForModule(module.getName()).get(),
                new EnableExtensionFix(module, domFileElement.getFile())
            );
        }
        else {
            holder.createProblem(
                domFileElement,
                HighlightSeverity.WARNING,
                SpringLocalize.fileSetNotConfiguredForFile().get(),
                new ConfigureFileSetFix(moduleExtension.getModule(), domFileElement.getFile())
            );
        }
    }

    @Override
    @Nonnull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.springFacetInspection();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "SpringFacetInspection";
    }

    private static class ConfigureFileSetFix extends EnableExtensionFix {
        protected ConfigureFileSetFix(@Nonnull consulo.module.Module module, PsiFile file) {
            super(module, file);
        }

        @Nonnull
        @Override
        public LocalizeValue getName() {
            return SpringLocalize.configureFileSetForFile();
        }

        @Override
        @RequiredUIAccess
        protected void doFix(Project project) {
            final SpringModuleExtension extension = SpringModuleExtension.getInstance(myModule);
            if (extension != null) {
                final Set<SpringFileSet> sets = extension.getFileSets();
                if (sets.size() == 0) {
                    addNewSet(myModule, sets);
                }
                else {
                    final ArrayList<SpringFileSet> list = new ArrayList<SpringFileSet>(sets);
                    final SpringFileSet newSet =
                        new XmlSpringFileSet(SpringFileSet.getUniqueId(sets), SpringLocalize.filesetNew().get(), extension) {
                            @Override
                            public boolean isNew() {
                                return true;
                            }
                        };
                    list.add(newSet);
                    BaseListPopupStep<SpringFileSet> step =
                        new BaseListPopupStep<SpringFileSet>(SpringLocalize.chooseFileSet().get(), list) {
                            @Override
                            @RequiredUIAccess
                            public PopupStep onChosen(SpringFileSet selectedValue, boolean finalChoice) {
                                if (selectedValue == newSet) {
                                    String name = SpringFileSet.getUniqueName(SpringLocalize.defaultFilesetName().get(), sets);
                                    newSet.setName(name);

                                    editSet(myModule, sets, newSet);
                                }
                                else {
                                    modifyExtensionOnce(myModule, springMutableModuleExtension -> {
                                        selectedValue.addFile(myVirtualFile);
                                    });
                                }
                                return super.onChosen(selectedValue, finalChoice);
                            }
                        };
                    JBPopupFactory.getInstance().createListPopup(step).showInBestPositionFor(DataManager.getInstance().getDataContext());
                }
            }
        }
    }

    private static class EnableExtensionFix implements LocalQuickFix, SyntheticIntentionAction {
        protected final Module myModule;
        protected final VirtualFile myVirtualFile;

        protected EnableExtensionFix(@Nonnull consulo.module.Module module, PsiFile file) {
            myModule = module;
            myVirtualFile = file.getVirtualFile();
        }

        @Override
        @Nonnull
        public LocalizeValue getName() {
            return SpringLocalize.addSpringFacet(myModule.getName());
        }

        @Override
        @Nonnull
        public LocalizeValue getText() {
            return getName();
        }

        @Override
        public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
            return true;
        }

        @Override
        public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            doFix(project);
            DomElementAnnotationsManager.getInstance(project).dropAnnotationsCache();
            DaemonCodeAnalyzer.getInstance(project).restart();
        }

        @Override
        public boolean startInWriteAction() {
            return false;
        }

        @Override
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            doFix(project);
            DomElementAnnotationsManager.getInstance(project).dropAnnotationsCache();
            DaemonCodeAnalyzer.getInstance(project).restart();
        }

        @RequiredUIAccess
        protected void doFix(Project project) {
            SpringModuleExtension extension = new DummySpringModuleExtension(myModule);

            Set<SpringFileSet> sets = SpringManager.getInstance(project).getAllSets(extension);
            for (SpringFileSet fileSet : sets) {
                if (fileSet.hasFile(myVirtualFile)) {
                    return;
                }
            }
            addNewSet(myModule, sets);
        }

        @RequiredUIAccess
        protected void addNewSet(final consulo.module.Module module, final Set<SpringFileSet> sets) {
            SpringFileSet set = new XmlSpringFileSet(
                SpringFileSet.getUniqueId(sets),
                SpringFileSet.getUniqueName(SpringLocalize.defaultFilesetName().get(), sets),
                module
            ) {
                @Override
                public boolean isNew() {
                    return true;
                }
            };
            editSet(module, sets, set);
        }

        @RequiredUIAccess
        protected void editSet(Module module, Set<SpringFileSet> sets, SpringFileSet set) {
            set.addFile(myVirtualFile);
            FileSetEditor editor = new FileSetEditor(myModule, set, sets);
            editor.show();
            if (editor.isOK()) {
                modifyExtensionOnce(module, it -> it.getFileSets().add(editor.getEditedFileSet().cloneTo(it)));
            }
        }

        @Nonnull
        @RequiredUIAccess
        public static SpringModuleExtension modifyExtensionOnce(
            consulo.module.Module module,
            Consumer<SpringMutableModuleExtension> consumer
        ) {
            WriteAction.run(() -> {
                ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
                SpringMutableModuleExtension springExtension = modifiableModel.getExtensionWithoutCheck(SpringMutableModuleExtension.class);
                assert springExtension != null;
                springExtension.setEnabled(true);
                consumer.accept(springExtension);
                modifiableModel.commit();
            });
            return ModuleUtilCore.getExtension(module, SpringModuleExtension.class);
        }
    }
}
