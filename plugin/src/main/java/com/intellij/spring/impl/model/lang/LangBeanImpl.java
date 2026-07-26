/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.lang;

import jakarta.annotation.Nullable;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.impl.ide.model.xml.lang.LangBean;

/**
 * @author peter
 */
public abstract class LangBeanImpl extends DomSpringBeanImpl implements LangBean {
  @Nullable
  @Override
  public String getClassName() {
    return null;
  }
}
