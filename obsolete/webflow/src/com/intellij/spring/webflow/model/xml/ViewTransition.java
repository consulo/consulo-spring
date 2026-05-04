package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

@ModelVersion(WebflowVersion.Webflow_2_0)
public interface ViewTransition extends Transition {
  /**
  * Indicates whether model binding should occur before this transition executes. Defaults to true.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  GenericAttributeValue<Boolean> getBind();

  /**
   * Sets the state history policy for this transition.  The default is 'preserve', which allows back-tracking to the current state after this transition executes.
   * 'discard' prevents back-tracking to the state, and 'invalidate' prevents back-tracking to the state as well as any previously entered view-state.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  GenericAttributeValue<History> getHistory();
}
