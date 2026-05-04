package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.webflow.model.converters.FlowAttributeMapperBeanConverter;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

  @ModelVersion(WebflowVersion.Webflow_1_0)
public interface AttributeMapper extends WebflowDomElement {

  /**
   * Returns the value of the bean child.
   * <pre>
   * <h3>Attribute null:bean documentation</h3>
   * The identifier of a custom flow attribute mapper implementation exported in the
   * Spring bean factory. This is similar to the &lt;ref bean="myBean"/&gt; notation of the Spring beans DTD.
   * <br>
   * Use this as an alternative to the child input-mapper and output-mapper elements
   * when you need full control of attribute mapping behavior for this subflow state.
   * </pre>
   *
   * @return the value of the bean child.
   */
  @NotNull
  @Convert(FlowAttributeMapperBeanConverter.class)
  GenericAttributeValue<SpringBeanPointer> getBean();

  @NotNull
  InputMapper getInputMapper();

  @NotNull
  OutputMapper getOutputMapper();
}
