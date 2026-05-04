package com.intellij.spring.security.model.xml;

import com.intellij.spring.model.values.converters.ResourceValueConverter;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/security:user-serviceElemType interface.
 */
public interface UserService extends DomSpringBean, SpringSecurityDomElement {

  @NotNull
  @Referencing(value = ResourceValueConverter.class, soft = true)
  GenericAttributeValue<String> getProperties();

  @NotNull
  List<User> getUsers();
}
