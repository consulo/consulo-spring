package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

  @ModelVersion(WebflowVersion.Webflow_1_0)
public interface Argument extends WebflowDomElement {

  /**
   * Returns the value of the expression child.
   * <pre>
   * <h3>Attribute null:expression documentation</h3>
   * The value expression for this bean method argument.
   * </pre>
   *
   * @return the value of the expression child.
   */
  @NotNull
  @Required
  GenericAttributeValue<String> getExpression();


  /**
   * Returns the value of the parameter-type child.
   * <pre>
   * <h3>Attribute null:parameter-type documentation</h3>
   * The method parameter type.  Optional.  If specified and the argument value does not equal the
   * parameter type, a type conversion will be attempted.
   * </pre>
   *
   * @return the value of the parameter-type child.
         */
	@NotNull
	GenericAttributeValue<String> getParameterType();


}
