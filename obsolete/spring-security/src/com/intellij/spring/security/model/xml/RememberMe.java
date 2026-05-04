package com.intellij.spring.security.model.xml;

import com.intellij.spring.model.xml.aop.RequiredBeanType;
import com.intellij.spring.model.xml.aop.TypedBeanResolveConverter;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:remember-meElemType interface.
 */
public interface RememberMe extends SpringSecurityDomElement {

  /**
   * Returns the value of the key child.
   * <pre>
   * <h3>Attribute null:key documentation</h3>
   * The "key" used to identify cookies from a specific token-based remember-me
   * application. You should set this to a unique value for your application.
   * </pre>
   *
   * @return the value of the key child.
   */
  @NotNull
  GenericAttributeValue<String> getKey();

  /**
   * Returns the value of the token-repository-ref child.
   * <pre>
   * <h3>Attribute null:token-repository-ref documentation</h3>
   * Reference to a PersistentTokenRepository bean for use with the persistent
   * token remember-me implementation.
   * </pre>
   *
   * @return the value of the token-repository-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.PERSISTENT_TOKEN_REPOSITORY)
  @Convert(TypedBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getTokenRepositoryRef();

  /**
   * Returns the value of the data-source-ref child.
   * <pre>
   * <h3>Attribute null:data-source-ref documentation</h3>
   * A reference to a DataSource bean
   * </pre>
   *
   * @return the value of the data-source-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.DATA_SOURCE)
  @Convert(TypedBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getDataSourceRef();

  /**
   * Returns the value of the services-ref child.
   * <pre>
   * <h3>Attribute null:services-ref documentation</h3>
   * Allows a custom implementation of RememberMeServices to be used. Note that
   * this implementation should return RememberMeAuthenticationToken instances with the same
   * "key" value as specified in the remember-me element. Alternatively it should register its
   * own AuthenticationProvider.
   * </pre>
   *
   * @return the value of the services-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.REMEMBER_ME_SERVICES)
  @Convert(TypedBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getServicesRef();

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

  /**
   * Returns the value of the token-validity-seconds child.
   * <pre>
   * <h3>Attribute null:token-validity-seconds documentation</h3>
   * The period (in seconds) for which the remember-me cookie should be valid.
   * If set to a negative value
   * </pre>
   *
   * @return the value of the token-validity-seconds child.
   */
  @NotNull
  GenericAttributeValue<Integer> getTokenValiditySeconds();
}
