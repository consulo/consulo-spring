/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl;

import com.intellij.spring.impl.ide.model.xml.beans.Alias;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.spring.impl.model.BaseSpringModel;
import consulo.util.collection.MultiMap;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;

/**
 * @author Dmitry Avdeev
 */
public class BeanNamesMapper {

  private final Map<String, SpringBaseBeanPointer> myBeansMap;
  private final Map<String, String> myAliasesMap;
  private final MultiMap<String, String> myAllBeanNames;

  private final Set<String> myDuplicatedNames = new HashSet<>();

  public BeanNamesMapper(BaseSpringModel model) {

    Collection<? extends SpringBaseBeanPointer> springBeans = model.getAllCommonBeans();
    Collection<SpringBaseBeanPointer> ownBeans = model.getOwnBeans();

    myBeansMap = new HashMap<>(springBeans.size());
    myAliasesMap = new HashMap<>();
    myAllBeanNames = new MultiMap<>() {
      @Nonnull
      @Override
      protected Collection<String> createCollection() {
        return new HashSet<>();
      }

      @Nonnull
      @Override
      protected Collection<String> createEmptyCollection() {
        return Collections.emptySet();
      }
    };

    for (SpringBaseBeanPointer bean : springBeans) {
      String beanName = bean.getName();
      if (StringUtil.isNotEmpty(beanName)) {
        SpringBaseBeanPointer duplication = myBeansMap.put(beanName, bean);
        if (duplication != null && ownBeans.contains(duplication)) {
          if (ownBeans.contains(bean)) {
            myDuplicatedNames.add(beanName);
          }
          else {
            myBeansMap.put(beanName, duplication);
          }
        }
        myAllBeanNames.putValue(beanName, beanName);
        for (String alias : bean.getAliases()) {
          registerAlias(beanName, alias);
        }
      }
    }

    List<Alias> aliases = model.getAliases(true);
    for (Alias anAlias : aliases) {
      registerAlias(anAlias.getAliasedBean().getStringValue(), anAlias.getAlias().getStringValue());
    }
  }

  @Nullable
  public SpringBeanPointer getBean(@Nonnull String beanName) {
    String curName = beanName;
    Set<String> visited = null;
    while (true) {
      SpringBeanPointer bean = myBeansMap.get(curName);
      if (bean != null) return bean.derive(beanName);

      String newName = myAliasesMap.get(curName);
      if (newName == null || (visited != null && visited.contains(curName))) return null;

      if (visited == null) {
        visited = new HashSet<>();
      }
      visited.add(curName);
      curName = newName;
    }
  }

  public Set<String> getAllBeanNames(String beanName) {
    return (Set<String>)myAllBeanNames.get(beanName);
  }

  public boolean isNameDuplicated(@Nonnull String name) {
    return myDuplicatedNames.contains(name);
  }

  private void registerAlias(String beanName, String alias) {

    if (!StringUtil.isNotEmpty(alias) || !StringUtil.isNotEmpty(beanName) || Comparing.equal(beanName, alias)) {
      return;
    }

    String duplication = myAliasesMap.put(alias, beanName);
    if (duplication != null || myBeansMap.containsKey(alias)) {
      myDuplicatedNames.add(alias);
    }

    HashSet<String> aliases = new HashSet<>();
    aliases.add(alias);
    while (!myBeansMap.containsKey(beanName)) {
      beanName = myAliasesMap.get(beanName);
      if (beanName == null) {
        return;
      }
      aliases.add(beanName);
    }
    for (String s : aliases) {
      myAllBeanNames.putValue(beanName, s);
    }
  }

}
