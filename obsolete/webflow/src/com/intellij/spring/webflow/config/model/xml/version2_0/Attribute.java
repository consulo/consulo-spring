package com.intellij.spring.webflow.config.model.xml.version2_0;

import com.intellij.psi.PsiType;
import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface Attribute extends WebflowConfigDomElement {

  @NotNull
  @Required
  GenericAttributeValue<String> getName();

  @NotNull
  GenericAttributeValue<PsiType> getType();

  @NotNull
  @Required
  GenericAttributeValue<String> getValue();
}
