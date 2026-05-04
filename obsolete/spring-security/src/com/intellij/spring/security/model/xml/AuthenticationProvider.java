package com.intellij.spring.security.model.xml;

import com.intellij.spring.model.xml.aop.RequiredBeanType;
import com.intellij.spring.model.xml.aop.TypedBeanResolveConverter;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/security:authentication-providerElemType interface.
 */
public interface AuthenticationProvider extends SpringSecurityDomElement {

  /**
   * Returns the value of the user-service-ref child.
   * <pre>
   * <h3>Attribute null:user-service-ref documentation</h3>
   * A reference to a user-service (or UserDetailsService bean) Id
   * </pre>
   *
   * @return the value of the user-service-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.USER_DETAILS_SERVICE)
  @Convert(TypedBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getUserServiceRef();

  @NotNull
  List<UserService> getUserServices();

  /**
   * Returns the list of password-encoder children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:password-encoder documentation</h3>
   * element which defines a password encoding strategy. Used by an
   * authentication provider to convert submitted passwords to hashed versions, for example.
   * </pre>
   *
   * @return the list of password-encoder children.
   */
  @NotNull
  List<PasswordEncoder> getPasswordEncoders();

  JdbcUserService getJdbcUserService();
}
