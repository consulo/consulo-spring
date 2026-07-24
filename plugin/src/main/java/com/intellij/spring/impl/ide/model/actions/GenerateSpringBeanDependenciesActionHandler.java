package com.intellij.spring.impl.ide.model.actions;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.model.actions.generate.GenerateSpringBeanDependenciesUtil;
import com.intellij.spring.impl.ide.model.actions.generate.SpringGenerateTemplatesHolder;
import com.intellij.spring.impl.ide.model.xml.beans.SpringInjection;
import consulo.application.Result;
import consulo.codeEditor.Editor;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.action.CodeInsightActionHandler;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.util.lang.Pair;

import java.util.List;

public class GenerateSpringBeanDependenciesActionHandler implements CodeInsightActionHandler {
  private final boolean mySetterDependency;

  public GenerateSpringBeanDependenciesActionHandler(boolean setterDependency) {
    mySetterDependency = setterDependency;
  }

  public boolean isSetterDependency() {
    return mySetterDependency;
  }

  public void invoke(Project project, Editor editor, PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    PsiElement element = file.findElementAt(offset);
    if (element != null) {
      final PsiClass psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);

      List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> list = new WriteCommandAction<List<Pair<SpringInjection,SpringGenerateTemplatesHolder>>>(psiClass.getProject()) {
        protected void run(Result<List<Pair<SpringInjection, SpringGenerateTemplatesHolder>>> result) throws Throwable {
          List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> list = GenerateSpringBeanDependenciesUtil
                  .generateDependenciesFor(GenerateSpringBeanDependenciesUtil.getSpringModel(psiClass), psiClass, mySetterDependency);

          result.setResult(list);
        }
      }.execute().getResultObject();

      for (Pair<SpringInjection, SpringGenerateTemplatesHolder> pair : list) {
         pair.getSecond().runTemplates();
      }
    }
  }

  public boolean startInWriteAction() {
    return false;
  }
}
