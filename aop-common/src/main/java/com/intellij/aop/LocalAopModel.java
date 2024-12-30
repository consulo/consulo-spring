/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop;

import com.intellij.aop.jam.AopConstants;
import com.intellij.aop.jam.AopModuleService;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import consulo.application.util.NotNullLazyValue;
import consulo.language.psi.PsiElement;
import consulo.language.util.ModuleUtilCore;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

/**
 * @author peter
 */
public class LocalAopModel implements AopModel {
  private final NotNullLazyValue<AopModel> myDelegate = new NotNullLazyValue<AopModel>() {
    @Nonnull
    protected AopModel compute() {
      return getAopModel();
    }
  };
  private final AopAdvisedElementsSearcher myAdvisedElementsSearcher;


  private AopModel getAopModel() {
    return AopModuleService.getAopModel(myHost != null ? ModuleUtilCore.findModuleForPsiElement(myHost) : null);
  }

  private final PsiMethod myMethod;
  private final PsiElement myHost;

  public LocalAopModel(final AopAdvisedElementsSearcher searcher) {
    this(null, null, searcher);
  }

  public LocalAopModel(@Nullable final PsiElement host, @Nullable final PsiMethod pointcutMethod, @Nonnull AopAdvisedElementsSearcher searcher) {
    myHost = host;
    myMethod = pointcutMethod;
    myAdvisedElementsSearcher = searcher;
  }

  protected PsiElement getHost() {
    return myHost;
  }

  public List<? extends AopAspect> getAspects() {
    return myDelegate.getValue().getAspects();
  }

  public List<? extends AopPointcut> getPointcuts() {
    return myDelegate.getValue().getPointcuts();
  }

  public List<AopIntroduction> getIntroductions() {
    final consulo.util.collection.SmartList<AopIntroduction> introductions = new SmartList<AopIntroduction>();
    for (final AopAspect aspect : getAspects()) {
      introductions.addAll(aspect.getIntroductions());
    }
    return introductions;
  }

  @Nullable
  public PsiMethod getPointcutMethod() {
    return myMethod;
  }

  @Nonnull
  public List<PsiParameter> resolveParameters(@Nonnull @NonNls String name) {
    return ContainerUtil.createMaybeSingletonList(findParameter(name, getPointcutMethod()));
  }

  @Nullable
  protected static PsiParameter findParameter(final String name, @Nullable final PsiMethod method) {
    if (method == null) return null;

    for (final PsiParameter parameter : method.getParameterList().getParameters()) {
      if (name.equals(parameter.getName())) return parameter;
    }
    return null;
  }

  @Nonnull
  public ArgNamesManipulator getArgNamesManipulator() {
    throw new UnsupportedOperationException();
  }

  public static boolean isJoinPointParamer(final PsiParameter parameter) {
    final String typeText = parameter.getType().getCanonicalText();
    return AopConstants.JOIN_POINT.equals(typeText) || AopConstants.PROCEEDING_JOIN_POINT.equals(typeText) || AopConstants.JOIN_POINT_STATIC_PART.equals(typeText);
  }

  public AopAdvisedElementsSearcher getAdvisedElementsSearcher() {
    return myAdvisedElementsSearcher;
  }

  @Nullable
  public IntroductionManipulator getIntroductionManipulator() {
    return null;
  }
}
