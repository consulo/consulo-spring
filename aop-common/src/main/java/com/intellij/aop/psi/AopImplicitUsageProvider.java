/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.jam.AopConstants;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.ImplicitUsageProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;

/**
 * @author peter
 */
@ExtensionImpl
public class AopImplicitUsageProvider implements ImplicitUsageProvider {
  public boolean isImplicitUsage(PsiElement element) {
    if (element instanceof PsiParameter) {
      PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
      if (method != null && method.getModifierList().findAnnotation(AopConstants.POINTCUT_ANNO) != null) {
        return true;
      }
    }
    return false;
  }

  public boolean isImplicitRead(PsiElement element) {
    return false;
  }

  public boolean isImplicitWrite(PsiElement element) {
    return false;
  }
}
