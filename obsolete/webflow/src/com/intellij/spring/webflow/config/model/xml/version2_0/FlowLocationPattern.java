package com.intellij.spring.webflow.config.model.xml.version2_0;

import com.intellij.spring.model.values.converters.ResourceValueConverter;
import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface FlowLocationPattern extends WebflowConfigDomElement {

  @NotNull
  @Required
  @Referencing(value = ResourceValueConverter.class)
  GenericAttributeValue<String> getValue();
}
