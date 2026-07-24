/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopAspect;
import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamChildrenQuery;
import com.intellij.jam.reflect.JamClassMeta;
import com.intellij.java.language.psi.PsiClass;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementRef;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * @author peter
 */
public abstract class AopAspectImpl implements AopAspect, JamElement {
  private static final JamChildrenQuery<AopIntroductionImpl> INTRODUCTIONS_QUERY =
    JamChildrenQuery.annotatedFields(AopConstants.DECLARE_PARENTS_ANNO, AopIntroductionImpl.class);

  public static final JamAnnotationMeta ASPECT_ANNO_META = new JamAnnotationMeta(AopConstants.ASPECT_ANNO);
  public static final JamClassMeta<AopAspectImpl> ASPECT_META = new JamClassMeta<>(AopAspectImpl.class).
    addAnnotation(ASPECT_ANNO_META).
    addChildrenQuery(INTRODUCTIONS_QUERY);

  private static final JamChildrenQuery<AopAdviceImpl> ADVICE_QUERY = JamChildrenQuery.composite(
    ASPECT_META.addAnnotatedMethodsQuery(AopAdviceMetas.BEFORE_META, AopAdviceImpl.Before.class),
    ASPECT_META.addAnnotatedMethodsQuery(AopAdviceMetas.AFTER_META, AopAdviceImpl.After.class),
    ASPECT_META.addAnnotatedMethodsQuery(AopAdviceMetas.AROUND_META, AopAdviceImpl.Around.class),
    ASPECT_META.addAnnotatedMethodsQuery(AopAdviceMetas.AFTER_RETURNING_META, AopAfterReturningAdviceImpl.class),
    ASPECT_META.addAnnotatedMethodsQuery(AopAdviceMetas.AFTER_THROWING_META, AopAfterThrowingAdviceImpl.class)
  );

  @Override
  public PsiElement getIdentifyingPsiElement() {
    return ASPECT_ANNO_META.getAnnotation(getPsiClass());
  }

  @Nonnull
  @Override
  @JamPsiConnector
  public abstract PsiClass getPsiClass();

  @Override
  public List<AopAdviceImpl> getAdvices() {
    return ADVICE_QUERY.findChildren(PsiElementRef.real(getPsiClass()));
  }

  @Override
  public List<AopIntroductionImpl> getIntroductions() {
    return INTRODUCTIONS_QUERY.findChildren(PsiElementRef.real(getPsiClass()));
  }
}
