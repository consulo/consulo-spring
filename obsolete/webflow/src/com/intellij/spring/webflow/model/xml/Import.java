package com.intellij.spring.webflow.model.xml;

import com.intellij.openapi.paths.PathReference;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

@ModelVersion(WebflowVersion.Webflow_1_0)
public interface Import extends WebflowDomElement {

  /**
   * Returns the value of the resource child.
   * <pre>
   * <h3>Attribute null:resource documentation</h3>
   * The relative resource path to a bean definition file to import.  Imported bean definitions
   * are referenceable by this flow and any of its inline flows.
   * The resource path is relative to this document.
   * <br>
   * For example:
   * <pre>
   *     &lt;import resource="orderitem-flow-beans.xml"/&gt;
   * </pre>
   * ... would look for 'orderitem-flow-beans.xml' in the same directory as this document.
   * </pre>
   *
   * @return the value of the resource child.
   */
  @NotNull
  @Required
  GenericAttributeValue<PathReference> getResource();
}
