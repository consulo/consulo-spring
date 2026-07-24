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

  public SpringBeanPointer derive(@Nonnull String name) {
    if (name.equals(myName)) return this;
    if (name.equals(myBasePointer.getName())) return myBasePointer;
    return new DerivedSpringBeanPointer(myBasePointer, name);
  }

  public String[] getAliases() {
    return myBasePointer.getAliases();
  }

  @Nonnull
  public SpringBaseBeanPointer getBasePointer() {
    return myBasePointer;
  }

  public boolean isValid() {
    return myBasePointer.isValid();
  }

  @Nullable
  public PsiClass getBeanClass() {
    return myBasePointer.getBeanClass();
  }

  public Image getBeanIcon() {
    return myBasePointer.getBeanIcon();
  }

  public PsiFile getContainingFile() {
    return myBasePointer.getContainingFile();
  }

  public PsiClass[] getEffectiveBeanType() {
    return myBasePointer.getEffectiveBeanType();
  }

  @Nullable
  public String getName() {
    return myName;
  }

  @Nullable
  public SpringBeanPointer getParentPointer() {
    return myBasePointer.getParentPointer();
  }

  @Nullable
  public PsiElement getPsiElement() {
    return myBasePointer.getPsiElement();
  }

  public PsiManager getPsiManager() {
    return myBasePointer.getPsiManager();
  }

  @Nonnull
  public CommonSpringBean getSpringBean() {
    return myBasePointer.getSpringBean();
  }

  public boolean isAbstract() {
    return myBasePointer.isAbstract();
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DerivedSpringBeanPointer)) return false;

    DerivedSpringBeanPointer that = (DerivedSpringBeanPointer)o;

    if (!myBasePointer.equals(that.myBasePointer)) return false;
    if (!myName.equals(that.myName)) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = myName.hashCode();
    result = 31 * result + myBasePointer.hashCode();
    return result;
  }
}
