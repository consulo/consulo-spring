package com.intellij.spring.security.model.xml;

import com.intellij.spring.model.xml.aop.RequiredBeanType;
import com.intellij.spring.model.xml.aop.TypedBeanResolveConverter;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.spring.security.model.converters.SpringSecurityResourceConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:openid-loginElemType interface.
 */
public interface OpenidLogin extends SpringSecurityDomElement {

  /**
   * Returns the value of the login-processing-url child.
   * <pre>
   * <h3>Attribute null:login-processing-url documentation</h3>
   * The URL that the login form is posted to. If unspecified, it defaults to
   *           /j_spring_security_check.
   * </pre>
   *
   * @return the value of the login-processing-url child.
   */
  @NotNull
  @Referencing(value = SpringSecurityResourceConverter.class, soft = true)
  GenericAttributeValue<String> getLoginProcessingUrl();

  /**
   * Returns the value of the default-target-url child.
   * <pre>
   * <h3>Attribute null:default-target-url documentation</h3>
   * The URL that will be redirected to after successful authentication, if the
   *           user's previous action could not be resumed. This generally happens if the user visits a
   *           login page without having first requested a secured operation that triggers
   *           authentication. If unspecified, defaults to the root of the
   *           application.
   * </pre>
   *
   * @return the value of the default-target-url child.
   */
  @NotNull
  @Referencing(value = SpringSecurityResourceConverter.class, soft = true)
  GenericAttributeValue<String> getDefaultTargetUrl();

  /**
   * Returns the value of the always-use-default-target child.
   * <pre>
   * <h3>Attribute null:always-use-default-target documentation</h3>
   * Whether the user should always be redirected to the default-target-url
   *           after login.
   * </pre>
   *
   * @return the value of the always-use-default-target child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getAlwaysUseDefaultTarget();

  /**
   * Returns the value of the login-page child.
   * <pre>
   * <h3>Attribute null:login-page documentation</h3>
   * The URL for the login page. If no login URL is specified, Spring Security
   *           will automatically create a login URL at /spring_security_login and a corresponding filter
   *           to render that login URL when requested.
   * </pre>
   *
   * @return the value of the login-page child.
   */
  @NotNull
  @Referencing(value = SpringSecurityResourceConverter.class, soft = true)
  GenericAttributeValue<String> getLoginPage();

  /**
   * Returns the value of the authentication-failure-url child.
   * <pre>
   * <h3>Attribute null:authentication-failure-url documentation</h3>
   * The URL for the login failure page. If no login failure URL is specified,
   *           Spring Security will automatically create a failure login URL at
   *           /spring_security_login?login_error and a corresponding filter to render that login failure
   *           URL when requested.
   * </pre>
   *
   * @return the value of the authentication-failure-url child.
   */
  @NotNull
  @Referencing(value = SpringSecurityResourceConverter.class, soft = true)
  GenericAttributeValue<String> getAuthenticationFailureUrl();

  /**
   * Returns the value of the authentication-success-handler-ref child.
   * <pre>
   * <h3>Attribute null:authentication-success-handler-ref documentation</h3>
   * Reference to an AuthenticationSuccessHandler bean which should be used to
   * handle a successful authentication request. Should not be used in combination with
   * default-target-url (or always-use-default-target-url) as the implementation should always
   * deal with navigation to the subsequent destination
   * </pre>
   *
   * @return the value of the authentication-success-handler-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.AUTHENTICATION_SUCCESS_HANDLER)
  @Convert(TypedBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getAuthenticationSuccessHandlerRef();

  /**
   * Returns the value of the authentication-failure-handler-ref child.
   * <pre>
   * <h3>Attribute null:authentication-failure-handler-ref documentation</h3>
   * Reference to an AuthenticationFailureHandler bean which should be used to
   * handle a failed authentication request. Should not be used in combination with
   * authentication-failure-url as the implementation should always deal with navigation to the
   * subsequent destination
   * </pre>
   *
   * @return the value of the authentication-failure-handler-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.AUTHENTICATION_FAILURE_HANDLER)
  @Convert(TypedBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getAuthenticationFailureHandlerRef();

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
}
