package com.intellij.spring.security.model.xml;

import com.intellij.spring.model.xml.aop.RequiredBeanType;
import com.intellij.spring.model.xml.aop.TypedBeanResolveConverter;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Convert;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/security:httpElemType interface.
 */
public interface Http extends SpringSecurityDomElement {

  /**
   * Returns the value of the auto-config child.
   * <pre>
   * <h3>Attribute null:auto-config documentation</h3>
   * Automatically registers a login form, BASIC authentication, anonymous
   * authentication, logout services, remember-me and servlet-api-integration. If set to
   * "true", all of these capabilities are added (although you can still customize the
   * configuration of each by providing the respective element). If unspecified, defaults to
   * "false".
   * </pre>
   *
   * @return the value of the auto-config child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getAutoConfig();

  /**
   * Returns the value of the use-expressions child.
   * <pre>
   * <h3>Attribute null:use-expressions documentation</h3>
   * Enables the use of expressions in the 'access' attributes in
   *           &lt;intercept-url&gt; elements rather than the traditional list of configuration
   *           attributes. Defaults to 'false'. If enabled, each attribute should contain a single
   *           boolean expression. If the expression evaluates to 'true', access will be granted.
   * </pre>
   *
   * @return the value of the use-expressions child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getUseExpressions();


  /**
   * Returns the value of the create-session child.
   * <pre>
   * <h3>Attribute null:create-session documentation</h3>
   * Controls the eagerness with which an HTTP session is created. If not set,
   *           defaults to "ifRequired". Note that if a custom SecurityContextRepository is set using
   *           security-context-repository-ref, then the only value which can be set is "always".
   *           Otherwise the session creation behaviour will be determined by the repository bean
   *           implementation.
   * </pre>
   *
   * @return the value of the create-session child.
   */
  @NotNull
  GenericAttributeValue<CreateSession> getCreateSession();

  /**
   * Returns the value of the security-context-repository-ref child.
   * <pre>
   * <h3>Attribute null:security-context-repository-ref documentation</h3>
   * A reference to a SecurityContextRepository bean. This can be used to
   * customize how the SecurityContext is stored between requests.
   * </pre>
   *
   * @return the value of the security-context-repository-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.SECURITY_CONTEXT_REPOSITORY)
  @Convert(TypedBeanResolveConverter.class)  
  GenericAttributeValue<SpringBeanPointer> getSecurityContextRepositoryRef();

  /**
   * Returns the value of the path-type child.
   * <pre>
   * <h3>Attribute null:path-type documentation</h3>
   * Defines the type of pattern used to specify URL paths (either JDK
   * 1.4-compatible regular expressions, or Apache Ant expressions). Defaults to "ant" if unspecified.
   * </pre>
   *
   * @return the value of the path-type child.
   */
  @NotNull
  GenericAttributeValue<PathType> getPathType();

  /**
   * Returns the value of the lowercase-comparisons child.
   * <pre>
   * <h3>Attribute null:lowercase-comparisons documentation</h3>
   * Whether test URLs should be converted to lower case prior to comparing
   *           with defined path patterns. If unspecified, defaults to "true".
   * </pre>
   *
   * @return the value of the lowercase-comparisons child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getLowercaseComparisons();

  /**
   * Returns the value of the servlet-api-provision child.
   * <pre>
   * <h3>Attribute null:servlet-api-provision documentation</h3>
   * Provides versions of HttpServletRequest security methods such as
   *           isUserInRole() and getPrincipal() which are implemented by accessing the Spring
   *           SecurityContext. Defaults to "true".
   * </pre>
   *
   * @return the value of the servlet-api-provision child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getServletApiProvision();

  /**
   * Returns the value of the access-decision-manager-ref child.
   * <pre>
   * <h3>Attribute null:access-decision-manager-ref documentation</h3>
   * Optional attribute specifying the ID of the AccessDecisionManager
   * implementation which should be used for authorizing HTTP requests.
   * </pre>
   *
   * @return the value of the access-decision-manager-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.ACCESS_DECISION_MANAGER)
  @Convert(TypedBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getAccessDecisionManagerRef();

  /**
   * Returns the value of the realm child.
   * <pre>
   * <h3>Attribute null:realm documentation</h3>
   * Optional attribute specifying the realm name that will be used for all
   * authentication features that require a realm name (eg BASIC and Digest authentication). If
   * unspecified, defaults to "Spring Security Application".
   * </pre>
   *
   * @return the value of the realm child.
   */
  @NotNull
  GenericAttributeValue<String> getRealm();

  /**
   * Returns the value of the session-fixation-protection child.
   * <pre>
   * <h3>Attribute null:session-fixation-protection documentation</h3>
   * Indicates whether an existing session should be invalidated when a user
   * authenticates and a new session started. If set to "none" no change will be made.
   * "newSession" will create a new empty session. "migrateSession" will create a new session
   * and copy the session attributes to the new session. Defaults to
   * "migrateSession".
   * </pre>
   *
   * @return the value of the session-fixation-protection child.
   */
  @NotNull
  GenericAttributeValue<SessionFixationProtection> getSessionFixationProtection();

