package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface GlobalTransitions extends TransitionOwner, WebflowDomElement {
  /**
   * A global transition defines a path through the flow that can be taken from all states.
   * A global transition may execute one or more actions before executing.
   * All transition actions must execute successfully for the transition itself to execute.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  @Required
  @SubTagList("transition")
  List<ViewTransition> getViewTransitions();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  ViewTransition addViewTransition();
}
