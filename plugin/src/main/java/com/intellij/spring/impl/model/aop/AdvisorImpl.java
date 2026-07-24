/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.aop;

import com.intellij.aop.AopAdvice;
import com.intellij.aop.AopAdviceType;
import com.intellij.aop.AopIntroduction;
import com.intellij.aop.AopPointcut;
import com.intellij.aop.psi.PointcutContext;
import com.intellij.aop.psi.PointcutMatchDegree;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.spring.impl.ide.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.aop.Advisor;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.xml.dom.DomUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author peter
 */
public abstract class AdvisorImpl extends DomSpringBeanImpl implements Advisor {
  @Override
  public String getClassName() {
    return "com.intellij.spring.model.xml.aop.Advisor";
  }

  @Nullable
  @Override
  public PsiPointcutExpression getPointcutExpression() {
    AopPointcut aopPointcut = getPointcutRef().getValue();
    return aopPointcut != null ? aopPointcut.getExpression().getValue() : getPointcut().getValue();
  }

  @Nonnull
  @Override
  public SpringAdvisedElementsSearcher getSearcher() {
    return new SpringAdvisedElementsSearcher(getPsiManager(), SpringUtils.getNonEmptySpringModelsByFile(DomUtil.getFile(this)));
  }

  @Nonnull
  @Override
  public AopAdviceType getAdviceType() {
    return AopAdviceType.AROUND;
  }

  @Override
  public PointcutMatchDegree accepts(PsiMethod method) {
    PsiPointcutExpression expression = getPointcutExpression();
    return expression != null ? expression.acceptsSubject(new PointcutContext(), method) : PointcutMatchDegree.FALSE;
  }

  @Override
  public List<? extends AopAdvice> getAdvices() {
    return Collections.singletonList(this);
  }

  @Override
  public List<? extends AopIntroduction> getIntroductions() {
    return Collections.emptyList();
  }

  @Nullable
  @Override
  public PsiClass getPsiClass() {
    SpringBeanPointer pointer = getAdviceRef().getValue();
    return pointer == null ? null : pointer.getBeanClass();
  }
}
