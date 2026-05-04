package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/security:port-mappingElemType interface.
 */
public interface PortMapping extends SpringSecurityDomElement {

  @NotNull
  GenericAttributeValue<String> getHttp();

  @NotNull
  GenericAttributeValue<String> getHttps();
}
