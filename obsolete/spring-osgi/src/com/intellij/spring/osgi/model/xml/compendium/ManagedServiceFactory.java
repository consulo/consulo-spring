package com.intellij.spring.osgi.model.xml.compendium;

import com.intellij.spring.model.xml.beans.Identified;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface ManagedServiceFactory extends SpringOsgiCompendiumDomElement, Identified, SpringBean {

  @NotNull
  GenericAttributeValue<Boolean> getPrimary();

  @NotNull
  @Required
  GenericAttributeValue<String> getFactoryPid();

  @NotNull
  GenericAttributeValue<UpdateStrategy> getUpdateStrategy();

  @NotNull
  GenericAttributeValue<String> getUpdateMethod();
}
