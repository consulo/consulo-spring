/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.impl.ide.model.xml.beans;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.SpringIcons;
import com.intellij.spring.impl.ide.model.SpringUtils;
import consulo.application.util.NotNullLazyValue;
import consulo.ui.image.Image;
import consulo.util.lang.Comparing;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public abstract class SpringBaseBeanPointer extends SpringBeanPointer{
  private final String myName;
  private final NotNullLazyValue<String[]> myAliases = new NotNullLazyValue<String[]>() {
    @Nonnull
    protected String[] compute() {
      return getSpringBean().getAliases();
    }
  };
  private final NotNullLazyValue<PsiClass[]> myEffectiveType = new NotNullLazyValue<PsiClass[]>() {
    @Nonnull
    protected PsiClass[] compute() {
      return SpringUtils.getEffectiveBeanTypes(getSpringBean());
    }
  };

  protected SpringBaseBeanPointer(@Nullable String name) {
    myName = name;
  }

  @Nullable
  public String getName() {
    return myName;
  }

  public PsiClass[] getEffectiveBeanType() {
    return myEffectiveType.getValue();
  }

  public Image getBeanIcon() {
    return SpringIcons.SPRING_BEAN_ICON;
  }

  public String[] getAliases() {
    return myAliases.getValue();
  }

  @Nonnull
  public SpringBaseBeanPointer getBasePointer() {
    return this;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SpringBaseBeanPointer)) return false;

    SpringBaseBeanPointer that = (SpringBaseBeanPointer)o;
    return Comparing.equal(getPsiElement(), that.getPsiElement());
  }

  public int hashCode() {
    return (myName != null ? myName.hashCode() : 0);
  }

}
