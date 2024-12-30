/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.impl.psi.impl.beanProperties.BeanProperty;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.spring.impl.ide.model.properties.PropertyReference;
import com.intellij.spring.impl.ide.model.properties.PropertyReferenceSet;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.xml.util.xml.*;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class BeanPropertyConverter extends Converter<List<BeanProperty>> implements CustomReferenceConverter<List<BeanProperty>> {

  public List<BeanProperty> fromString(final @Nullable String s, final ConvertContext context) {
    if (s == null) {
      return null;
    }
    final GenericAttributeValue<List<BeanProperty>> value = (GenericAttributeValue<List<BeanProperty>>)context.getInvocationElement();
    final PropertyReference[] references = createReferences(value, value.getXmlAttributeValue(), context);
    if (references.length > 0) {
      final ResolveResult[] results = references[references.length - 1].multiResolve(false);
      final ArrayList<BeanProperty> list = new ArrayList<BeanProperty>(results.length);
      for (ResolveResult result : results) {
        final PsiMethod method = (PsiMethod) result.getElement();
         if (method != null) {
           final BeanProperty beanProperty = BeanProperty.createBeanProperty(method);
           if (beanProperty != null) {
             list.add(beanProperty);
           }
         }
      }
      return list;
    }
    return null;
  }

  public String toString(final @Nullable List<BeanProperty> beanProperty, final ConvertContext context) {
    return null;
  }

  @Nonnull
  public PropertyReference[] createReferences(final GenericDomValue<List<BeanProperty>> genericDomValue, final PsiElement element, final ConvertContext context) {
    final CommonSpringBean springBean = SpringConverterUtil.getCurrentBeanCustomAware(context);
    if (springBean != null) {
      return new PropertyReferenceSet(element, springBean.getBeanClass(), genericDomValue, context, springBean).getPsiReferences();
    }
    return new PropertyReference[0];
  }
}
