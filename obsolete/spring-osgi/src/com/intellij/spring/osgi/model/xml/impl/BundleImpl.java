package com.intellij.spring.osgi.model.xml.impl;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.osgi.constants.SpringOsgiConstants;
import com.intellij.spring.osgi.model.xml.Bundle;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class BundleImpl extends DomSpringBeanImpl implements Bundle {

  @NotNull
  public String getClassName() {
    return SpringOsgiConstants.OSGI_FRAMEWORK_BUNDLE_CLASSNAME;
  }
}
