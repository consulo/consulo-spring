/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.xml.beans;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.model.xml.CustomBean;
import com.intellij.spring.impl.ide.model.xml.CustomBeanWrapper;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.util.lang.Comparing;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * @author peter
 */
public class CustomSpringBeanPointer extends SpringBaseBeanPointer {
  private final DomSpringBeanPointer myBasePointer;
  private final int myIndex;

  private CustomSpringBeanPointer(@Nonnull CustomBeanWrapper wrapper, CustomBean bean, int index) {
    super(bean.getBeanName());
    myIndex = index;
    myBasePointer = DomSpringBeanPointer.createDomSpringBeanPointer(wrapper);
  }

  @Nonnull
  @Override
  public CustomBean getSpringBean() {
    return ((CustomBeanWrapper)myBasePointer.getSpringBean()).getCustomBeans().get(myIndex);
  }

  @Override
  public boolean isValid() {
    if (!myBasePointer.isValid()) return false;

    DomSpringBean baseBean = myBasePointer.getSpringBean();
    if (!(baseBean instanceof CustomBeanWrapper customBeanWrapper)) return false;

    List<CustomBean> beans = customBeanWrapper.getCustomBeans();
    return beans.size() > myIndex;
  }

  @Override
  public PsiManager getPsiManager() {
    return myBasePointer.getPsiManager();
  }

  public static CustomSpringBeanPointer createCustomSpringBeanPointer(CustomBean bean) {
    CustomBeanWrapper wrapper = bean.getWrapper();
    int index = wrapper.getCustomBeans().indexOf(bean);
    assert index >= 0;
    return new CustomSpringBeanPointer(wrapper, bean, index);
  }

  @Override
  public boolean isAbstract() {
    return false;
  }

  @Override
  public SpringBeanPointer getParentPointer() {
    return null;
  }

  @Override
  public PsiElement getPsiElement() {
    return getSpringBean().getIdentifyingPsiElement();
  }

  @Override
  public SpringBeanPointer derive(@Nonnull String name) {
    return Comparing.equal(name, getName()) ? this : new DerivedSpringBeanPointer(this, name);
  }

  @Override
  public PsiClass getBeanClass() {
    return getSpringBean().getBeanClass();
  }

  @Override
  public PsiFile getContainingFile() {
    return myBasePointer.getContainingFile();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CustomSpringBeanPointer)) return false;
    if (!super.equals(o)) return false;

    CustomSpringBeanPointer that = (CustomSpringBeanPointer)o;

    return myIndex == that.myIndex
      && Objects.equals(myBasePointer, that.myBasePointer);
  }

  @Override
  public int hashCode() {
    int result = 31 * super.hashCode() + Objects.hashCode(myBasePointer);
    return 31 * result + myIndex;
  }
}