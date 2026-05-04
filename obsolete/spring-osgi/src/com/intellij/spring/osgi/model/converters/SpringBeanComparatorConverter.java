package com.intellij.spring.osgi.model.converters;

import com.intellij.psi.CommonClassNames;
import com.intellij.spring.model.converters.SpringBeanResolveConverterForDefiniteClasses;
import com.intellij.util.xml.ConvertContext;

public class SpringBeanComparatorConverter  extends SpringBeanResolveConverterForDefiniteClasses {
  protected String[] getClassNames(final ConvertContext context) {
    return new String[] {CommonClassNames.JAVA_UTIL_COMPARATOR};
  }
}
