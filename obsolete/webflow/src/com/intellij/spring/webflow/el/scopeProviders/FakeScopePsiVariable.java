package com.intellij.spring.webflow.el.scopeProviders;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.RenameableFakePsiElement;
import com.intellij.psi.xml.XmlFile;

import javax.swing.*;

public abstract class FakeScopePsiVariable extends RenameableFakePsiElement {
  private final XmlFile myPsiFile;
  private final PsiElement myNavigationElement;
  private final String myVarName;

  public FakeScopePsiVariable(final XmlFile psiFile, final PsiElement navigationElement, final String varName) {
    super(psiFile);
    myPsiFile = psiFile;
    myNavigationElement = navigationElement;
    myVarName = varName;
  }

  public String getName() {
    return myVarName;
  }

  public PsiElement getParent() {
    return myPsiFile;
  }

  public abstract String getTypeName();
  
  public Icon getIcon() {
    return null;
  }

  @Override
  public PsiElement getNavigationElement() {
    return myNavigationElement;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final FakeScopePsiVariable that = (FakeScopePsiVariable)o;

    if (myNavigationElement != null ? !myNavigationElement.equals(that.myNavigationElement) : that.myNavigationElement != null)
      return false;
    if (myVarName != null ? !myVarName.equals(that.myVarName) : that.myVarName != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myPsiFile != null ? myPsiFile.hashCode() : 0;
    result = 31 * result + (myNavigationElement != null ? myNavigationElement.hashCode() : 0);
    result = 31 * result + (myVarName != null ? myVarName.hashCode() : 0);
    return result;
  }
}
