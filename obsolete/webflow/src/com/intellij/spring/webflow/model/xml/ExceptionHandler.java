package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.webflow.model.converters.FlowExecutionHandlerBeanConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface ExceptionHandler extends WebflowDomElement {

  @NotNull
  @Required
  @Convert(value = FlowExecutionHandlerBeanConverter.class)
  GenericAttributeValue<SpringBeanPointer> getBean();
}
