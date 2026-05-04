package com.intellij.spring.webflow.model.xml;

import com.intellij.psi.PsiType;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Set extends WebflowDomElement {

  @NotNull
  //todo ??? @Referencing(value = ScopeExpressionResultReferenceConverter.class, soft = true)
  GenericAttributeValue<String> getName();

  @NotNull
  @Required
  GenericAttributeValue<String> getValue();


  /**
   * The expected value type. If the actual value type does not match the expected, a type conversion will be attempted.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  GenericAttributeValue<PsiType> getType();


  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  GenericAttributeValue<String> getAttribute();


  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  GenericAttributeValue<Scope> getScope();

  @NotNull
  List<Attribute> getAttributes();

  Attribute addAttribute();
}
