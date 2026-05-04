package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.Convert;
import com.intellij.spring.webflow.model.converters.IdentifiedStateConverter;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/webflow:ifElemType interface.
 */
public interface If extends WebflowDomElement {

  /**
   * Returns the value of the test child.
   * <pre>
   * <h3>Attribute null:test documentation</h3>
   * The transition criteria expression to be tested. This should be a boolean
   * ${expression} that evaluates against this flow's request context.
   * <br>
   * For example:
   * <pre>
   * 	&lt;if test="${flowScope.sale.shipping} then="enterShippingDetails"/&gt;
   * 	&lt;if test="${lastEvent.id == 'search'} then="bindSearchParameters"/&gt;
   * </pre>
   * </pre>
   *
   * @return the value of the test child.
   */
  @NotNull
  @Required
  GenericAttributeValue<String> getTest();

  @NotNull
  @Required
  @Convert(IdentifiedStateConverter.class)
  GenericAttributeValue<Object> getThen();

  @NotNull
  @Convert(IdentifiedStateConverter.class)
  GenericAttributeValue<Object> getElse();
}
