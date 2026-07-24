/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopAdvice;
import com.intellij.aop.AopAdviceType;
import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.psi.PointcutContext;
import com.intellij.aop.psi.PointcutMatchDegree;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.jam.JamChief;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiAnnotationMemberValue;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.module.Module;

import consulo.xml.language.psi.XmlTag;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public abstract class AopAdviceImpl implements JamChief, AopAdvice, PointcutContainer {
  private final AopAdviceType myType;
  protected final JamAnnotationMeta myAnnoMeta;

  public AopAdviceImpl(AopAdviceType type, JamAnnotationMeta annoMeta) {
    myAnnoMeta = annoMeta;
    myType = type;
  }

  @Nullable
  protected PsiAnnotationMemberValue getAnnoParam() {
    return myAnnoMeta.getAttribute(getPsiElement(), AopAdviceMetas.VALUE_ATTR).getPsiElement();
  }

  @Override
  public JamStringAttributeElement<String> getArgNames() {
    return myAnnoMeta.getAttribute(getPsiElement(), AopAdviceMetas.ARG_NAMES_ATTR);
  }

  @Nullable
  @Override
  public PsiPointcutExpression getPointcutExpression() {
    PsiAnnotationMemberValue param = getAnnoParam();
    return AopPointcutImpl.getPsiPointcutExpression(param);
  }

  @Nonnull
  @Override
  public AopAdviceType getAdviceType() {
    return myType;
  }

  @Override
  public AopAdvisedElementsSearcher getSearcher() {
    PsiPointcutExpression expression = getPointcutExpression();
    return expression == null ? null : expression.getContainingFile().getAopModel().getAdvisedElementsSearcher();
  }

  @Override
  public PointcutMatchDegree accepts(PsiMethod method) {
    PsiPointcutExpression expression = getPointcutExpression();
    return expression == null ? PointcutMatchDegree.FALSE : expression.acceptsSubject(new PointcutContext(expression), method);
  }

  @Override
  public XmlTag getXmlTag() {
    return null;
  }

  @Override
  public Module getModule() {
    return null;
  }

  @Override
  public PsiAnnotation getIdentifyingPsiElement() {
    return myAnnoMeta.getAnnotation(getPsiElement());
  }

  @Override
  public PsiFile getContainingFile() {
    return getPsiElement().getContainingFile();
  }

  @Nullable
  public static PsiParameter findParameter(@Nullable PsiMethod method, @Nonnull String parameterName) {
    if (method != null) {
      PsiParameter[] parameters = method.getParameterList().getParameters();
      for (PsiParameter parameter : parameters) {
        if (parameterName.equals(parameter.getName())) return parameter;
      }
    }
    return null;
  }

  @Nullable
  @Override
  public PsiAnnotation getAnnotation() {
    return getIdentifyingPsiElement();
  }

  @Override
  public PsiManager getPsiManager() {
    return getPsiElement().getManager();
  }

  @Override
  @RequiredReadAction
  public boolean isValid() {
    return getPsiElement().isValid();
  }

  @JamPsiConnector
  @Override
  public abstract PsiMethod getPsiElement();

  public abstract static class Before extends AopAdviceImpl {
    public Before() {
      super(AopAdviceType.BEFORE, AopAdviceMetas.BEFORE_META);
    }
  }
  abstract public static class After extends AopAdviceImpl {
    public After() {
      super(AopAdviceType.AFTER, AopAdviceMetas.AFTER_META);
    }
  }
  public abstract static class Around extends AopAdviceImpl {
    public Around() {
      super(AopAdviceType.AROUND, AopAdviceMetas.AROUND_META);
    }
  }
}