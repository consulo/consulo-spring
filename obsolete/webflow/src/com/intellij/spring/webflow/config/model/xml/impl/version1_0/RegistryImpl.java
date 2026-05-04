package com.intellij.spring.webflow.config.model.xml.impl.version1_0;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.webflow.config.model.xml.version1_0.Registry;
import org.jetbrains.annotations.NotNull;


@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class RegistryImpl extends DomSpringBeanImpl implements Registry {

  @NotNull
  public String getClassName() {
    return FLOW_DEFINITION_REGISTRY_CLASS;
  }
}
