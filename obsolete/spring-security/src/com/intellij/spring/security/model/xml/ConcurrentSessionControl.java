package com.intellij.spring.security.model.xml;

import com.intellij.spring.model.xml.aop.RequiredBeanType;
import com.intellij.spring.model.xml.aop.TypedBeanResolveConverter;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Convert;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:concurrent-session-controlElemType interface.
 */
public interface ConcurrentSessionControl extends SpringSecurityDomElement {

  /**
   * Returns the value of the max-sessions child.
   * <pre>
   * <h3>Attribute null:max-sessions documentation</h3>
   * The maximum number of sessions a single user can have open at the same
   * time. Defaults to "1".
   * </pre>
   *
   * @return the value of the max-sessions child.
   */
  @NotNull
  GenericAttributeValue<Integer> getMaxSessions();

  /**
   * Returns the value of the expired-url child.
   * <pre>
   * <h3>Attribute null:expired-url documentation</h3>
   * The URL a user will be redirected to if they attempt to use a session
   * which has been "expired" by the concurrent session controller because they have logged in
   * again.
   * </pre>
   *
   * @return the value of the expired-url child.
   */
  @NotNull
  GenericAttributeValue<String> getExpiredUrl();

  /**
   * Returns the value of the exception-if-maximum-exceeded child.
   * <pre>
   * <h3>Attribute null:exception-if-maximum-exceeded documentation</h3>
   * Specifies that an exception should be raised when a user attempts to login
   *           when they already have the maximum configured sessions open. The default behaviour is to
   *           expire the original session.
   * </pre>
   *
   * @return the value of the exception-if-maximum-exceeded child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getExceptionIfMaximumExceeded();

  /**
   * Returns the value of the session-registry-alias child.
   * <pre>
   * <h3>Attribute null:session-registry-alias documentation</h3>
   * Allows you to define an alias for the SessionRegistry bean in order to
   * access it in your own configuration
   * </pre>
   *
   * @return the value of the session-registry-alias child.
   */
  @NotNull
  GenericAttributeValue<String> getSessionRegistryAlias();

  /**
   * Returns the value of the session-registry-ref child.
   * <pre>
   * <h3>Attribute null:session-registry-ref documentation</h3>
   * A reference to an external SessionRegistry implementation which will be
   * used in place of the standard one.
   * </pre>
   *
   * @return the value of the session-registry-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.SESSION_REGISTRY)
  @Convert(TypedBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getSessionRegistryRef();
}
