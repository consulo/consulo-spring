package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_1_0)
public interface EntryActions extends ActionsOwner, SetOwner, WebflowDomElement {

   /**
   * Returns the list of evaluate-action children.
   *
   * @return the list of evaluate-action children.
   */
  @NotNull
  List<EvaluateAction> getEvaluateActions();

  /**
   * Adds new child to the list of evaluate-action children.
   *
   * @return created child
   */
  EvaluateAction addEvaluateAction();
}
