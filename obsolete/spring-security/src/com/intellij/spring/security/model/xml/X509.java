package com.intellij.spring.security.model.xml;

import com.intellij.spring.model.xml.aop.RequiredBeanType;
import com.intellij.spring.model.xml.aop.TypedBeanResolveConverter;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:x509ElemType interface.
 */
public interface X509 extends SpringSecurityDomElement {

  /**
   * Returns the value of the subject-principal-regex child.
   * <pre>
   * <h3>Attribute null:subject-principal-regex documentation</h3>
   * The regular expression used to obtain the username from the certificate's
   * subject. Defaults to matching on the common name using the pattern
   * "CN=(.*?),".
   * </pre>
   *
   * @return the value of the subject-principal-regex child.
   */
  @NotNull
  GenericAttributeValue<String> getSubjectPrincipalRegex();

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
