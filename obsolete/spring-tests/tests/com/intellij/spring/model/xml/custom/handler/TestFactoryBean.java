/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.xml.custom.handler;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author peter
*/
public class TestFactoryBean implements FactoryBean {
  public Object getObject() throws Exception {
    throw new UnsupportedOperationException("Method getObject is not yet implemented in " + getClass().getName());
  }

  public Class getObjectType() {
    return String.class;
  }

  public boolean isSingleton() {
    throw new UnsupportedOperationException("Method isSingleton is not yet implemented in " + getClass().getName());
  }
}
