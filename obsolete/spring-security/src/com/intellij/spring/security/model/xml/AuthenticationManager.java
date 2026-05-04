package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.spring.security.model.ModelVersion;
import com.intellij.spring.security.model.SpringSecurityVersion;
import com.intellij.spring.model.xml.DomSpringBean;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:authentication-managerElemType interface.
 */
public interface AuthenticationManager extends SpringSecurityDomElement, DomSpringBean {
  /**
   * Returns the value of the session-controller-ref child.
   * <pre>
   * <h3>Attribute null:session-controller-ref documentation</h3>
   * Allows the session controller to be set on the internal AuthenticationManager. This should not be used with the &lt;concurrent-session-control element
   * </pre>
   *
   * @return the value of the session-controller-ref child.
   */
  @NotNull
  GenericAttributeValue<String> getSessionControllerRef();

  @NotNull
  AuthenticationProvider getAuthenticationProvider();

  @NotNull
  @ModelVersion(SpringSecurityVersion.SpringSecurity_2_0)
  GenericAttributeValue<String> getAlias();
}
