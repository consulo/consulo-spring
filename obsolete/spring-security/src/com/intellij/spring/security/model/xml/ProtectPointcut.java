package com.intellij.spring.security.model.xml;

import com.intellij.spring.security.model.xml.converters.SpringSecurityRolesConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/security:protect-pointcutElemType interface.
 */
public interface ProtectPointcut extends SpringSecurityDomElement {

  /**
   * Returns the value of the expression child.
   * <pre>
   * <h3>Attribute null:expression documentation</h3>
   * An AspectJ expression, including the 'execution' keyword. For example,
   * 'execution(int com.foo.TargetObject.countLength(String))' (without the quotes).
   * </pre>
   *
   * @return the value of the expression child.
   */
  @NotNull
  GenericAttributeValue<String> getExpression();


  /**
   * Returns the value of the access child.
   * <pre>
   * <h3>Attribute null:access documentation</h3>
   * Access configuration attributes list that applies to all methods matching the pointcut, e.g. "ROLE_A,ROLE_B"
   * </pre>
   *
   * @return the value of the access child.
   */
  @NotNull
  @Convert(value = SpringSecurityRolesConverter.class)
  GenericAttributeValue<List<String>> getAccess();
}
