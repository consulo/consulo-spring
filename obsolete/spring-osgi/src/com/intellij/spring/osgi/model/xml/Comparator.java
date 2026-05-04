package com.intellij.spring.osgi.model.xml;

import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface Comparator extends SpringOsgiDomElement {

  @NotNull
  @Required
  NaturalOrdering getNatural();
}
