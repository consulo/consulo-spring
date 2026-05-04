package com.intellij.spring.webflow.config.model.xml.version2_0;

import com.intellij.psi.PsiClass;
import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.ExtendClass;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface FlowBuilder extends WebflowConfigDomElement {

  @NotNull
  GenericAttributeValue<String> getId();

  @NotNull
  @Attribute("class")
  @Required
  @ExtendClass("org.springframework.webflow.engine.builder.FlowBuilder")
  GenericAttributeValue<PsiClass> getClazz();

  @NotNull
  FlowDefinitionAttributes getFlowDefinitionAttributes();
}
