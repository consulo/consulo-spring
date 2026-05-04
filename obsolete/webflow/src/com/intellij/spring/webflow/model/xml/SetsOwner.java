package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_2_0)
public interface SetsOwner extends WebflowDomElement {
  /**
   * Sets an attribute value in a scope.
   */
  @NotNull
  List<Set> getSets();

  Set addSet();
}