package com.intellij.spring.security.model.xml;

import com.intellij.spring.security.model.converters.SpringSecurityResourceConverter;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:logoutElemType interface.
 */
public interface Logout extends SpringSecurityDomElement {

  /**
   * Returns the value of the logout-url child.
   * <pre>
   * <h3>Attribute null:logout-url documentation</h3>
   * Specifies the URL that will cause a logout. Spring Security will
   * initialize a filter that responds to this particular URL. Defaults to
   * /j_spring_security_logout if unspecified.
   * </pre>
   *
   * @return the value of the logout-url child.
   */
  @NotNull
  @Referencing(value = SpringSecurityResourceConverter.class, soft = true)
  GenericAttributeValue<String> getLogoutUrl();

  /**
   * Returns the value of the logout-success-url child.
   * <pre>
   * <h3>Attribute null:logout-success-url documentation</h3>
   * Specifies the URL to display once the user has logged out. If not
   * specified, defaults to /.
   * </pre>
   *
   * @return the value of the logout-success-url child.
   */
  @NotNull
  @Referencing(value = SpringSecurityResourceConverter.class, soft = true)
  GenericAttributeValue<String> getLogoutSuccessUrl();

  /**
   * Returns the value of the invalidate-session child.
   * <pre>
   * <h3>Attribute null:invalidate-session documentation</h3>
   * Specifies whether a logout also causes HttpSession invalidation, which is
   *           generally desirable. If unspecified, defaults to true.
   * </pre>
   *
   * @return the value of the invalidate-session child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getInvalidateSession();
}
