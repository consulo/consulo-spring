package com.intellij.spring.security.model.xml;

import com.intellij.spring.security.model.xml.converters.InterceptMethodConverter;
import com.intellij.spring.security.model.xml.converters.SpringSecurityRolesConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/security:protectElemType interface.
 */
public interface Protect extends SpringSecurityDomElement {

  /**
   * Returns the value of the method child.
   * <pre>
   * <h3>Attribute null:method documentation</h3>
   * A method name
   * </pre>
   *
   * @return the value of the method child.
   */
  @NotNull
  @Convert(value = InterceptMethodConverter.class, soft = true)  
  GenericAttributeValue<String> getMethod();


  /**
   * Returns the value of the access child.
   * <pre>
   * <h3>Attribute null:access documentation</h3>
   * Access configuration attributes list that applies to the method, e.g.
   *           "ROLE_A,ROLE_B".
   * </pre>
   *
   * @return the value of the access child.
   */
  @NotNull
  @Convert(value = SpringSecurityRolesConverter.class)
  GenericAttributeValue<List<String>> getAccess();
}
