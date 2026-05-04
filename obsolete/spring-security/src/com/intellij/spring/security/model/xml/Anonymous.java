package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:anonymousElemType interface.
 */
public interface Anonymous extends SpringSecurityDomElement {

  /**
   * Returns the value of the key child.
   * <pre>
   * <h3>Attribute null:key documentation</h3>
   * The key shared between the provider and filter. This generally does not
   * need to be set. If unset, it will default to "doesNotMatter".
   * </pre>
   *
   * @return the value of the key child.
   */
  @NotNull
  GenericAttributeValue<String> getKey();

  /**
   * Returns the value of the username child.
   * <pre>
   * <h3>Attribute null:username documentation</h3>
   * The username that should be assigned to the anonymous request. This allows
   * the principal to be identified, which may be important for logging and auditing. if unset,
   * defaults to "anonymousUser".
   * </pre>
   *
   * @return the value of the username child.
   */
  @NotNull
  GenericAttributeValue<String> getUsername();

  /**
   * Returns the value of the granted-authority child.
   * <pre>
   * <h3>Attribute null:granted-authority documentation</h3>
   * The granted authority that should be assigned to the anonymous request.
   * Commonly this is used to assign the anonymous request particular roles, which can
   * subsequently be used in authorization decisions. If unset, defaults to
   * "ROLE_ANONYMOUS".
   * </pre>
   *
   * @return the value of the granted-authority child.
   */
  @NotNull
  GenericAttributeValue<String> getGrantedAuthority();

  /**
   * Returns the value of the enabled child.
   * <pre>
   * <h3>Attribute null:enabled documentation</h3>
   * With the default namespace setup, the anonymous "authentication" facility
   * is automatically enabled. You can disable it using this property.
   * </pre>
   *
   * @return the value of the enabled child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getEnabled();
}
