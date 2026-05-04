package com.intellij.spring.osgi.model.xml;

import com.intellij.spring.model.converters.SpringBeanListConverter;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Bundle extends SpringOsgiDomElement, DomSpringBean {

  @NotNull
  GenericAttributeValue<String> getSymbolicName();

  @Convert(value = SpringBeanListConverter.class)
  GenericAttributeValue<List<SpringBean>> getDependsOn();

  @NotNull
  GenericAttributeValue<String> getLocation();

  @NotNull
  GenericAttributeValue<BundleAction> getAction();

  @NotNull
  GenericAttributeValue<BundleAction> getDestroyAction();

  @NotNull
  GenericAttributeValue<Integer> getStartLevel();
}
