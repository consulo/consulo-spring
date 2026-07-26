package com.intellij.spring.impl.model.jee;

import com.intellij.spring.impl.ide.model.xml.jee.LocalSlsb;
import com.intellij.spring.impl.model.DomSpringBeanImpl;
import jakarta.annotation.Nonnull;

public abstract class LocalSlsbImpl extends DomSpringBeanImpl implements LocalSlsb {
  @Nonnull
  @Override
  public String getClassName() {
    return "org.springframework.ejb.access.LocalStatelessSessionProxyFactoryBean";
  }
}