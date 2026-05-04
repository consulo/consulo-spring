package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.*;
import com.intellij.spring.webflow.model.converters.IdentifiedStateConverter;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

@ModelVersion(WebflowVersion.Webflow_1_0)
public interface StartState extends WebflowDomElement {

  /**
   * Returns the value of the idref child.
   * <pre>
   * <h3>Attribute null:idref documentation</h3>
   * The identifier of the start state of this flow.  The start state is the point where flow execution begins.
   * </pre>
   *
   * @return the value of the idref child.
   */
  @NotNull
  @Required
  @Convert(IdentifiedStateConverter.class)
  GenericAttributeValue<Object> getIdref();
}
