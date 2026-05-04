package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_2_0)
public interface RendersOwner extends WebflowDomElement {

  /**
   * Requests that the next view render a fragment of content.
   * Multiple fragments may be specified using a comma delimiter.
   */
  @NotNull
  List<Render> getRenders();

  Render addRender();
}