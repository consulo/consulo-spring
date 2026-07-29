/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.beans;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.util.PropertyUtil;
import com.intellij.spring.impl.ide.model.xml.beans.PNamespaceValue;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.util.collection.ContainerUtil;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.GenericDomValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author peter
 */
public abstract class PNamespaceValueImpl implements PNamespaceValue {
  @Nonnull
  @Override
  public List<? extends PsiType> getRequiredTypes() {
    return getPropertyType(this, getPropertyName());
  }

  @Nonnull
  static List<PsiType> getPropertyType(DomElement value, @Nonnull String name) {
    SpringBean bean = (SpringBean)value.getParent();
    assert bean != null;
    PsiClass beanClass = bean.getBeanClass();
    if (beanClass != null) {
      List<PsiMethod> methods = PropertyUtil.getSetters(beanClass, name);
      return ContainerUtil.map2List(methods, PropertyUtil::getPropertyType);
    }
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public String getPropertyName() {
    return getXmlElementName();
  }

  @Nullable
  @Override
  public PsiType[] getTypesByValue() {
    return null;
  }

  @Nonnull
  @Override
  public GenericDomValue<SpringBeanPointer> getRefElement() {
    return getParent().getGenericInfo().getAttributeChildDescription(getPropertyName() + "-ref").getDomAttributeValue(getParent());
  }

  @Nonnull
  @Override
  public GenericDomValue<?> getValueElement() {
    return this;
  }
}
