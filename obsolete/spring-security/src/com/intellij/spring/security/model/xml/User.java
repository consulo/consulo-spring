package com.intellij.spring.security.model.xml;

import com.intellij.spring.security.model.xml.converters.SpringSecurityRolesConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/security:userElemType interface.
 */
public interface User extends SpringSecurityDomElement {

  /**
   * Returns the value of the name child.
   * <pre>
   * <h3>Attribute null:name documentation</h3>
   * The username assigned to the user.
   * </pre>
   *
   * @return the value of the name child.
   */
  @NotNull
  GenericAttributeValue<String> getName();


  /**
   * Returns the value of the password child.
   * <pre>
   * <h3>Attribute null:password documentation</h3>
   * The password assigned to the user. This may be hashed if the corresponding
   * authentication provider supports hashing (remember to set the "hash" attribute of the
   * "user-service" element).
   * </pre>
   *
   * @return the value of the password child.
   */
  @NotNull
  GenericAttributeValue<String> getPassword();

  /**
   * Returns the value of the authorities child.
   * <pre>
   * <h3>Attribute null:authorities documentation</h3>
   * One of more authorities granted to the user. Separate authorities with a
   *           comma (but no space). For example, "ROLE_USER,ROLE_ADMINISTRATOR"
   * </pre>
   *
   * @return the value of the authorities child.
   */
  @NotNull
  @Convert(value = SpringSecurityRolesConverter.class)
  GenericAttributeValue<List<String>> getAuthorities();

  /**
   * Returns the value of the locked child.
   * <pre>
   * <h3>Attribute null:locked documentation</h3>
   * Can be set to "true" to mark an account as locked and  unusable.
   * </pre>
   *
   * @return the value of the locked child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getLocked();

  /**
   * Returns the value of the disabled child.
   * <pre>
   * <h3>Attribute null:disabled documentation</h3>
   * Can be set to "true" to mark an account as disabled and unusable.
   * </pre>
   *
   * @return the value of the disabled child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getDisabled();


}
