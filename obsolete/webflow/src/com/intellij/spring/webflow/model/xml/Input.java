package com.intellij.spring.webflow.model.xml;

import com.intellij.psi.PsiType;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.NameValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

@ModelVersion(WebflowVersion.Webflow_2_0)
public interface Input extends WebflowDomElement {

  /**
   * The name of the input attribute.
   */
  @NotNull
  @Required
  @NameValue    
  GenericAttributeValue<String> getName();

  /**
   * The value of the input attribute.
   */
  @NotNull
  GenericAttributeValue<String> getValue();

  /**
   * The expected value type.  If the actual value type is not compatible with the expected type, a type conversion will be attempted.
   */
  @NotNull
  GenericAttributeValue<PsiType> getType();

  /**
   * Returns the value of the required child.
   * <pre>
   * <h3>Attribute null:required documentation</h3>
   * Whether or not this input is required.
   * If marked required and the value evaluates to null an error will be reported.
   * </pre>
   *
   * @return the value of the required child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getRequired();
}