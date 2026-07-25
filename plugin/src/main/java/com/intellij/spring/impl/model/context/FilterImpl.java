package com.intellij.spring.impl.model.context;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.impl.ide.model.xml.context.Filter;
import jakarta.annotation.Nullable;

public abstract class FilterImpl extends DomSpringBeanImpl implements Filter {
  @Nullable
  @Override
  public String getClassName() {
    return null; //todo
  }
}
