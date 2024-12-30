/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.aop;

import com.intellij.spring.impl.ide.constants.SpringConstants;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.tx.Advice;
import com.intellij.spring.impl.ide.model.xml.tx.AnnotationDriven;
import consulo.annotation.component.ExtensionImpl;
import consulo.xml.util.xml.XmlName;
import consulo.xml.util.xml.reflect.DomExtender;
import consulo.xml.util.xml.reflect.DomExtensionsRegistrar;
import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
@ExtensionImpl
public class SpringTxDomExtender extends DomExtender<Beans> {

  @Nonnull
  @Override
  public Class<Beans> getElementClass() {
    return Beans.class;
  }

  public void registerExtensions(@Nonnull final Beans element, @Nonnull final DomExtensionsRegistrar registrar) {
    registrar.registerCollectionChildrenExtension(new XmlName("advice", SpringConstants.TX_NAMESPACE_KEY), Advice.class);
    registrar.registerCollectionChildrenExtension(new XmlName("annotation-driven", SpringConstants.TX_NAMESPACE_KEY), AnnotationDriven.class);
  }
}