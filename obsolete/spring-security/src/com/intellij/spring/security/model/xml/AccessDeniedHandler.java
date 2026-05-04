package com.intellij.spring.security.model.xml;

import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:access-denied-handlerElemType interface.
 */
public interface AccessDeniedHandler extends SpringSecurityDomElement {

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

  /**
   * Returns the value of the error-page child.
   * <pre>
   * <h3>Attribute null:error-page documentation</h3>
   * The access denied page that an authenticated user will be redirected to if
   * they request a page which they don't have the authority to access.
   * </pre>
   *
   * @return the value of the error-page child.
   */
  @NotNull
  GenericAttributeValue<String> getErrorPage();
}
