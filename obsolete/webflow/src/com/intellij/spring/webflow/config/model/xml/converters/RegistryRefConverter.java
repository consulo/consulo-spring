package com.intellij.spring.webflow.config.model.xml.converters;

import com.intellij.spring.webflow.config.model.xml.version1_0.Registry;
import com.intellij.spring.webflow.model.converters.WebflowBeanResolveConverterForDefiniteClasses;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
public class RegistryRefConverter extends WebflowBeanResolveConverterForDefiniteClasses {

  @NotNull
  protected String[] getClassNames(final ConvertContext context) {
    return new String[]{ Registry.FLOW_DEFINITION_REGISTRY_CLASS };
  }
}
