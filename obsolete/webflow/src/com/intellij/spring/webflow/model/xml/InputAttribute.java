package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

@ModelVersion(WebflowVersion.Webflow_1_0)
public interface InputAttribute extends WebflowDomElement {

  /**
   * Returns the value of the name child.
   * <pre>
   * <h3>Attribute null:name documentation</h3>
   * The name of the input attribute.
   * </pre>
   *
   * @return the value of the name child.
   */
  @NotNull
  @Required
  GenericAttributeValue<String> getName();

  @NotNull
  GenericAttributeValue<Scope> getScope();

  @NotNull
  GenericAttributeValue<Boolean> getRequired();
}
