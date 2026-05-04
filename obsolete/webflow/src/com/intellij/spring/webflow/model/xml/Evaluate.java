package com.intellij.spring.webflow.model.xml;

import com.intellij.psi.PsiType;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_2_0)
public interface Evaluate extends WebflowDomElement {

  /**
   * The expression to evaluate.
   */
  @NotNull
  @Required
  GenericAttributeValue<String> getExpression();

   /**
   * Where to assign the expression evaluation result.  If not specified, the result will not be assigned.
   */
  @NotNull
  //@Referencing(value = ScopeExpressionResultReferenceConverter.class, soft = true)
  GenericAttributeValue<String> getResult();


  /**
   * The type of result expected to be returned from evaluating the expresion.
   * If specified and the result is not compatible with the expected type, a type conversion will be attempted.
   */
  @NotNull
  GenericAttributeValue<PsiType> getResultType();

  /**
   * An attribute for the action.
   */
  @NotNull
  List<Attribute> getAttributes();

  Attribute addAttribute();
}
