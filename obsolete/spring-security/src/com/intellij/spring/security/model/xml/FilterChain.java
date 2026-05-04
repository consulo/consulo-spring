package com.intellij.spring.security.model.xml;

import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.security.model.xml.converters.SpringSecurityFiltersBeansConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/security:filter-chainElemType interface.
 */
public interface FilterChain extends SpringSecurityDomElement {

  @NotNull
  GenericAttributeValue<String> getPattern();

  @NotNull
  @Convert(value = SpringSecurityFiltersBeansConverter.class)
  GenericAttributeValue<List<SpringBean>> getFilters();
}
