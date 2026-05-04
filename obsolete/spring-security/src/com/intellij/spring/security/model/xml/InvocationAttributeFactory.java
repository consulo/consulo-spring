package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.Convert;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:invocation-attribute-factoryElemType interface.
 */
public interface InvocationAttributeFactory extends SpringSecurityDomElement {

  /**
   * Returns the value of the ref child.
   * <pre>
   * <h3>Attribute null:ref documentation</h3>
   * Defines a reference to a Spring bean Id.
   * </pre>
   *
   * @return the value of the ref child.
   */
  @NotNull
  @Convert(SpringBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getRef();
}
