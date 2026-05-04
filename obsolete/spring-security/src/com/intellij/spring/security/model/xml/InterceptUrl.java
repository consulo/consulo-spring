package com.intellij.spring.security.model.xml;

import com.intellij.spring.security.model.converters.SpringSecurityResourceConverter;
import com.intellij.spring.security.model.xml.converters.InterceptUrlAccessWrappingConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:intercept-urlElemType interface.
 */
public interface InterceptUrl extends SpringSecurityDomElement {

  /**
   * Returns the value of the pattern child.
   * <pre>
   * <h3>Attribute null:pattern documentation</h3>
   * The pattern which defines the URL path. The content will depend on the
   * type set in the containing http element, so will default to ant path syntax.
   * </pre>
   *
   * @return the value of the pattern child.
   */
  @NotNull
  @Referencing(value = SpringSecurityResourceConverter.class, soft = true)
  GenericAttributeValue<String> getPattern();

  /**
   * Returns the value of the access child.
   * <pre>
   * <h3>Attribute null:access documentation</h3>
   * The access configuration attributes that apply for the configured path.
   * </pre>
   *
   * @return the value of the access child.
   */
  @NotNull
  @Convert(value = InterceptUrlAccessWrappingConverter.class)
  GenericAttributeValue<Object> getAccess();

  /**
   * Returns the value of the method child.
   * <pre>
   * <h3>Attribute null:method documentation</h3>
   * The HTTP Method for which the access configuration attributes should
   * apply. If not specified, the attributes will apply to any method.
   * </pre>
   *
   * @return the value of the method child.
   */
  @NotNull
  GenericAttributeValue<Method> getMethod();

  /**
   * Returns the value of the filters child.
   * <pre>
   * <h3>Attribute null:filters documentation</h3>
   * The filter list for the path. Currently can be set to "none" to remove a
   * path from having any filters applied. The full filter stack (consisting of all filters
   * created by the namespace configuration, and any added using 'custom-filter'), will be
   * applied to any other paths.
   * </pre>
   *
   * @return the value of the filters child.
   */
  @NotNull
  GenericAttributeValue<Filters> getFilters();

  /**
   * Returns the value of the requires-channel child.
   * <pre>
   * <h3>Attribute null:requires-channel documentation</h3>
   * Used to specify that a URL must be accessed over http or https, or that there is no preference.
   * </pre>
   *
   * @return the value of the requires-channel child.
   */
  @NotNull
  GenericAttributeValue<RequiresChannel> getRequiresChannel();
}
