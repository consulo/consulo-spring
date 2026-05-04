package com.intellij.spring.osgi.model.xml.impl;

import com.intellij.psi.CommonClassNames;
import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.osgi.model.xml.List;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class ListImpl extends DomSpringBeanImpl implements List {

  @Nullable
  public String getClassName() {
    return CommonClassNames.JAVA_UTIL_LIST;
  }
}
