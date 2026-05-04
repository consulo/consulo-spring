package com.intellij.spring.webflow.config.model.xml.version2_0;

import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface FlowExecutionAttributes extends WebflowConfigDomElement {

  @NotNull
  AlwaysRedirectOnPause getAlwaysRedirectOnPause();

  @NotNull
  List<Attribute> getAttributes();
}
