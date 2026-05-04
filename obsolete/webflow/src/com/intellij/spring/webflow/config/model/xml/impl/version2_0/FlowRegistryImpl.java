package com.intellij.spring.webflow.config.model.xml.impl.version2_0;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.webflow.config.model.xml.version2_0.FlowRegistry;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class FlowRegistryImpl extends DomSpringBeanImpl implements FlowRegistry {

  @NotNull
  public String getClassName() {
    return FLOW_REGISTRY_CLASS;
  }
}

