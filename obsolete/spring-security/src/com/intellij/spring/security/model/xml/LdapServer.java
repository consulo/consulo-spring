package com.intellij.spring.security.model.xml;

import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.values.converters.ResourceValueConverter;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:ldap-serverElemType interface.
 */
public interface LdapServer extends DomSpringBean, SpringSecurityDomElement {

  /**
   * Returns the value of the url child.
   * <pre>
   * <h3>Attribute null:url documentation</h3>
   * Specifies a URL.
   * </pre>
   *
   * @return the value of the url child.
   */
  @NotNull
  GenericAttributeValue<String> getUrl();

  /**
   * Returns the value of the port child.
   * <pre>
   * <h3>Attribute null:port documentation</h3>
   * Specifies an IP port number. Used to configure an embedded LDAP server, for example.
   * </pre>
   *
   * @return the value of the port child.
   */
  @NotNull
  GenericAttributeValue<Integer> getPort();

  /**
   * Returns the value of the manager-dn child.
   * <pre>
   * <h3>Attribute null:manager-dn documentation</h3>
   * Username (DN) of the "manager" user identity which will be used to
   * authenticate to a (non-embedded) LDAP server. If omitted, anonymous access will be used.
   * </pre>
   *
   * @return the value of the manager-dn child.
   */
  @NotNull
  GenericAttributeValue<String> getManagerDn();

  /**
   * Returns the value of the manager-password child.
   * <pre>
   * <h3>Attribute null:manager-password documentation</h3>
   * The password for the manager DN.
   * </pre>
   *
   * @return the value of the manager-password child.
   */
  @NotNull
  GenericAttributeValue<String> getManagerPassword();

  /**
   * Returns the value of the ldif child.
   * <pre>
   * <h3>Attribute null:ldif documentation</h3>
   * Explicitly specifies an ldif file resource to load into an embedded LDAP server
   * </pre>
   *
   * @return the value of the ldif child.
   */
  @NotNull
  @Referencing(value = ResourceValueConverter.class, soft = true)  
  GenericAttributeValue<String> getLdif();

  /**
   * Returns the value of the root child.
   * <pre>
   * <h3>Attribute null:root documentation</h3>
   * Optional root suffix for the embedded LDAP server. Default is "dc=springframework,dc=org"
   * </pre>
   *
   * @return the value of the root child.
   */
  @NotNull
  GenericAttributeValue<String> getRoot();
}
