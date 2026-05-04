package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_2_0)
public interface EvaluatesOwner extends WebflowDomElement {

  /**
   * Returns the list of evaluate children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/webflow:evaluate documentation</h3>
   * Evaluates an expression against the flow request context.
   * </pre>
   *
   * @return the list of evaluate children.
   */
  @NotNull
  List<Evaluate> getEvaluates();

  /**
   * Adds new child to the list of evaluate children.
   *
   * @return created child
   */
  Evaluate addEvaluate();
}
