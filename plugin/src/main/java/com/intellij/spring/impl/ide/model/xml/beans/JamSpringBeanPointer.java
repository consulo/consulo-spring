/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.xml.beans;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.model.jam.JamPsiMemberSpringBean;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.ui.image.Image;
import consulo.util.lang.Comparing;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public final class JamSpringBeanPointer extends SpringBaseBeanPointer {
  @Nonnull
  private final JamPsiMemberSpringBean mySpringBean;

  protected JamSpringBeanPointer(@Nonnull JamPsiMemberSpringBean springBean) {
    super(springBean.getBeanName());
    mySpringBean = springBean;
  }

  @Nonnull
  @Override
  public JamPsiMemberSpringBean getSpringBean() {
    return mySpringBean;
  }

  @Override
  public boolean isAbstract() {
    return false;
  }

  @Nullable
  @Override
  public SpringBeanPointer getParentPointer() {
    return null;
  }

  @Nullable
  @Override
  public PsiElement getPsiElement() {
    JamPsiMemberSpringBean springBean = getSpringBean();

    return springBean.getIdentifyingPsiElement();
  }

  @Override
  public SpringBeanPointer derive(@Nonnull String name) {
    return Comparing.equal(name, getName()) ? this : new DerivedSpringBeanPointer(getBasePointer(), name);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) return true;
    if (!(o instanceof JamSpringBeanPointer)) return false;
    if (!super.equals(o)) return false;

    JamSpringBeanPointer that = (JamSpringBeanPointer)o;

    return mySpringBean.equals(that.mySpringBean);
  }

  @Override
  public int hashCode() {
    return 31 * super.hashCode() + mySpringBean.hashCode();
  }

  @Nullable
  @Override
  public PsiClass getBeanClass() {
    return getSpringBean().getBeanClass();
  }

  @Override
  public PsiManager getPsiManager() {
    return getSpringBean().getPsiManager();
  }

  @Override
  public PsiFile getContainingFile() {
    return getSpringBean().getContainingFile();
  }

  @Override
  public Image getBeanIcon() {
    return SpringImplIconGroup.springjavabean();
  }

  @Override
  public boolean isValid() {
    return getSpringBean().isValid();
  }
}