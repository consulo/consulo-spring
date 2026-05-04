package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Convert;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:salt-sourceElemType interface.
 */
public interface SaltSource extends SpringSecurityDomElement {

  /**
   * Returns the value of the user-property child.
   * <pre>
   * <h3>Attribute null:user-property documentation</h3>
   * A property of the UserDetails object which will be
   * used as salt by a password encoder. Typically something like
   * "username" might be used.
   * </pre>
   *
   * @return the value of the user-property child.
   */
  @NotNull
  GenericAttributeValue<String> getUserProperty();

  /**
   * Returns the value of the system-wide child.
   * <pre>
   * <h3>Attribute null:system-wide documentation</h3>
   * A single value that will be used as the salt for a
   * password encoder.
   * </pre>
   *
   * @return the value of the system-wide child.
   */
  @NotNull
  GenericAttributeValue<String> getSystemWide();

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
