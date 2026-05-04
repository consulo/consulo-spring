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
 * http://www.springframework.org/schema/security:global-method-securityElemType interface.
 */
public interface GlobalMethodSecurity extends SpringSecurityDomElement {

  /**
   * Returns the value of the pre-post-annotations child.
   * <pre>
   * <h3>Attribute null:pre-post-annotations documentation</h3>
   * Specifies whether the use of Spring Security's pre and post invocation
   * annotations (@PreFilter, @PreAuthorize, @PostFilter, @PostAuthorize) should be enabled for
   * this application context. Defaults to "disabled".
   * </pre>
   *
   * @return the value of the pre-post-annotations child.
   */
  @NotNull
  GenericAttributeValue<PrePostAnnotations> getPrePostAnnotations();

  /**
   * Returns the value of the secured-annotations child.
   * <pre>
   * <h3>Attribute null:secured-annotations documentation</h3>
   * Specifies whether the use of Spring Security's @Secured annotations should
   * be enabled for this application context. Defaults to "disabled".
   * </pre>
   *
   * @return the value of the secured-annotations child.
   */
  @NotNull
  GenericAttributeValue<SecuredAnnotations> getSecuredAnnotations();

  /**
   * Returns the value of the jsr250-annotations child.
   * <pre>
   * <h3>Attribute null:jsr250-annotations documentation</h3>
   * Specifies whether JSR-250 style attributes are to be used
   * (for example "RolesAllowed"). This will require the javax.annotation.security classes on the classpath.
   * Defaults to "disabled".
   * </pre>
   *
   * @return the value of the jsr250-annotations child.
   */
  @NotNull
  GenericAttributeValue<Jsr250Annotations> getJsr250Annotations();

  /**
   * Returns the value of the access-decision-manager-ref child.
   * <pre>
   * <h3>Attribute null:access-decision-manager-ref documentation</h3>
   * Optional AccessDecisionManager bean ID to override the default used for method security.
   * </pre>
   *
   * @return the value of the access-decision-manager-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.ACCESS_DECISION_MANAGER)
  @Convert(TypedBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getAccessDecisionManagerRef();

  /**
   * Returns the value of the run-as-manager-ref child.
   * <pre>
   * <h3>Attribute null:run-as-manager-ref documentation</h3>
   * Optional RunAsmanager implementation which will be used by the configured MethodSecurityInterceptor
   * </pre>
   *
   * @return the value of the run-as-manager-ref child.
   */
  @NotNull
  @RequiredBeanType(SpringSecurityClassesConstants.REMEMBER_ME_SERVICES)
  @Convert(TypedBeanResolveConverter.class)  
  GenericAttributeValue<SpringBeanPointer> getRunAsManagerRef();

  /**
   * Returns the list of protect-pointcut children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:protect-pointcut documentation</h3>
   * Defines a protected pointcut and the access control configuration
   * attributes that apply to it. Every bean registered in the Spring application context
   * that provides a method that matches the pointcut will receive security authorization.
   * </pre>
   *
   * @return the list of protect-pointcut children.
   */
  @NotNull
  List<ProtectPointcut> getProtectPointcuts();

  /**
   * Returns the value of the pre-post-annotation-handling child.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:pre-post-annotation-handling documentation</h3>
   * Allows the default expression-based mechanism for handling Spring
   * Security's pre and post invocation annotations (@PreFilter, @PreAuthorize,
   *
   * @return the value of the pre-post-annotation-handling child.
   * @PostFilter, @PostAuthorize) to be replace entirely. Only applies if these
   * annotations are enabled.
   * </pre>
   */
  @NotNull
  PrePostAnnotationHandling getPrePostAnnotationHandling();

  /**
   * Returns the value of the expression-handler child.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:expression-handler documentation</h3>
   * Defines the SecurityExpressionHandler instance which will be used if
   * expression-based access-control is enabled. A default implementation (with no ACL support)
   * will be used if not supplied.
   * </pre>
   *
   * @return the value of the expression-handler child.
   */
  @NotNull
  ExpressionHandler getExpressionHandler();
}
