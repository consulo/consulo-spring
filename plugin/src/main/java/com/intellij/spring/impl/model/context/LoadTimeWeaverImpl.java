package com.intellij.spring.impl.model.context;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.impl.ide.model.xml.context.LoadTimeWeaver;
import jakarta.annotation.Nullable;

public abstract class LoadTimeWeaverImpl extends DomSpringBeanImpl implements LoadTimeWeaver {
  @Nullable
  @Override
  public String getClassName() {
    return null; //todo
  }
}
