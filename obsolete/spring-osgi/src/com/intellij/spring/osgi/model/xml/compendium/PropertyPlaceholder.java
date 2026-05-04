package com.intellij.spring.osgi.model.xml.compendium;

import com.intellij.spring.model.xml.beans.Identified;
import com.intellij.spring.model.xml.beans.Props;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;


public interface PropertyPlaceholder extends SpringOsgiCompendiumDomElement, Identified {

  @NotNull
  @Required
  GenericAttributeValue<String> getPersistentId();

  @NotNull
  GenericAttributeValue<String> getPlaceholderPrefix();

  @NotNull
  GenericAttributeValue<String> getPlaceholderSuffix();

  @NotNull
  GenericAttributeValue<String> getDefaultsRef();

  @NotNull
  Props getDefaultProperties();
}
