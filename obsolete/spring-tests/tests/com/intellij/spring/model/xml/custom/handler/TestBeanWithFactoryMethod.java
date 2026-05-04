/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.custom.handler;

/**
 * @author peter
*/
public class TestBeanWithFactoryMethod {
  public static String foo(int a) {
    return null;
  }
  public static String foo(boolean a) {
    return null;
  }
  public StringBuffer foo(char a) {
    return null;
  }

  public static StringBuffer bar(int a) {
    return null;
  }
  public static String bar(boolean a) {
    return null;
  }
}