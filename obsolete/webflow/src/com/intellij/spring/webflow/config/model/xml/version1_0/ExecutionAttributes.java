package com.intellij.spring.webflow.config.model.xml.version1_0;

import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/webflow-config:execution-attributesType interface.
 */
public interface ExecutionAttributes extends WebflowConfigDomElement {

  /**
   * Returns the value of the alwaysRedirectOnPause child.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/webflow-config:alwaysRedirectOnPause documentation</h3>
   * Sets the 'alwaysRedirectOnPause' execution attribute value.  'alwaysRedirectOnPause' allows
   * control over whether each time a flow execution pauses a browser redirect is performed.  If
   * not specified the default value is 'true' unless explicitly set otherwise.
   * </pre>
   *
   * @return the value of the alwaysRedirectOnPause child.
   */
  @NotNull
  AlwaysRedirectOnPause getAlwaysRedirectOnPause();


  /**
   * Returns the list of attribute children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/webflow-config:attribute documentation</h3>
   * A single attribute describing an element.  Attributes have string keys and object values.
   * An attribute's type may be specified using the 'type' attribute.
   * </pre>
   *
   * @return the list of attribute children.
   */
  @NotNull
  List<Attribute> getAttributes();

  /**
   * Adds new child to the list of attribute children.
   *
   * @return created child
   */
  Attribute addAttribute();


}
