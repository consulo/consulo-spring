package com.intellij.spring.osgi.model.xml.compendium;

import com.intellij.spring.model.xml.beans.Identified;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

public interface ManagedService extends SpringOsgiCompendiumDomElement, Identified, SpringBean {

  @NotNull
  GenericAttributeValue<Boolean> getPrimary();

  GenericAttributeValue<String> getPersistentId();

  @NotNull
  GenericAttributeValue<UpdateStrategy> getUpdateStrategy();

  @NotNull
  GenericAttributeValue<String> getUpdateMethod();

}
