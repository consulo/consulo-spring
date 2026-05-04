package com.intellij.spring.webflow.model.xml;

import com.intellij.psi.PsiType;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface Attribute extends WebflowDomElement {

  @NotNull
  @Required
  GenericAttributeValue<String> getName();

   /**
   * The target type of the attribute value; to facilitate from-string type conversion. This type
   * string may be an alias (e.g 'int') or a fully-qualified class (e.g. 'java.lang.Integer').
   */
  @NotNull
  GenericAttributeValue<PsiType> getType();


  /**
   * Returns the value of the value child.
   * <pre>
   * <h3>Attribute null:value documentation</h3>
   * The value of this attribute; a short-cut alternative to an explicit child 'value' element.
   * </pre>
   *
   * @return the value of the value child.
   */
  @NotNull
  @com.intellij.util.xml.Attribute("value")
  GenericAttributeValue<String> getValueAttr();


	/**
         * Returns the value of the value child.
         *
         * @return the value of the value child.
         */
	@NotNull
	GenericDomValue<String> getValue();


}
