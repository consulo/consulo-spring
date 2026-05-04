package com.intellij.spring.security.model.xml;

import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:custom-filterElemType interface.
 */
public interface CustomFilter extends SpringSecurityDomElement {

  /**
   * Returns the value of the after child.
   * <pre>
   * <h3>Attribute null:after documentation</h3>
   * The filter immediately after which the custom-filter should be placed in
   * the chain. This feature will only be needed by advanced users who wish to mix their own
   * filters into the security filter chain and have some knowledge of the standard Spring
   * Security filters. The filter names map to specific Spring Security implementation filters.
   * </pre>
   *
   * @return the value of the after child.
   */
  @NotNull
  GenericAttributeValue<NamedSecurityFilter> getAfter();

  /**
   * Returns the value of the before child.
   * <pre>
   * <h3>Attribute null:before documentation</h3>
   * The filter immediately before which the custom-filter should be placed in the chain
   * </pre>
   *
   * @return the value of the before child.
   */
  @NotNull
  GenericAttributeValue<NamedSecurityFilter> getBefore();

  /**
   * Returns the value of the position child.
   * <pre>
   * <h3>Attribute null:position documentation</h3>
   * The explicit position at which the custom-filter should be placed in the
   * chain. Use if you are replacing a standard filter.
   * </pre>
   *
   * @return the value of the position child.
   */
  @NotNull
  GenericAttributeValue<NamedSecurityFilter> getPosition();

  @NotNull
  @Convert(SpringBeanResolveConverter.class)  
  GenericAttributeValue<SpringBeanPointer> getRef();

}
