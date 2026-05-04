package com.intellij.spring.security.model.xml;

import com.intellij.spring.model.xml.aop.RequiredBeanType;
import com.intellij.spring.model.xml.aop.TypedBeanResolveConverter;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.Convert;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:jdbc-user-serviceElemType interface.
 */
public interface JdbcUserService extends SpringSecurityDomElement {

  /**
   * Returns the value of the data-source-ref child.
   * <pre>
   * <h3>Attribute null:data-source-ref documentation</h3>
   * The bean ID of the DataSource which provides the required tables.
   * </pre>
   *
   * @return the value of the data-source-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.DATA_SOURCE)
  @Convert(TypedBeanResolveConverter.class)  
  GenericAttributeValue<SpringBeanPointer> getDataSourceRef();

  /**
   * Returns the value of the cache-ref child.
   * <pre>
   * <h3>Attribute null:cache-ref documentation</h3>
   * Defines a reference to a cache for use with a UserDetailsService.
   * </pre>
   *
   * @return the value of the cache-ref child.
   */
  @NotNull
  @Convert(SpringBeanResolveConverter.class)  
  GenericAttributeValue<SpringBeanPointer> getCacheRef();

  /**
   * Returns the value of the users-by-username-query child.
   * <pre>
   * <h3>Attribute null:users-by-username-query documentation</h3>
   * An SQL statement to query a username, password, and enabled status given a username
   * </pre>
   *
   * @return the value of the users-by-username-query child.
   */
  @NotNull
  GenericAttributeValue<String> getUsersByUsernameQuery();

  /**
   * Returns the value of the authorities-by-username-query child.
   * <pre>
   * <h3>Attribute null:authorities-by-username-query documentation</h3>
   * An SQL statement to query for a user's granted authorities given a username.
   * </pre>
   *
   * @return the value of the authorities-by-username-query child.
   */
  @NotNull
  GenericAttributeValue<String> getAuthoritiesByUsernameQuery();

  /**
   * Returns the value of the group-authorities-by-username-query child.
   * <pre>
   * <h3>Attribute null:group-authorities-by-username-query documentation</h3>
   * An SQL statement to query user's group authorities given a username.
   * </pre>
   *
   * @return the value of the group-authorities-by-username-query child.
   */
  @NotNull
  GenericAttributeValue<String> getGroupAuthoritiesByUsernameQuery();

  /**
   * Returns the value of the role-prefix child.
   * <pre>
   * <h3>Attribute null:role-prefix documentation</h3>
   * A non-empty string prefix that will be added to role strings loaded from
   * persistent storage (e.g. "ROLE_"). Use the value "none" for no prefix in cases where the
   * default is non-empty.
   * </pre>
   *
   * @return the value of the role-prefix child.
   */
  @NotNull
  GenericAttributeValue<String> getRolePrefix();
}
