package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

@ModelVersion(WebflowVersion.Webflow_2_0_3)
public interface Binding extends WebflowDomElement {

  /**
   * The name of the model property to bind to.
   */
  @NotNull
  @Required
  GenericAttributeValue<String> getProperty();

  /**
   * The name of a custom converter to use to format the property for display in the UI.
   */
  @NotNull
  GenericAttributeValue<String> getConverter();

  /**
   * Indicates if this binding is required.  A required binding generates an error when setting a null or empty value.
   * A binding that is not required allows null and empty values to be bound.
   */
  @NotNull
  GenericAttributeValue<Boolean> getRequired();

}
