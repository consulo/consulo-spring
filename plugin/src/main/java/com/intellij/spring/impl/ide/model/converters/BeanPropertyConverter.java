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
import consulo.xml.dom.*;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class BeanPropertyConverter extends Converter<List<BeanProperty>> implements CustomReferenceConverter<List<BeanProperty>> {

  public List<BeanProperty> fromString(@Nullable String s, ConvertContext context) {
    if (s == null) {
      return null;
    }
    GenericAttributeValue<List<BeanProperty>> value = (GenericAttributeValue<List<BeanProperty>>)context.getInvocationElement();
    PropertyReference[] references = createReferences(value, value.getXmlAttributeValue(), context);
    if (references.length > 0) {
      ResolveResult[] results = references[references.length - 1].multiResolve(false);
      ArrayList<BeanProperty> list = new ArrayList<BeanProperty>(results.length);
      for (ResolveResult result : results) {
        PsiMethod method = (PsiMethod) result.getElement();
         if (method != null) {
           BeanProperty beanProperty = BeanProperty.createBeanProperty(method);
           if (beanProperty != null) {
             list.add(beanProperty);
           }
         }
      }
      return list;
    }
    return null;
  }

  public String toString(@Nullable List<BeanProperty> beanProperty, ConvertContext context) {
    return null;
  }

  @Nonnull
  public PropertyReference[] createReferences(GenericDomValue<List<BeanProperty>> genericDomValue, PsiElement element, ConvertContext context) {
    CommonSpringBean springBean = SpringConverterUtil.getCurrentBeanCustomAware(context);
    if (springBean != null) {
      return new PropertyReferenceSet(element, springBean.getBeanClass(), genericDomValue, context, springBean).getPsiReferences();
    }
    return new PropertyReference[0];
  }
}
