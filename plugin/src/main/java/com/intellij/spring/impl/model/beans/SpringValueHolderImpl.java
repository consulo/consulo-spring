/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.beans;

import com.intellij.spring.impl.ide.model.xml.beans.SpringValueHolder;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringRef;
import com.intellij.spring.impl.ide.model.xml.beans.SpringValue;
import consulo.xml.dom.GenericDomValue;
import consulo.xml.dom.DomUtil;

/**
 * @author peter
 */
public abstract class SpringValueHolderImpl extends SpringElementsHolderImpl implements SpringValueHolder {

  public GenericDomValue<SpringBeanPointer> getRefElement() {
    SpringRef springRef = getRef();
    if (DomUtil.hasXml(springRef.getBean())) return springRef.getBean();
    if (DomUtil.hasXml(springRef.getLocal())) return springRef.getLocal();
    if (DomUtil.hasXml(springRef.getParentAttr())) return springRef.getParentAttr();
    return getRefAttr();
  }

  public GenericDomValue<?> getValueElement() {
    SpringValue springValue = getValue();
    if (!DomUtil.hasXml(springValue)) return getValueAttr();
    return springValue;
  }
}
