package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_1_0)
public interface StartActions extends ActionsOwner, SetOwner, WebflowDomElement {

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  List<EvaluateAction> getEvaluateActions();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  EvaluateAction addEvaluateAction();
}
