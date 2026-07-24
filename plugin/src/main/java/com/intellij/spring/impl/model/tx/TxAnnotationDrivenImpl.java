/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.tx;

import com.intellij.aop.AopAdvice;
import com.intellij.aop.AopAdviceType;
import com.intellij.aop.AopIntroduction;
import com.intellij.aop.psi.PointcutMatchDegree;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.java.language.codeInsight.AnnotationUtil;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMember;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.spring.impl.ide.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.tx.AnnotationDriven;
import consulo.application.util.function.Processor;
import consulo.xml.dom.DomUtil;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author peter
 */
public abstract class TxAnnotationDrivenImpl extends DomSpringBeanImpl implements AnnotationDriven {
  @NonNls private static final String TRANSACTIONAL = "org.springframework.transaction.annotation.Transactional";

  @Nonnull
  public String getClassName() {
    return "org.springframework.transaction.interceptor.TransactionInterceptor";
  }

  public PointcutMatchDegree accepts(PsiMethod method) {
    if (!isTransactionallyAnnotated(method)) {
      PsiClass psiClass = method.getContainingClass();
      if (psiClass == null) return PointcutMatchDegree.FALSE;
      if (isTransactionallyAnnotated(psiClass) || hasAnnotatedInterface(psiClass)) {
        return PointcutMatchDegree.TRUE;
      }

      return PointcutMatchDegree.FALSE;
    }
    return PointcutMatchDegree.TRUE;
  }

  private static boolean hasAnnotatedInterface(PsiClass psiClass) {
    return !InheritanceUtil.processSupers(psiClass, false, new Processor<PsiClass>() {
      public boolean process(PsiClass psiClass) {
        if (psiClass.isInterface() && isTransactionallyAnnotated(psiClass)) {
          return false;
        }
        return true;
      }
    });
  }

  private static boolean isTransactionallyAnnotated(PsiMember method) {
    return AnnotationUtil.isAnnotated(method, TRANSACTIONAL, true, true);
  }

  @Nonnull
  public AopAdviceType getAdviceType() {
    return AopAdviceType.AROUND;
  }

  public PsiPointcutExpression getPointcutExpression() {
    return null;
  }

  public PsiClass getPsiClass() {
    return null;
  }

  public List<? extends AopIntroduction> getIntroductions() {
    return Collections.emptyList();
  }

  public List<? extends AopAdvice> getAdvices() {
    return Collections.singletonList(this);
  }

  @Nonnull
  public SpringAdvisedElementsSearcher getSearcher() {
    return new SpringAdvisedElementsSearcher(getPsiManager(), SpringUtils.getNonEmptySpringModelsByFile(DomUtil.getFile(this)));
  }
}
