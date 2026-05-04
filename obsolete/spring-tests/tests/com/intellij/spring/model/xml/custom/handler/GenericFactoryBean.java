/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.custom.handler;

/**
 * @author peter
*/
public class GenericFactoryBean extends TestFactoryBean {
  public Class<String> getObjectType() {
    return String.class;
  }
}