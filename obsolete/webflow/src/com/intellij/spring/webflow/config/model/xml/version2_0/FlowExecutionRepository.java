package com.intellij.spring.webflow.config.model.xml.version2_0;

import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

public interface FlowExecutionRepository extends WebflowConfigDomElement {

  @NotNull
  GenericAttributeValue<Integer> getMaxExecutions();

  @NotNull
  GenericAttributeValue<Integer> getMaxExecutionSnapshots();
}
