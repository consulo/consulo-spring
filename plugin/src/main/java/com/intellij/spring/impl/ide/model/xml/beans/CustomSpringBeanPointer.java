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
  public CustomBean getSpringBean() {
    return ((CustomBeanWrapper)myBasePointer.getSpringBean()).getCustomBeans().get(myIndex);
  }

  public boolean isValid() {
    if (!myBasePointer.isValid()) return false;

    DomSpringBean baseBean = myBasePointer.getSpringBean();
    if (!(baseBean instanceof CustomBeanWrapper)) return false;

    List<CustomBean> beans = ((CustomBeanWrapper)baseBean).getCustomBeans();
    if (beans.size() <= myIndex) return false;

    return true;
  }

  public PsiManager getPsiManager() {
    return myBasePointer.getPsiManager();
  }

  public static CustomSpringBeanPointer createCustomSpringBeanPointer(CustomBean bean) {
    CustomBeanWrapper wrapper = bean.getWrapper();
    int index = wrapper.getCustomBeans().indexOf(bean);
    assert index >= 0;
    return new CustomSpringBeanPointer(wrapper, bean, index);
  }

  public boolean isAbstract() {
    return false;
  }

  public SpringBeanPointer getParentPointer() {
    return null;
  }

  public PsiElement getPsiElement() {
    return getSpringBean().getIdentifyingPsiElement();
  }

  public SpringBeanPointer derive(@Nonnull String name) {
    return Comparing.equal(name, getName()) ? this : new DerivedSpringBeanPointer(this, name);
  }

  public PsiClass getBeanClass() {
    return getSpringBean().getBeanClass();
  }

  public PsiFile getContainingFile() {
    return myBasePointer.getContainingFile();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CustomSpringBeanPointer)) return false;
    if (!super.equals(o)) return false;

    CustomSpringBeanPointer that = (CustomSpringBeanPointer)o;

    if (myIndex != that.myIndex) return false;
    if (myBasePointer != null ? !myBasePointer.equals(that.myBasePointer) : that.myBasePointer != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (myBasePointer != null ? myBasePointer.hashCode() : 0);
    result = 31 * result + myIndex;
    return result;
  }
}