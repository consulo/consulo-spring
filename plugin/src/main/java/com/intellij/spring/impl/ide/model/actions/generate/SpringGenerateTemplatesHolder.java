package com.intellij.spring.impl.ide.model.actions.generate;

import consulo.application.ApplicationManager;
import consulo.application.Result;
import consulo.codeEditor.Editor;
import consulo.document.util.TextRange;
import consulo.fileEditor.FileEditorManager;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.template.Template;
import consulo.language.editor.template.TemplateManager;
import consulo.language.editor.template.event.TemplateEditingAdapter;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.navigation.OpenFileDescriptor;
import consulo.navigation.OpenFileDescriptorFactory;
import consulo.project.Project;
import consulo.util.lang.Pair;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.Supplier;

public class SpringGenerateTemplatesHolder extends ArrayList<Pair<PsiElement, Supplier<Template>>> {
  private final Project myProject;

  public SpringGenerateTemplatesHolder(Project project) {
    myProject = project;
  }

  public void addTemplateFactory(PsiElement element, Supplier<Template> template) {
    add(new Pair<PsiElement, Supplier<Template>>(element, template));
  }

  public void runTemplates() {
    TemplateManager manager = TemplateManager.getInstance(myProject);


    runTemplates(manager, 0);

  }

  private void runTemplates(final TemplateManager manager, final int index) {
    if(index >= size()) return;
    new WriteCommandAction(myProject) {
      protected void run(Result result) throws Throwable {
        Pair<PsiElement, Supplier<Template>> pair = get(index);
        Editor editor = getEditor(pair.getFirst());
        if (editor != null) {
          PsiDocumentManager.getInstance(myProject).doPostponedOperationsAndUnblockDocument(editor.getDocument());
          Supplier<Template> factory = pair.getSecond();
          if (factory != null) {
            Template template = factory.get();
            if (template != null) {
              manager.startTemplate(editor, template, new TemplateEditingAdapter() {
                public void templateFinished(Template template) {
                  if (index + 1 < size()) {
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                      public void run() {
                        runTemplates(manager, index + 1);
                      }
                    });
                  }
                }
              });
            }
          }
        }
      }
    }.execute();
  }

  @Nullable
  private Editor getEditor(PsiElement element) {
    PsiFile psiFile = element.getContainingFile();
    if (psiFile != null) {
      VirtualFile virtualFile = psiFile.getVirtualFile();
      if (virtualFile != null) {
        TextRange range = element.getTextRange();
        int textOffset = range.getStartOffset();

        OpenFileDescriptor descriptor = OpenFileDescriptorFactory.getInstance(myProject).builder(virtualFile).offset(textOffset).build();
        return FileEditorManager.getInstance(myProject).openTextEditor(descriptor, true);
      }
    }
    return null;
  }
}

