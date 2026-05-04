package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_1_0)
public interface ActionsOwner extends WebflowDomElement{

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  List<Action> getActions();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  Action addAction();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  List<BeanAction> getBeanActions();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  BeanAction addBeanAction();
}
