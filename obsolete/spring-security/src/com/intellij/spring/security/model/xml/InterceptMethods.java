package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Convert;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.xml.aop.RequiredBeanType;
import com.intellij.spring.model.xml.aop.TypedBeanResolveConverter;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/security:intercept-methodsElemType interface.
 */
public interface InterceptMethods extends SpringSecurityDomElement {

  /**
   * Returns the value of the access-decision-manager-ref child.
   * <pre>
   * <h3>Attribute null:access-decision-manager-ref documentation</h3>
   * Optional AccessDecisionManager bean ID to be used by the created method
   *           security interceptor.
   * </pre>
   *
   * @return the value of the access-decision-manager-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.ACCESS_DECISION_MANAGER)
  @Convert(TypedBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getAccessDecisionManagerRef();


  /**
   * Returns the list of protect children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:protect documentation</h3>
   * Defines a protected method and the access control configuration
   *               attributes that apply to it. We strongly advise you NOT to mix "protect" declarations
   *               with any services provided "global-method-security".
   * </pre>
   *
   * @return the list of protect children.
   */
  @NotNull
  List<Protect> getProtects();
}
