/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopPointcut;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.jam.JamConverter;
import com.intellij.jam.JamElement;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.model.common.ReadOnlyGenericValue;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamMethodMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiAnnotationMemberValue;
import com.intellij.java.language.psi.PsiBinaryExpression;
import com.intellij.java.language.psi.PsiMethod;
import consulo.document.util.TextRange;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.language.psi.PsiManager;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.Pair;
import consulo.xml.dom.GenericValue;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

/**
 * @author peter
 */
public abstract class AopPointcutImpl implements JamElement, AopPointcut, PointcutContainer {
  private static final JamAnnotationMeta POINTCUT_META = new JamAnnotationMeta(AopConstants.POINTCUT_ANNO);
  public static final JamMethodMeta<AopPointcutImpl> POINTCUT_METHOD_META = new JamMethodMeta<AopPointcutImpl>(AopPointcutImpl.class);

  private static final JamStringAttributeMeta.Single<String> ARG_NAMES_META = JamAttributeMeta.singleString("argNames");

  public GenericValue<PsiPointcutExpression> getExpression() {
    JamStringAttributeMeta.Single<PsiPointcutExpression> meta =
      JamAttributeMeta.singleString("value", new JamConverter<PsiPointcutExpression>() {
        @Override
        public PsiPointcutExpression fromString(@Nullable String s, JamStringAttributeElement<PsiPointcutExpression> context) {
          return getPointcutExpression(context.getPsiElement());
        }
      });
    return POINTCUT_META.getAttribute(getPsiElement(), meta);
  }

  public PsiElement getIdentifyingPsiElement() {
    PsiAnnotation annotation = getAnnotation();
    return annotation == null ? getPsiElement() : annotation;
  }

  public JamStringAttributeElement<String> getArgNames() {
    return POINTCUT_META.getAttribute(getPsiElement(), ARG_NAMES_META);
  }

  public GenericValue<String> getQualifiedName() {
    return ReadOnlyGenericValue.getInstance(getPsiElement().getContainingClass().getQualifiedName() + "." + getPsiElement().getName());
  }

  public int getParameterCount() {
    return getPsiElement().getParameterList().getParametersCount();
  }

  @Nullable
  protected PsiPointcutExpression getPointcutExpression(@Nullable PsiAnnotationMemberValue value) {
    return getPsiPointcutExpression(value);
  }

  @Nullable
  public static PsiPointcutExpression getPsiPointcutExpression(@Nullable PsiElement value) {
    if (value instanceof PsiBinaryExpression) {
      return getPsiPointcutExpression(((PsiBinaryExpression)value).getLOperand());
    }

    if (value instanceof PsiLanguageInjectionHost) {
      List<Pair<PsiElement, TextRange>> list = InjectedLanguageManager.getInstance(value.getProject()).getInjectedPsiFiles(value);
      if (list != null) {
        Pair<PsiElement, TextRange> pair = ContainerUtil.find(list, pair1 -> pair1.first instanceof AopPointcutExpressionFile);
        if (pair != null) {
          return ((AopPointcutExpressionFile)pair.first).getPointcutExpression();
        }
      }
    }
    return null;
  }

  @Nullable
  public PsiAnnotation getAnnotation() {
    return POINTCUT_META.getAnnotation(getPsiElement());
  }

  public PsiManager getPsiManager() {
    return getPsiElement().getManager();
  }

  @Nonnull
  @JamPsiConnector
  public abstract PsiMethod getPsiElement();

  public boolean isValid() {
    return getPsiElement().isValid();
  }
}
