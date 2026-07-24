/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.aop;

import com.intellij.aop.AopAdviceType;
import com.intellij.aop.AopPointcut;
import com.intellij.aop.psi.PointcutContext;
import com.intellij.aop.psi.PointcutMatchDegree;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.jam.model.common.BaseImpl;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.spring.impl.ide.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.aop.BasicAdvice;
import consulo.xml.dom.DomUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public abstract class BasicAdviceImpl extends BaseImpl implements BasicAdvice {

  @Nullable
  public PsiPointcutExpression getPointcutExpression() {
    AopPointcut aopPointcut = getPointcutRef().getValue();
    return aopPointcut != null ? aopPointcut.getExpression().getValue() : getPointcut().getValue();
  }

  @Nonnull
  public SpringAdvisedElementsSearcher getSearcher() {
    return new SpringAdvisedElementsSearcher(getPsiManager(), SpringUtils.getNonEmptySpringModelsByFile(DomUtil.getFile(this)));
  }

  @Nonnull
  public AopAdviceType getAdviceType() {
    return AopAdviceType.valueOf(getXmlElementName().replace('-', '_').toUpperCase());
  }

  public PointcutMatchDegree accepts(PsiMethod method) {
    PsiPointcutExpression expression = getPointcutExpression();
    if (expression == null) return PointcutMatchDegree.FALSE;

    return expression.acceptsSubject(new PointcutContext(getMethod().getValue()), method);
  }


}
