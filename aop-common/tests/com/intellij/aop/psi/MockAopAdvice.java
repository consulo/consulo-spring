/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopAdvice;
import com.intellij.aop.AopAdviceType;
import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.java.language.psi.PsiMethod;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.util.IncorrectOperationException;
import consulo.module.Module;
import consulo.xml.language.psi.XmlElementFactory;
import consulo.xml.language.psi.XmlTag;
import jakarta.annotation.Nonnull;
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
  @Override
  public PsiPointcutExpression getPointcutExpression() {
    return myPointcutExpression;
  }

  @Nonnull
  @Override
  public AopAdviceType getAdviceType() {
    throw new UnsupportedOperationException("Method getAdviceType is not yet implemented in " + getClass().getName());
  }

  @Override
  public PointcutMatchDegree accepts(PsiMethod method) {
    PsiPointcutExpression expression = getPointcutExpression();
    return expression != null ? expression.acceptsSubject(new PointcutContext(expression), method) : PointcutMatchDegree.FALSE;
  }

  @Override
  public boolean isValid() {
    throw new UnsupportedOperationException("Method isValid is not yet implemented in " + getClass().getName());
  }

  @Nullable
  @Override
  public XmlTag getXmlTag() {
    return myXmlTag;
  }

  @Override
  public PsiManager getPsiManager() {
    throw new UnsupportedOperationException("Method getPsiManager is not yet implemented in " + getClass().getName());
  }

  @Override
  public AopAdvisedElementsSearcher getSearcher() {
    return myPointcutExpression.getContainingFile().getAopModel().getAdvisedElementsSearcher();
  }

  @Nullable
  @Override
  public Module getModule() {
    throw new UnsupportedOperationException("Method getModule is not yet implemented in " + getClass().getName());
  }

  @Nullable
  @Override
  public PsiElement getIdentifyingPsiElement() {
    return getXmlTag();
  }

  @Nullable
  @Override
  public PsiFile getContainingFile() {
    throw new UnsupportedOperationException("Method getContainingFile is not yet implemented in " + getClass().getName());
  }
}
