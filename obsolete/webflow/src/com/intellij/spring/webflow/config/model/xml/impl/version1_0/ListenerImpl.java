package com.intellij.spring.webflow.config.model.xml.impl.version1_0;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.webflow.config.model.xml.version1_0.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class ListenerImpl extends DomSpringBeanImpl implements Listener {

  @NotNull
  public String getClassName() {
    return FLOW_LISTENER_CLASS;
  }
}
