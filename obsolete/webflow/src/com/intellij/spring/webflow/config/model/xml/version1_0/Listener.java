package com.intellij.spring.webflow.config.model.xml.version1_0;

import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.spring.webflow.config.model.xml.converters.FlowExecutionListenerRefConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/webflow-config:listenerType interface.
 */
public interface Listener extends WebflowConfigDomElement {
  @NonNls String FLOW_LISTENER_CLASS = "org.springframework.webflow.execution.FlowExecutionListener";

  /**
   * Returns the value of the ref child.
   * <pre>
   * <h3>Attribute null:ref documentation</h3>
   * The idref to your flow execution listener.
   * </pre>
   *
   * @return the value of the ref child.
   */
  @NotNull
  @Required
  @Convert(FlowExecutionListenerRefConverter.class)
  GenericAttributeValue<SpringBeanPointer> getRef();

  /**
   * Returns the value of the criteria child.
   * <pre>
   * <h3>Attribute null:criteria documentation</h3>
   * The flow definitions your listener should apply to, delimited by commas or '*' for "all".
   * Example: 'flow1,flow2,flow3'.
   * </pre>
   *
   * @return the value of the criteria child.
   */
  @NotNull
  GenericAttributeValue<String> getCriteria();
}
