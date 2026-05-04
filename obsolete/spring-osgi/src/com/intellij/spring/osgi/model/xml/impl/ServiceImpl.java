package com.intellij.spring.osgi.model.xml.impl;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.osgi.constants.SpringOsgiConstants;
import com.intellij.spring.osgi.model.xml.Service;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class ServiceImpl extends DomSpringBeanImpl implements Service {

  @NotNull
  public String getClassName() {
    return SpringOsgiConstants.OSGI_SERVICE_FACTORY_BEAN_CLASSNAME;
  }
}
