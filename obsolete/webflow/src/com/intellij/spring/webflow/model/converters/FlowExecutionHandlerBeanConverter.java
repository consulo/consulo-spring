package com.intellij.spring.webflow.model.converters;

import com.intellij.spring.webflow.constants.WebflowConstants;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NotNull;


public class FlowExecutionHandlerBeanConverter extends WebflowBeanResolveConverterForDefiniteClasses {

  @NotNull
  protected String[] getClassNames(final ConvertContext context) {
    return new String[]{  WebflowConstants.FLOW_EXECUTION_HANDLER_CLASSNAME};
  }
}

