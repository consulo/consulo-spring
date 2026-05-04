package com.intellij.spring.security.model.xml;

import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.xml.aop.RequiredBeanType;
import com.intellij.spring.model.xml.aop.TypedBeanResolveConverter;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Convert;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:ldap-user-serviceElemType interface.
 */
public interface LdapUserService extends SpringSecurityDomElement {

  /**
   * Returns the value of the server-ref child.
   * <pre>
   * <h3>Attribute null:server-ref documentation</h3>
   * The optional server to use. If omitted, and a default LDAP server is
   * registered (using &lt;ldap-server&gt; with no Id), that server will be used.
   * </pre>
   *
   * @return the value of the server-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.LDAP_CONTEXT_SOURCE)
  @Convert(TypedBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getServerRef();

  /**
   * Returns the value of the user-search-filter child.
   * <pre>
   * <h3>Attribute null:user-search-filter documentation</h3>
   * The LDAP filter used to search for users (optional).
   * For example "(uid={0})". The substituted parameter is the user's login name.
   * </pre>
   *
   * @return the value of the user-search-filter child.
   */
  @NotNull
  GenericAttributeValue<String> getUserSearchFilter();

  /**
   * Returns the value of the user-search-base child.
   * <pre>
   * <h3>Attribute null:user-search-base documentation</h3>
   * Search base for user searches. Defaults to "". Only used with a
   *           'user-search-filter'.
   * </pre>
   *
   * @return the value of the user-search-base child.
   */
  @NotNull
  GenericAttributeValue<String> getUserSearchBase();

  /**
   * Returns the value of the group-search-filter child.
   * <pre>
   * <h3>Attribute null:group-search-filter documentation</h3>
   * Group search filter. Defaults to (uniqueMember={0}).
   * The substituted parameter is the DN of the user.
   * </pre>
   *
   * @return the value of the group-search-filter child.
   */
  @NotNull
  GenericAttributeValue<String> getGroupSearchFilter();

  /**
   * Returns the value of the group-search-base child.
   * <pre>
   * <h3>Attribute null:group-search-base documentation</h3>
   * Search base for group membership searches.
   * Defaults to "" (searching from the root).
   * </pre>
   *
   * @return the value of the group-search-base child.
   */
  @NotNull
  GenericAttributeValue<String> getGroupSearchBase();

  /**
   * Returns the value of the group-role-attribute child.
   * <pre>
   * <h3>Attribute null:group-role-attribute documentation</h3>
   * The LDAP attribute name which contains the role name which will be used
   * within Spring Security. Defaults to "cn".
   * </pre>
   *
   * @return the value of the group-role-attribute child.
   */
  @NotNull
  GenericAttributeValue<String> getGroupRoleAttribute();

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
   * Returns the value of the role-prefix child.
   * <pre>
   * <h3>Attribute null:role-prefix documentation</h3>
   * A non-empty string prefix that will be added to role strings loaded from
   * persistent storage (e.g. "ROLE_"). Use the value "none" for no prefix in cases where the
   *  default is non-empty.
   * </pre>
   *
   * @return the value of the role-prefix child.
   */
  @NotNull
  GenericAttributeValue<String> getRolePrefix();

  /**
   * Returns the value of the user-details-class child.
   * <pre>
   * <h3>Attribute null:user-details-class documentation</h3>
   * Allows the objectClass of the user entry to be specified. If set, the
   * framework will attempt to load standard attributes for the defined class into the returned
   * UserDetails object
   * </pre>
   *
   * @return the value of the user-details-class child.
   */
  @NotNull
  GenericAttributeValue<UserDetailsClass> getUserDetailsClass();
}
