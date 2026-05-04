package com.intellij.spring.webflow.model.xml;

import com.intellij.psi.PsiType;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

@ModelVersion(WebflowVersion.Webflow_2_0)
public interface Output extends WebflowDomElement {

  /**
   * The name of the output attribute.
   */
  @NotNull
  @Required
  GenericAttributeValue<String> getName();

  /**
   * The value of the output attribute.
   */
  @NotNull
  GenericAttributeValue<String> getValue();

  /**
   * The expected value type.  If the actual value type is not compatible with the expected type, a type conversion will be attempted.
   */
  @NotNull
  GenericAttributeValue<PsiType> getType();

  /**
   * Whether or not this output is required.
   * If marked required and the value evaluates to null an error will be reported.
   */
  @NotNull
  GenericAttributeValue<Boolean> getRequired();
}
