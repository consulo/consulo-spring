package com.intellij.spring.impl.ide;

import com.intellij.spring.impl.ide.model.xml.beans.ListOrSet;
import consulo.annotation.component.ExtensionImpl;
import consulo.xml.dom.reflect.DomExtender;
import consulo.xml.dom.reflect.DomExtensionsRegistrar;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class ListOrSetExtender extends DomExtender<ListOrSet> {
  @Nonnull
  @Override
  public Class<ListOrSet> getElementClass() {
    return ListOrSet.class;
  }

  public void registerExtensions(@Nonnull ListOrSet element, @Nonnull DomExtensionsRegistrar registrar) {
    SpringDefaultDomExtender.registerDefaultBeanExtensions(registrar);
  }
}
