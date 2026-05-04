package com.intellij.spring.webflow.config.model.xml.version2_0;

import com.intellij.psi.PsiFile;
import com.intellij.spring.model.converters.SpringResourceConverter;
import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.NameValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface FlowLocation extends WebflowConfigDomElement {

  /**
   * The id assign the flow definition in this registry.  Optional.
   * Specify when you wish to provide a custom flow definition identifier.
   * When specified, the id must be unique among all flows in this registry.
   */
  @NotNull
  @NameValue    
  GenericAttributeValue<String> getId();

  @NotNull
  @Required
  @Convert(SpringResourceConverter.class)
  GenericAttributeValue<PsiFile> getPath();

  @NotNull
  FlowDefinitionAttributes getFlowDefinitionAttributes();
}
