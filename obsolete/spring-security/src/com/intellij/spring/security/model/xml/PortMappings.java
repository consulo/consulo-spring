package com.intellij.spring.security.model.xml;

import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/security:port-mappingsElemType interface.
 */
public interface PortMappings extends SpringSecurityDomElement {

  @NotNull
  List<PortMapping> getPortMappings();
}
