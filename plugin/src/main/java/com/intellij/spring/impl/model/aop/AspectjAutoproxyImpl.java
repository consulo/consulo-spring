/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.aop;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.impl.ide.model.xml.aop.AspectjAutoproxy;
import com.intellij.spring.impl.ide.constants.SpringConstants;

/**
 * @author peter
 */
public abstract class AspectjAutoproxyImpl extends DomSpringBeanImpl implements AspectjAutoproxy {
  @Override
  public String getClassName() {
    return SpringConstants.ASPECTJ_AUTOPROXY_BEAN_CLASS;
  }
}
