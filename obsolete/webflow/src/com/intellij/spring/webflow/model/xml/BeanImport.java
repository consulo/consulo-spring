package com.intellij.spring.webflow.model.xml;

import org.jetbrains.annotations.NotNull;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Convert;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.spring.webflow.model.converters.WebflowBeanResourceConverter;
import com.intellij.psi.xml.XmlFile;

@ModelVersion(WebflowVersion.Webflow_2_0)
public interface BeanImport extends WebflowDomElement {

  /**
   * Returns the value of the resource child.
   * <pre>
   * <h3>Attribute null:resource documentation</h3>
   * The relative resource path to the bean definition file to import.  Imported bean definitions
   * are then referenceable by this flow.  The resource path is relative to this document.
   * <br>
   * For example:
   * <pre>
   *     &lt;bean-import resource="orderitem-flow-beans.xml"/&gt;
   * </pre>
   * ... would look for 'orderitem-flow-beans.xml' in the same directory as this document.
   * </pre>
   *
   * @return the value of the resource child.
   */
  @NotNull
  @Required
  @Convert(WebflowBeanResourceConverter.class)
  GenericAttributeValue<XmlFile> getResource();
}
