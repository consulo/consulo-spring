package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.NameValue;
import org.jetbrains.annotations.NotNull;

public interface Identified extends WebflowDomElement {

  @NotNull
  @NameValue(unique = true)
  GenericAttributeValue<String> getId();
}
