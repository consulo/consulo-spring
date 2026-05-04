package com.intellij.spring.webflow.config.model.xml.converters;

import com.intellij.spring.webflow.constants.WebflowConstants;
import com.intellij.spring.webflow.model.converters.WebflowBeanResolveConverterForDefiniteClasses;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NotNull;

public class ConversationManagerRefConverter extends WebflowBeanResolveConverterForDefiniteClasses {

  @NotNull
  protected String[] getClassNames(final ConvertContext context) {
    return new String[]{  WebflowConstants.CONVERSATION_MANAGER_CLASS_NAME};
  }
}
