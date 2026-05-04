package com.intellij.spring.osgi.model.xml.impl;

import com.intellij.psi.CommonClassNames;
import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.osgi.model.xml.Reference;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class ReferenceImpl extends DomSpringBeanImpl implements Reference {

  @Nullable
  public String getClassName() {
    return CommonClassNames.JAVA_LANG_OBJECT;
  }
}