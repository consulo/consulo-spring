package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/security:filter-invocation-definition-sourceElemType interface.
 */
public interface FilterInvocationDefinitionSource extends SpringSecurityDomElement {

  /**
   * Returns the value of the use-expressions child.
   * <pre>
   * <h3>Attribute null:use-expressions documentation</h3>
   * Enables the use of expressions in the 'access' attributes in
   * &lt;intercept-url&gt; elements rather than the traditional list of configuration
   * attributes. Defaults to 'false'. If enabled, each attribute should contain a single
   * boolean expression. If the expression evaluates to 'true', access will be granted.
   * </pre>
   *
   * @return the value of the use-expressions child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getUseExpressions();

  /**
   * Returns the value of the lowercase-comparisons child.
   * <pre>
   * <h3>Attribute null:lowercase-comparisons documentation</h3>
   * as for http element
   * </pre>
   *
   * @return the value of the lowercase-comparisons child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getLowercaseComparisons();

  /**
   * Returns the value of the path-type child.
   * <pre>
   * <h3>Attribute null:path-type documentation</h3>
   * Defines the type of pattern used to specify URL paths (either JDK
   * 1.4-compatible regular expressions, or Apache Ant expressions). Defaults to "ant" if
   * unspecified.
   * </pre>
   *
   * @return the value of the path-type child.
   */
  @NotNull
  GenericAttributeValue<PathType> getPathType();

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
}
