package com.intellij.spring.webflow.config.model.xml.version2_0;

import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.spring.webflow.config.model.xml.version1_0.Listener;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface FlowExecutionListeners extends WebflowConfigDomElement {

  @NotNull
  @Required
  List<Listener> getListeners();
}
