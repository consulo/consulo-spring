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
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author peter
 */
public abstract class PNamespaceValueImpl implements PNamespaceValue {

  @Nonnull
  public List<? extends PsiType> getRequiredTypes() {
    return getPropertyType(this, getPropertyName());
  }

  @Nonnull
  static List<PsiType> getPropertyType(DomElement value, @Nonnull @NonNls String name) {
    SpringBean bean = (SpringBean)value.getParent();
    assert bean != null;
    PsiClass beanClass = bean.getBeanClass();
    if (beanClass != null) {
      List<PsiMethod> methods = PropertyUtil.getSetters(beanClass, name);
      return ContainerUtil.map2List(methods, psiMethod -> PropertyUtil.getPropertyType(psiMethod));
    }
    return Collections.emptyList();
  }

  @Nonnull
  @NonNls
  public String getPropertyName() {
    return getXmlElementName();
  }

  @Nullable
  public PsiType[] getTypesByValue() {
    return null;
  }

  @Nonnull
  public GenericDomValue<SpringBeanPointer> getRefElement() {
    return getParent().getGenericInfo().getAttributeChildDescription(getPropertyName() + "-ref").getDomAttributeValue(getParent());
  }

  @Nonnull
  public GenericDomValue<?> getValueElement() {
    return this;
  }
}
