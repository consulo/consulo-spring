package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/security:filter-chain-mapElemType interface.
 */
public interface FilterChainMap extends SpringSecurityDomElement {

  /**
   * Returns the value of the path-type child.
   * <pre>
   * <h3>Attribute null:path-type documentation</h3>
   * Defines the type of pattern used to specify URL paths
   * (either JDK 1.4-compatible regular expressions, or Apache Ant expressions). Defaults to "ant" if unspecified.
   * </pre>
   *
   * @return the value of the path-type child.
   */
  @NotNull
  GenericAttributeValue<PathType> getPathType();

  /**
   * Returns the list of filter-chain children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/security:filter-chain documentation</h3>
   * Used within filter-chain-map to define a specific URL pattern and the
   * list of filters which apply to the URLs matching that pattern. When multiple
   * filter-chain elements are used within a filter-chain-map element, the most specific
   * patterns must be placed at the top of the list, with most general ones at the bottom.
   * </pre>
   *
   * @return the list of filter-chain children.
   */
  @NotNull
  List<FilterChain> getFilterChains();
}
