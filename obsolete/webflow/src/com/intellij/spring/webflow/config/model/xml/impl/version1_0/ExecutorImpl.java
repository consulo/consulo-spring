package com.intellij.spring.webflow.config.model.xml.impl.version1_0;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.webflow.config.model.xml.version1_0.Executor;
import org.jetbrains.annotations.NotNull;

/**
 * User: Sergey.Vasiliev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class ExecutorImpl extends DomSpringBeanImpl implements Executor {

  @NotNull
  public String getClassName() {
    return FLOW_EXECUTOR_CLASS;
  }
}
