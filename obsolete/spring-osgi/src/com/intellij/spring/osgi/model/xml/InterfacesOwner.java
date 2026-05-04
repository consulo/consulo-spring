package com.intellij.spring.osgi.model.xml;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.ExtendClass;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

public interface InterfacesOwner extends SpringOsgiDomElement {

  @NotNull
  @ExtendClass(instantiatable = false, allowInterface = true, allowEnum = false, allowAbstract = true, canBeDecorator = false)
  GenericAttributeValue<PsiClass> getInterface();

  @NotNull
  Interfaces getInterfaces();
}