/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.aop;

import com.intellij.spring.impl.ide.constants.SpringConstants;
import com.intellij.spring.impl.ide.model.xml.SpringModelElement;
import com.intellij.spring.impl.ide.model.xml.aop.AopConfig;
import consulo.annotation.component.ExtensionImpl;
import consulo.xml.language.XmlName;
import consulo.xml.dom.reflect.DomExtender;
import consulo.xml.dom.reflect.DomExtensionsRegistrar;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
@ExtensionImpl
public class SpringAopDomExtender extends DomExtender<SpringModelElement> {
  @Nonnull
  @Override
  public Class<SpringModelElement> getElementClass() {
    return SpringModelElement.class;
  }

  public void registerExtensions(@Nonnull SpringModelElement element, @Nonnull DomExtensionsRegistrar registrar) {
    registrar.registerCollectionChildrenExtension(new XmlName("config", SpringConstants.AOP_NAMESPACE_KEY), AopConfig.class);
  }

}