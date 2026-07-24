/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import jakarta.annotation.Nonnull;

import com.intellij.aop.AopAdvice;
import com.intellij.aop.AopAdviceType;
import com.intellij.aop.AopAdvisedElementsSearcher;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.module.Module;
import com.intellij.psi.xml.XmlTag;
import consulo.language.util.IncorrectOperationException;
import com.intellij.util.xml.MockDomElement;

import jakarta.annotation.Nullable;

/**
 * @author peter
*/
public class MockAopAdvice extends MockDomElement implements AopAdvice {
  private final PsiPointcutExpression myPointcutExpression;
  private final XmlTag myXmlTag;

  public MockAopAdvice(PsiPointcutExpression pointcutExpression) throws IncorrectOperationException {
    myPointcutExpression = pointcutExpression;
    myXmlTag = XmlElementFactory.getInstance(pointcutExpression.getProject()).createTagFromText("<a/>");
  }

  @Nullable
  public PsiPointcutExpression getPointcutExpression() {
    return myPointcutExpression;
  }

  @Nonnull
  public AopAdviceType getAdviceType() {
    throw new UnsupportedOperationException("Method getAdviceType is not yet implemented in " + getClass().getName());
  }

  public PointcutMatchDegree accepts(PsiMethod method) {
    PsiPointcutExpression expression = getPointcutExpression();
    return expression != null ? expression.acceptsSubject(new PointcutContext(expression), method) : PointcutMatchDegree.FALSE;
  }

  public boolean isValid() {
    throw new UnsupportedOperationException("Method isValid is not yet implemented in " + getClass().getName());
  }

  @Nullable
  public XmlTag getXmlTag() {
    return myXmlTag;
  }

  public PsiManager getPsiManager() {
    throw new UnsupportedOperationException("Method getPsiManager is not yet implemented in " + getClass().getName());
  }

  public AopAdvisedElementsSearcher getSearcher() {
    return myPointcutExpression.getContainingFile().getAopModel().getAdvisedElementsSearcher();
  }

  @Nullable
  public Module getModule() {
    throw new UnsupportedOperationException("Method getModule is not yet implemented in " + getClass().getName());
  }

  @Nullable
  public PsiElement getIdentifyingPsiElement() {
    return getXmlTag();
  }

  @Nullable
  public PsiFile getContainingFile() {
    throw new UnsupportedOperationException("Method getContainingFile is not yet implemented in " + getClass().getName());
  }
}
