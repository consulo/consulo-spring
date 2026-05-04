package com.intellij.spring.webflow.config.model.xml.version2_0;

import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface FlowExecutionListener extends WebflowConfigDomElement {

  @NotNull
  @Required
  GenericAttributeValue<String> getRef();

  /**
   * The criteria that determines the flow definitions your listener should observe, delimited by commas or '*' for "all".
   * Example: 'flow1,flow2,flow3'.
   */
  @NotNull
  GenericAttributeValue<String> getCriteria();
}
