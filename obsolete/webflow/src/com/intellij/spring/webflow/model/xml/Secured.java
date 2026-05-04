package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * Secures this flow definition element.
 * The current user invoking this element must have the required security attributes otherwise access to the element will be denied.
 * <br>
 * Note: This element configures a meta-attribute.
 * For the attribute to be enforced, the flow execution must be observed by a SecurityFlowExecutionListener.
 */
@ModelVersion(WebflowVersion.Webflow_2_0)
public interface Secured extends WebflowDomElement {

  @NotNull
  @Required
  GenericAttributeValue<String> getAttributes();

  @NotNull
  GenericAttributeValue<Match> getMatch();
}
