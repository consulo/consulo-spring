/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package com.intellij.spring.impl.model.util;

import com.intellij.spring.impl.ide.model.xml.util.SpringConstant;
import com.intellij.spring.impl.model.DomSpringBeanImpl;
import javax.annotation.Nonnull;

/**
 * @author Dmitry Avdeev
 */
public abstract class UtilConstantImpl extends DomSpringBeanImpl implements SpringConstant {
  @Nonnull
  public String getClassName() {
    return "org.springframework.beans.factory.config.FieldRetrievingFactoryBean";
  }

}
