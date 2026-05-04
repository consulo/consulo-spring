package com.intellij.spring.osgi.model.xml;

import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

public interface BasicListener extends SpringOsgiDomElement {

  @NotNull
  @Convert(SpringBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getRef();

  SpringBean getBean();
}
