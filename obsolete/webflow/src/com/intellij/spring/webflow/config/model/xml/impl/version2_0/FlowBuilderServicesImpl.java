package com.intellij.spring.webflow.config.model.xml.impl.version2_0;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.webflow.config.model.xml.version2_0.FlowBuilderServices;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class FlowBuilderServicesImpl extends DomSpringBeanImpl implements FlowBuilderServices {

  @NotNull
  public String getClassName() {
    return FLOW_BUILDER_SERVICES_CLASS;
  }
}
