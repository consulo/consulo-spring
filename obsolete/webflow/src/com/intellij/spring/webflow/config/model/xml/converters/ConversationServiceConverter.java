package com.intellij.spring.webflow.config.model.xml.converters;

import com.intellij.spring.webflow.config.model.xml.version2_0.FlowBuilderServices;
import com.intellij.spring.webflow.model.converters.WebflowBeanResolveConverterForDefiniteClasses;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NotNull;

public class ConversationServiceConverter extends WebflowBeanResolveConverterForDefiniteClasses {

  @NotNull
  protected String[] getClassNames(final ConvertContext context) {
    return new String[]{FlowBuilderServices.CONVERSATION_SERVICE_CLASS};
  }
}