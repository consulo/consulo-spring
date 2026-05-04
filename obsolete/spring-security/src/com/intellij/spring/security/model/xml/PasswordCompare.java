package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:password-compareElemType interface.
 */
public interface PasswordCompare extends SpringSecurityDomElement {

  /**
   * Returns the value of the password-attribute child.
   * <pre>
   * <h3>Attribute null:password-attribute documentation</h3>
   * The attribute in the directory which contains the user password.
   * Defaults to "userPassword".
   * </pre>
   *
   * @return the value of the password-attribute child.
   */
  @NotNull
  GenericAttributeValue<String> getPasswordAttribute();

  /**
   * Returns the value of the hash child.
   * <pre>
   * <h3>Attribute null:hash documentation</h3>
   * Defines the hashing algorithm used on user passwords. We recommend
   *           strongly against using MD4, as it is a very weak hashing algorithm.
   * </pre>
   *
   * @return the value of the hash child.
   */
  @NotNull
  GenericAttributeValue<Hash> getHash();

  /**
   * Returns the value of the password-encoder child.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:password-encoder documentation</h3>
   * element which defines a password encoding strategy. Used by an
   *                     authentication provider to convert submitted passwords to hashed versions, for
   *                     example.
   * </pre>
   *
   * @return the value of the password-encoder child.
   */
  @NotNull
  PasswordEncoder getPasswordEncoder();
}
