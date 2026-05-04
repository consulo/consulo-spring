/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.model.xml.custom;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author peter
*/
class MyBeanDefinitionsRegistry extends DefaultListableBeanFactory implements ResourceLoader {
  private final List myResult = new ArrayList();
  private final ResourceLoader myDelegate = new DefaultResourceLoader(getClass().getClassLoader());

  MyBeanDefinitionsRegistry() {
    myResult.add("no_infrastructures");
  }

  public List getResult() {
    return myResult;
  }

  public void registerBeanDefinition(String beanName, final BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
    super.registerBeanDefinition(beanName, beanDefinition);
    if (beanDefinition.getRole() == BeanDefinition.ROLE_INFRASTRUCTURE) {
      myResult.set(0, "has_infrastructures");
      return;
    }

    List info = new ArrayList();

    appendTag(info, beanName, "beanName");
    appendTag(info, beanDefinition.getBeanClassName(), "beanClassName");
    info.add("constructorArgumentCount");
    info.add(String.valueOf(beanDefinition.getConstructorArgumentValues().getArgumentCount()));

    if (beanDefinition instanceof AbstractBeanDefinition) {
      final AbstractBeanDefinition definition = (AbstractBeanDefinition)beanDefinition;
      appendTag(info, definition.getFactoryMethodName(), "factoryMethodName");
      appendTag(info, definition.getFactoryBeanName(), "factoryBeanName");
    }
    final Object source = beanDefinition.getSource();

    if (source instanceof int[]) {
      info.add("path");
      StringBuilder path = new StringBuilder("x"); //x: otherwise string may be empty
      final int[] ints = (int[])source;
      for (int i = 0; i < ints.length; i++) {
        if (i > 0) path.append(";");
        path.append(ints[i]);
      }
      info.add(path.toString());
    }
    myResult.add(info);
  }

  private static void appendTag(final List info, final String value, final String tagName) {
    if (value == null) return;

    info.add(tagName);
    info.add(CustomBeanParser.encode(value));
  }

  public ClassLoader getClassLoader() {
    return getClass().getClassLoader();
  }

  public Resource getResource(final String location) {
    return myDelegate.getResource(location);
  }
}
