package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Convert;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:password-encoderElemType interface.
 */
public interface PasswordEncoder extends SpringSecurityDomElement {

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
   * Returns the value of the hash child.
   * <pre>
   * <h3>Attribute null:hash documentation</h3>
   * Defines the hashing algorithm used on user passwords. We recommend
   * strongly against using MD4, as it is a very weak hashing algorithm.
   * </pre>
   *
   * @return the value of the hash child.
   */
  @NotNull
  GenericAttributeValue<Hash> getHash();

  /**
   * Returns the value of the base64 child.
   * <pre>
   * <h3>Attribute null:base64 documentation</h3>
   * Whether a string should be base64 encoded
   * </pre>
   *
   * @return the value of the base64 child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getBase64();

  /**
   * Returns the value of the salt-source child.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:salt-source documentation</h3>
   * Password salting strategy. A system-wide constant or a property from the UserDetails object can be used.
   * </pre>
   *
   * @return the value of the salt-source child.
   */
  @NotNull
  SaltSource getSaltSource();
}
