/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.xml.beans;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.SpringIcons;
import com.intellij.spring.impl.ide.model.jam.JamPsiMemberSpringBean;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
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
  public JamPsiMemberSpringBean getSpringBean() {
    return mySpringBean;
  }

  public boolean isAbstract() {
    return false;
  }

  @Nullable
  public SpringBeanPointer getParentPointer() {
    return null;
  }

  @Nullable
  public PsiElement getPsiElement() {
    JamPsiMemberSpringBean springBean = getSpringBean();

    return springBean.getIdentifyingPsiElement();
  }

  public SpringBeanPointer derive(@Nonnull String name) {
    return Comparing.equal(name, getName()) ? this : new DerivedSpringBeanPointer(getBasePointer(), name);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof JamSpringBeanPointer)) return false;
    if (!super.equals(o)) return false;

    JamSpringBeanPointer that = (JamSpringBeanPointer)o;

    if (!mySpringBean.equals(that.mySpringBean)) return false;

    return true;
  }

  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + mySpringBean.hashCode();
    return result;
  }

  @Nullable
  public PsiClass getBeanClass() {
    return getSpringBean().getBeanClass();
  }

  public PsiManager getPsiManager() {
    return getSpringBean().getPsiManager();
  }

  public PsiFile getContainingFile() {
    return getSpringBean().getContainingFile();
  }

  public Image getBeanIcon() {
    return SpringIcons.SPRING_JAVA_BEAN_ICON;
  }

  public boolean isValid() {
    return getSpringBean().isValid();
  }

}