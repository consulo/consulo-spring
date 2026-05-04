package com.intellij.spring.webflow.model.converters;

import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.webflow.util.WebflowUtil;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class WebflowVarBeanConverter extends WebflowBeanResolveConverterForDefiniteClasses {

  @NotNull
  public Collection<SpringBeanPointer> getVariants(final ConvertContext context) {
    final List<SpringBeanPointer> filtered = new ArrayList<SpringBeanPointer>();

    for (SpringBeanPointer pointer : super.getVariants(context)) {
      final CommonSpringBean bean = pointer.getSpringBean();
      if (bean.isValid() && WebflowUtil.isNonSingletonPrototype(bean)) {
        filtered.add(pointer);
      }
    }

    return filtered;
  }

  protected String[] getClassNames(final ConvertContext context) {
    return null;
  }
}
