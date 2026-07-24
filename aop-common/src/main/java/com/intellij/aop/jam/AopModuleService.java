/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopAspect;
import com.intellij.aop.AopModel;
import com.intellij.aop.AopPointcut;
import com.intellij.jam.JamService;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.util.NotNullLazyValue;
import consulo.disposer.Disposable;
import consulo.ide.ServiceManager;
import consulo.module.Module;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author peter
 */
@ServiceAPI(ComponentScope.MODULE)
@ServiceImpl
@Singleton
public class AopModuleService implements Disposable {
  private final NotNullLazyValue<AopModelImpl> myModel;
  private static final AopModel EMPTY_AOP_MODEL = new AopModel() {
    @Override
    public List<? extends AopAspect> getAspects() {
      return Collections.emptyList();
    }

    @Override
    public List<? extends AopPointcut> getPointcuts() {
      return Collections.emptyList();
    }
  };

  @Nonnull
  public static AopModuleService getService(@Nonnull Module module) {
    return ServiceManager.getService(module, AopModuleService.class);
  }

  @Nonnull
  public static AopModel getAopModel(@Nullable Module module) {
    return module == null ? EMPTY_AOP_MODEL : getService(module).getModel();
  }

  @Inject
  public AopModuleService(final @Nonnull Module module) {
    myModel = new NotNullLazyValue<>() {
      @Nonnull
      @Override
      protected AopModelImpl compute() {
        return new AopModelImpl(module);
      }
    };
  }

  @Nullable
  public static AopAspectImpl getAspect(@Nonnull PsiClass cls) {
    return JamService.getJamService(cls.getProject()).getJamElement(AopAspectImpl.class, cls);
  }

  @Nullable
  public static AopAdviceImpl getAdvice(@Nonnull PsiMethod method) {
    return JamService.getJamService(method.getProject()).getJamElement(AopAdviceImpl.class, method);
  }

  @Nullable
  public static AopPointcutImpl getPointcut(@Nonnull PsiMethod method) {
    return JamService.getJamService(method.getProject()).getJamElement(AopPointcutImpl.class, method);
  }

  @Nullable
  public static AopIntroductionImpl getIntroduction(@Nonnull PsiField field) {
    return JamService.getJamService(field.getProject()).getJamElement(AopIntroductionImpl.class, field);
  }

  @Nonnull
  public synchronized AopModelImpl getModel() {
    return myModel.getValue();
  }

  @Override
  public void dispose() {
  }
}
