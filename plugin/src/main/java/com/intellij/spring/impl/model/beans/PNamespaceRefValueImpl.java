/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.xml.beans.PNamespaceRefValue;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.xml.dom.GenericDomValue;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

/**
 * @author peter
 */
public abstract class PNamespaceRefValueImpl implements PNamespaceRefValue {

  @Nonnull
  public List<? extends PsiType> getRequiredTypes() {
    return PNamespaceValueImpl.getPropertyType(this, getPropertyName());
  }

  @Nonnull
  @NonNls
  public String getPropertyName() {
    String name = getXmlElementName();
    return name.substring(0, name.length() - "-ref".length());
  }

  @Nullable
  public PsiType[] getTypesByValue() {
    return null;
  }

  @Nonnull
  public GenericDomValue<SpringBeanPointer> getRefElement() {
    return this;
  }

  @Nonnull
  public GenericDomValue<?> getValueElement() {
    return getParent().getGenericInfo().getAttributeChildDescription(getPropertyName()).getDomAttributeValue(getParent());
  }
}