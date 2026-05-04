package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:pre-post-annotation-handlingElemType interface.
 */
public interface PrePostAnnotationHandling extends SpringSecurityDomElement {

  /**
   * Returns the value of the invocation-attribute-factory child.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:invocation-attribute-factory documentation</h3>
   * Defines the PrePostInvocationAttributeFactory instance which
   *  is used to generate pre and post invocation metadata from the annotated methods.
   * </pre>
   *
   * @return the value of the invocation-attribute-factory child.
   */
  @NotNull
  InvocationAttributeFactory getInvocationAttributeFactory();

  @NotNull
  PreInvocationAdvice getPreInvocationAdvice();

  @NotNull
  PostInvocationAdvice getPostInvocationAdvice();
}
