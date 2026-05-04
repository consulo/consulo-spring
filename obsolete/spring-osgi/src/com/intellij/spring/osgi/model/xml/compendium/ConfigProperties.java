package com.intellij.spring.osgi.model.xml.compendium;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface ConfigProperties extends SpringOsgiCompendiumDomElement {

  /**
   * Returns the value of the persistent-id child.
   * <pre>
   * <h3>Attribute null:persistent-id documentation</h3>
   *         			The persistent id under which the properties to be exported are registered.
   * <p/>
   * </pre>
   *
   * @return the value of the persistent-id child.
   */
  @NotNull
  @Required
  GenericAttributeValue<String> getPersistentId();
}
