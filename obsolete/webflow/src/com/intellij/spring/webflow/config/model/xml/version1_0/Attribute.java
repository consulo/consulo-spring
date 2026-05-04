package com.intellij.spring.webflow.config.model.xml.version1_0;

import com.intellij.psi.PsiType;
import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/webflow-config:attributeType interface.
 */
public interface Attribute extends WebflowConfigDomElement {

  /**
   * Returns the value of the name child.
   * <pre>
   * <h3>Attribute null:name documentation</h3>
   * The name of the attribute.
   * </pre>
   *
   * @return the value of the name child.
   */
  @NotNull
  @Required
  GenericAttributeValue<String> getName();


  /**
   * Returns the value of the type child.
   * <pre>
   * <h3>Attribute null:type documentation</h3>
   * The attribute's type, used to perform a from-string type conversion if specified.
   * </pre>
   *
   * @return the value of the type child.
   */
  @NotNull
  GenericAttributeValue<PsiType> getType();


  /**
   * Returns the value of the value child.
   * <pre>
   * <h3>Attribute null:value documentation</h3>
   * The attribute value, subject to type conversion if the 'type' attribute is defined.
   * </pre>
   *
   * @return the value of the value child.
   */
  @NotNull
  @Required
  GenericAttributeValue<String> getValue();
}
