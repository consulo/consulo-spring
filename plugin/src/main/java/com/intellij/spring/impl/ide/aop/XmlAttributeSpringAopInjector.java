package com.intellij.spring.impl.ide.aop;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.xml.psi.xml.XmlAttribute;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 05/02/2023
 */
@ExtensionImpl
public class XmlAttributeSpringAopInjector extends SpringAopInjector {
  @Nonnull
  @Override
  public Class<? extends PsiElement> getElementClass() {
    return XmlAttribute.class;
  }
}
