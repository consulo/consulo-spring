 package com.intellij.spring.webflow.config.model.xml.version2_0;

import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface AlwaysRedirectOnPause extends WebflowConfigDomElement {

  @NotNull
  @Required
  GenericAttributeValue<Boolean> getValue();
}
