package com.intellij.spring.webflow.model.converters;

import com.intellij.spring.webflow.constants.WebflowConstants;
import com.intellij.spring.webflow.util.WebflowUtil;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.Nullable;

/**
 * User: plt
 */
public class ActionBeanConverter extends WebflowBeanResolveConverterForDefiniteClasses {

  @Nullable
  protected String[] getClassNames(final ConvertContext context) {
    return WebflowUtil.isAction(context) ? new String[]{WebflowConstants.ACTION_BEAN_CLASSNAME} : null;
  }
}