  /**
   * Returns the value of the entry-point-ref child.
   * <pre>
   * <h3>Attribute null:entry-point-ref documentation</h3>
   * Allows a customized AuthenticationEntryPoint to be used.
   * </pre>
   *
   * @return the value of the entry-point-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.AUTHENTICATION_ENTRY_POINT)
  @Convert(TypedBeanResolveConverter.class)
  GenericAttributeValue<String> getEntryPointRef();

  /**
   * Returns the value of the once-per-request child.
   * <pre>
   * <h3>Attribute null:once-per-request documentation</h3>
   * Corresponds to the observeOncePerRequest property of
   * FilterSecurityInterceptor. Defaults to "true"
   * </pre>
   *
   * @return the value of the once-per-request child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getOncePerRequest();


  /**
   * Returns the value of the access-denied-page child.
   * <pre>
   * <h3>Attribute null:access-denied-page documentation</h3>
   * Deprecated in favour of the access-denied-handler
   *           element.
   * </pre>
   *
   * @return the value of the access-denied-page child.
   */
  @NotNull
  GenericAttributeValue<String> getAccessDeniedPage();

  /**
   * Returns the value of the disable-url-rewriting child.
   * <pre>
   * <h3>Attribute null:disable-url-rewriting documentation</h3>
   * </pre>
   *
   * @return the value of the disable-url-rewriting child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getDisableUrlRewriting();

  /**
   * Returns the list of intercept-url children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:intercept-url documentation</h3>
   * Specifies the access attributes and/or filter list for a particular set of URLs.
   * </pre>
   *
   * @return the list of intercept-url children.
   */
  @NotNull
  List<InterceptUrl> getInterceptUrls();

  /**
   * Returns the list of access-denied-handler children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:access-denied-handler documentation</h3>
   * Defines the access-denied strategy that should be used. An access
   * denied page can be defined or a reference to an AccessDeniedHandler instance.
   * </pre>
   *
   * @return the list of access-denied-handler children.
   */
  @NotNull
  List<AccessDeniedHandler> getAccessDeniedHandlers();

  /**
   * Returns the list of form-login children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:form-login documentation</h3>
   * Sets up a form login configuration for authentication with a username and password
   * </pre>
   *
   * @return the list of form-login children.
   */
  @NotNull
  List<FormLogin> getFormLogins();

  /**
   * Returns the list of openid-login children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:openid-login documentation</h3>
   * Sets up form login for authentication with an Open ID
   *         identity
   * </pre>
   *
   * @return the list of openid-login children.
   */
  @NotNull
  List<OpenidLogin> getOpenidLogins();

  /**
   * Returns the list of x509 children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:x509 documentation</h3>
   * Adds support for X.509 client authentication.
   * </pre>
   *
   * @return the list of x509 children.
   */
  @NotNull
  List<X509> getX509s();

  /**
   * Returns the list of http-basic children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:http-basic documentation</h3>
   * Adds support for basic authentication (this is an element to permit
   * future expansion, such as supporting an "ignoreFailure" attribute)
   * </pre>
   *
   * @return the list of http-basic children.
   */
  @NotNull
  List<GenericDomValue<String>> getHttpBasics();

  /**
   * Returns the list of logout children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:logout documentation</h3>
   * Incorporates a logout processing filter. Most web applications require
   * a logout filter, although you may not require one if you write a controller to
   * provider similar logic.
   * </pre>
   *
   * @return the list of logout children.
   */
  @NotNull
  List<Logout> getLogouts();

  /**
   * Returns the list of concurrent-session-control children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:concurrent-session-control documentation</h3>
   * Adds support for concurrent session control, allowing limits to be
   *               placed on the number of sessions a user can have.
   * </pre>
   *
   * @return the list of concurrent-session-control children.
   */
  @NotNull
  List<ConcurrentSessionControl> getConcurrentSessionControls();

  /**
   * Returns the list of remember-me children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:remember-me documentation</h3>
   * Sets up remember-me authentication. If used with the "key" attribute
   * (or no attributes) the cookie-only implementation will be used. Specifying
   * "token-repository-ref" or "remember-me-data-source-ref" will use the more secure,
   * persisten token approach.
   * </pre>
   *
   * @return the list of remember-me children.
   */
  @NotNull
  List<RememberMe> getRememberMes();

  /**
   * Returns the list of anonymous children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:anonymous documentation</h3>
   * Adds support for automatically granting all anonymous web requests a
   * particular principal identity and a corresponding granted authority.
   * </pre>
   *
   * @return the list of anonymous children.
   */
  @NotNull
  List<Anonymous> getAnonymouses();

  /**
   * Returns the list of port-mappings children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:port-mappings documentation</h3>
   * Defines the list of mappings between http and https ports for use in
   *               redirects
   * </pre>
   *
   * @return the list of port-mappings children.
   */
  @NotNull
  List<PortMappings> getPortMappingses();

  @NotNull
  CustomFilter getCustomFilter();
}
