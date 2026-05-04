package com.intellij.spring.osgi.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

public interface Reference extends SpringOsgiDomElement, BaseOsgiReference {

  @NotNull
  GenericAttributeValue<SingleReferenceCardinality> getCardinality();

  @NotNull
  GenericAttributeValue<Integer> getTimeout();

}
