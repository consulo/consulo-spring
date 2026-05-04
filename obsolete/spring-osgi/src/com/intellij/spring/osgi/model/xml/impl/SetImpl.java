package com.intellij.spring.osgi.model.xml.impl;

import com.intellij.psi.CommonClassNames;
import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.osgi.model.xml.Set;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SetImpl extends DomSpringBeanImpl implements Set {

  @Nullable
  public String getClassName() {
    return CommonClassNames.JAVA_UTIL_SET;
  }
}
