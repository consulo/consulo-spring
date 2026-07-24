/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiMethod;
import consulo.language.psi.PsiElement;
import consulo.language.psi.meta.PsiMetaData;
import org.jetbrains.annotations.NonNls;

/**
 * @author peter
 */
public class AopMetaData implements PsiMetaData {
  private PsiAnnotation myAnnotation;

  @Override
  public PsiElement getDeclaration() {
    return myAnnotation;
  }

  @Override
  public String getName(PsiElement context) {
    return getName();
  }

  @Override
  public final String getName() {
    return ((PsiMethod)myAnnotation.getParent().getParent()).getName();
  }

  @Override
  public void init(PsiElement element) {
    myAnnotation = (PsiAnnotation) element;
  }

  @Override
  public Object[] getDependences() {
    return new Object[]{myAnnotation};
  }
}
