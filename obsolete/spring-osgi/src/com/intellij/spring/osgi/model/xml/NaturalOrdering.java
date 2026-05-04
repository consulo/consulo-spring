package com.intellij.spring.osgi.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface NaturalOrdering extends SpringOsgiDomElement {

  @NotNull
  @Required
  GenericAttributeValue<OrderingBasis> getBasis();
}
