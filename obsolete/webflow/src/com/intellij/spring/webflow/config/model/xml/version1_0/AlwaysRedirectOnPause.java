package com.intellij.spring.webflow.config.model.xml.version1_0;

import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface AlwaysRedirectOnPause extends WebflowConfigDomElement {

  /**
   * Returns the value of the value child.
   * <pre>
   * <h3>Attribute null:value documentation</h3>
   * true = always redirect on pause; false = do not, only redirect when explicitly instructed by the flow definition.
   * </pre>
   *
   * @return the value of the value child.
   */
  @NotNull
  @Required
  GenericAttributeValue<Boolean> getValue();
}
