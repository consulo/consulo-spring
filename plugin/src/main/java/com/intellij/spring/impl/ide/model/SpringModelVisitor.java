/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model;

import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.*;
import consulo.xml.dom.DomUtil;

/**
 * @author Dmitry Avdeev
 */
public abstract class SpringModelVisitor {

  /**
   * Visits a bean.
   *
   * @param bean bean to be visited.
   * @return false to stop traversing.
   */
  protected boolean visitBean(CommonSpringBean bean) {
    return true;
  }

  protected boolean visitProperty(SpringPropertyDefinition property) {
    return true;
  }

  protected boolean visitConstructorArg(ConstructorArg arg) {
    return true;
  }

  protected boolean visitValueHolder(SpringValueHolder valueHolder) {
    return true;
  }

  protected boolean visitMapEntry(SpringEntry entry) {
    return true;
  }

  protected boolean visitRef(SpringRef ref) {
    return true;
  }

  protected boolean visitIdref(Idref idref) {
    return true;
  }

  public static boolean visitBeans(SpringModelVisitor visitor, Beans beans) {
    for (CommonSpringBean bean : SpringUtils.getChildBeans(beans, true)) {
      if (!visitBean(visitor, bean)) {
        return false;
      }
    }
    return true;
  }

  public static boolean visitBean(SpringModelVisitor visitor, CommonSpringBean bean) {
    if (bean instanceof DomSpringBean && !DomUtil.hasXml(((DomSpringBean)bean))) return true;
    if (!visitor.visitBean(bean)) return false;

    for (SpringPropertyDefinition property : SpringUtils.getProperties(bean)) {
      if (!visitor.visitProperty(property)) return false;
      if (property instanceof SpringValueHolder && !visitValueHolder(visitor, (SpringValueHolder)property)) return false;
    }
    for (ConstructorArg arg : SpringUtils.getConstructorArgs(bean)) {
      if (!visitor.visitConstructorArg(arg)) return false;
      if (!visitValueHolder(visitor, arg)) return false;
    }
    return true;
  }

  public static boolean visitValueHolder(SpringModelVisitor visitor, SpringValueHolder elementsHolder) {

    return !DomUtil.hasXml(elementsHolder) || visitor.visitValueHolder(elementsHolder) && visitElementsHolder(visitor, elementsHolder);

  }

  private static boolean visitElementsHolder(SpringModelVisitor visitor, SpringElementsHolder elementsHolder) {
    if (!DomUtil.hasXml(elementsHolder)) return true;
    if (!visitBean(visitor, elementsHolder.getBean())) return false;
    SpringRef ref = elementsHolder.getRef();
    if (DomUtil.hasXml(ref) && !visitor.visitRef(ref)) return false;
    Idref idref = elementsHolder.getIdref();
    if (DomUtil.hasXml(idref) && !visitor.visitIdref(idref)) return false;
    if (!visitCollection(visitor, elementsHolder.getList())) return false;
    if (!visitCollection(visitor, elementsHolder.getSet())) return false;
    if (!visitMap(visitor, elementsHolder.getMap())) return false;
    return true;
  }

  public static boolean visitCollection(SpringModelVisitor visitor, ListOrSet collection) {
    if (!DomUtil.hasXml(collection)) return true;

    for (CommonSpringBean bean : SpringUtils.getChildBeans(collection, true)) {
      if (!visitBean(visitor, bean)) return false;
    }
    for (ListOrSet listOrSet : collection.getSets()) {
      if (!visitCollection(visitor, listOrSet)) return false;
    }
    for (ListOrSet listOrSet : collection.getLists()) {
      if (!visitCollection(visitor, listOrSet)) return false;
    }
    for (SpringMap map : collection.getMaps()) {
      if (!visitMap(visitor, map)) return false;
    }
    for (SpringRef ref : collection.getRefs()) {
      if (!visitor.visitRef(ref)) return false;
    }
    for (Idref idref : collection.getIdrefs()) {
      if (!visitor.visitIdref(idref)) return false;
    }
    return true;
  }

  public static boolean visitMap(SpringModelVisitor visitor, SpringMap map) {
    if (!DomUtil.hasXml(map)) return true;
    for (SpringEntry entry : map.getEntries()) {
      if (!visitor.visitMapEntry(entry)) return false;
      if (!visitValueHolder(visitor, entry)) return false;
      SpringKey key = entry.getKey();
      if (DomUtil.hasXml(key) && !visitElementsHolder(visitor, key)) return false;
    }
    return true;
  }
}
