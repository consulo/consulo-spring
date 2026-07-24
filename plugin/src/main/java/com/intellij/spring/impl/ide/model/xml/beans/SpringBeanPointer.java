/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.xml.beans;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.model.PsiElementPointer;
import com.intellij.spring.impl.ide.model.jam.JamPsiMemberSpringBean;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.CustomBean;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.ui.image.Image;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public abstract class SpringBeanPointer implements PsiElementPointer {

  @Nullable
  public abstract String getName();

  @Nonnull
  public abstract CommonSpringBean getSpringBean();

  public abstract boolean isAbstract();

  public boolean isReferenceTo(@Nullable CommonSpringBean springBean) {
    if (springBean == null) return false;
    PsiFile file = springBean.getContainingFile();
    return file != null && file.equals(getContainingFile()) && springBean.equals(getSpringBean());
  }

  @Nullable
  public abstract SpringBeanPointer getParentPointer();

  public abstract SpringBeanPointer derive(@Nonnull String name);

  @Nullable
  public abstract PsiClass getBeanClass();

  public abstract PsiManager getPsiManager();

  public abstract PsiClass[] getEffectiveBeanType();

  public abstract PsiFile getContainingFile();

  public abstract Image getBeanIcon();

  public abstract String[] getAliases();

  public static SpringBaseBeanPointer createSpringBeanPointer(@Nonnull CommonSpringBean springBean) {
    if (springBean instanceof CustomBean) {
      return CustomSpringBeanPointer.createCustomSpringBeanPointer((CustomBean)springBean);
    }
    if (springBean instanceof DomSpringBean) {
      return DomSpringBeanPointer.createDomSpringBeanPointer((DomSpringBean)springBean);
    }
    if (springBean instanceof JamPsiMemberSpringBean) {
      return new JamSpringBeanPointer((JamPsiMemberSpringBean)springBean);
    }
    throw new AssertionError("Unknown bean type: " + springBean);
  }

  public String toString() {
    return getName();
  }

  @Nonnull
  public abstract SpringBaseBeanPointer getBasePointer();

  public abstract boolean isValid();
}
