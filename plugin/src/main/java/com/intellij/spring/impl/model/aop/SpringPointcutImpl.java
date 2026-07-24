/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.aop;

import com.intellij.aop.psi.AopReferenceExpression;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.java.language.psi.PsiParameter;
import com.intellij.spring.impl.ide.model.xml.aop.SpringPointcut;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiRecursiveElementVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * @author peter
 */
public abstract class SpringPointcutImpl implements SpringPointcut {
  public PsiElement getIdentifyingPsiElement() {
    return getXmlTag();
  }

  public int getParameterCount() {
    PsiPointcutExpression expression = getExpression().getValue();
    if (expression == null) return -1;

    final Set<String> paramNames = new HashSet<String>();
    expression.acceptChildren(new PsiRecursiveElementVisitor() {
      @Override public void visitElement(PsiElement element) {
        if (element instanceof AopReferenceExpression) {
          PsiElement psiElement = ((AopReferenceExpression)element).resolve();
          if (psiElement instanceof PsiParameter) {
            paramNames.add(((PsiParameter)psiElement).getName());
          }
        }
        super.visitElement(element);
      }
    });
    return paramNames.size();
  }

}
