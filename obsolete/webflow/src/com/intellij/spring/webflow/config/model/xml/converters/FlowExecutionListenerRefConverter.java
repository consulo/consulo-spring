package com.intellij.spring.webflow.config.model.xml.converters;

import com.intellij.spring.webflow.constants.WebflowConstants;
import com.intellij.spring.webflow.model.converters.WebflowBeanResolveConverterForDefiniteClasses;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NotNull;

public class FlowExecutionListenerRefConverter extends WebflowBeanResolveConverterForDefiniteClasses {

   @NotNull
  protected String[] getClassNames(final ConvertContext context) {
   return new String[]{  WebflowConstants.EXECUTION_LISTENER_CLASS_NAME};
  }
}
