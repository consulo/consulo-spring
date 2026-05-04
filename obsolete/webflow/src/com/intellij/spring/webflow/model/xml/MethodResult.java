package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

@ModelVersion(WebflowVersion.Webflow_1_0)
public interface MethodResult extends WebflowDomElement {

  @NotNull
  @Required
  GenericAttributeValue<String> getName();

  @NotNull
  GenericAttributeValue<Scope> getScope();
}
