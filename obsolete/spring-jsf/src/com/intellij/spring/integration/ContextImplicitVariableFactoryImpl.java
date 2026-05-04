package com.intellij.spring.integration;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Factory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.jsp.JspImplicitVariable;
import com.intellij.psi.impl.FakePsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ContextImplicitVariableFactoryImpl extends ContextImplicitVariableFactory {

  private final PsiFile myDummyFile;

  public ContextImplicitVariableFactoryImpl(final Project project) {
    myDummyFile = PsiFileFactory.getInstance(project).createFileFromText("DummyContextImplicitVariableFactory.java", "");
  }

  @NotNull
  public ContextImplicitVariable createContextVariable(@NotNull final String contextName, @NotNull final Factory<List<JspImplicitVariable>> factory) {
    return new ContextImplicitVariable(contextName, new FakePsiElement() {
      public PsiElement getParent() {
        return myDummyFile;
      }
    }, factory);
  }

  public void dispose() {

  }
}
