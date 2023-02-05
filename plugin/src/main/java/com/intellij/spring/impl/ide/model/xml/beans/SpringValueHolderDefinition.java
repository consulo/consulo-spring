/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.xml.beans;

import consulo.xml.util.xml.GenericDomValue;

/**
 * @author peter
 */
public interface SpringValueHolderDefinition extends TypeHolder {

  GenericDomValue<?> getValueElement();

  GenericDomValue<SpringBeanPointer> getRefElement();
}
