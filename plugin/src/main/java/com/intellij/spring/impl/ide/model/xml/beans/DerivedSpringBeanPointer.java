/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.impl.ide.model.xml.beans;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.ui.image.Image;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public class DerivedSpringBeanPointer extends SpringBeanPointer{
  private final String myName;
  private final SpringBaseBeanPointer myBasePointer;

  public DerivedSpringBeanPointer(@Nonnull SpringBaseBeanPointer basePointer, @Nonnull String name) {
    myBasePointer = basePointer;
    myName = name;
  }

  @Override
  public SpringBeanPointer derive(@Nonnull String name) {
    if (name.equals(myName)) return this;
    if (name.equals(myBasePointer.getName())) return myBasePointer;
    return new DerivedSpringBeanPointer(myBasePointer, name);
  }

  @Override
  public String[] getAliases() {
    return myBasePointer.getAliases();
  }

  @Nonnull
  @Override
  public SpringBaseBeanPointer getBasePointer() {
    return myBasePointer;
  }

  @Override
  public boolean isValid() {
    return myBasePointer.isValid();
  }

  @Nullable
  @Override
  public PsiClass getBeanClass() {
    return myBasePointer.getBeanClass();
  }

  @Override
  public Image getBeanIcon() {
    return myBasePointer.getBeanIcon();
  }

  @Override
  public PsiFile getContainingFile() {
    return myBasePointer.getContainingFile();
  }

  @Override
  public PsiClass[] getEffectiveBeanType() {
    return myBasePointer.getEffectiveBeanType();
  }

  @Nullable
  @Override
  public String getName() {
    return myName;
  }

  @Nullable
  @Override
  public SpringBeanPointer getParentPointer() {
    return myBasePointer.getParentPointer();
  }

  @Nullable
  @Override
  public PsiElement getPsiElement() {
    return myBasePointer.getPsiElement();
  }

  @Override
  public PsiManager getPsiManager() {
    return myBasePointer.getPsiManager();
  }

  @Nonnull
  @Override
  public CommonSpringBean getSpringBean() {
    return myBasePointer.getSpringBean();
  }

  @Override
  public boolean isAbstract() {
    return myBasePointer.isAbstract();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) return true;
    if (!(o instanceof DerivedSpringBeanPointer)) return false;

    DerivedSpringBeanPointer that = (DerivedSpringBeanPointer)o;

    return myBasePointer.equals(that.myBasePointer)
      && myName.equals(that.myName);
  }

  @Override
  public int hashCode() {
    return 31 * myName.hashCode() + myBasePointer.hashCode();
  }
}
