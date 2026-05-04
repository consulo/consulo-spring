package com.intellij.spring.osgi.model.xml;

import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.osgi.model.converters.SpringBeanComparatorConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

public interface BaseReferenceCollection extends SpringOsgiDomElement, BaseOsgiReference {

  @NotNull
  @Convert(SpringBeanComparatorConverter.class)
  GenericAttributeValue<SpringBeanPointer> getComparatorRef();

  @NotNull
  GenericAttributeValue<CollectionCardinality> getCardinality();

  @NotNull
  Comparator getComparator();
}
