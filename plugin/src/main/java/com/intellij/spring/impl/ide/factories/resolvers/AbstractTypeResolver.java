/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.factories.resolvers;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.factories.ObjectTypeResolver;
import com.intellij.spring.impl.ide.factories.SpringFactoryBeansManager;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringProperty;
import com.intellij.spring.impl.ide.model.xml.beans.SpringPropertyDefinition;
import consulo.language.psi.PsiManager;
import consulo.util.lang.StringUtil;
import consulo.xml.dom.DomUtil;
import consulo.xml.dom.convert.BooleanValueConverter;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Serega Vasiliev, Taras Tielkes
 */
public abstract class AbstractTypeResolver implements ObjectTypeResolver {
  @NonNls private static final String CLASS_ARRAY_EDITOR_SEPARATOR = ",";

  @Nullable
  protected static String getPropertyValue(@Nonnull CommonSpringBean bean, @Nonnull String propertyName) {
    if (bean instanceof SpringBean) {
      SpringPropertyDefinition property = SpringUtils.findPropertyByName((SpringBean)bean, propertyName);
      if (property != null) {
        String value = SpringUtils.getStringPropertyValue(property);
        if (value != null) return value;
      }
    }
    return null;
  }

  @Nonnull
  protected static Set<String> getListOrSetValues(@Nonnull SpringBean bean, @Nonnull String propertyName) {
    SpringPropertyDefinition property = SpringUtils.findPropertyByName(bean, propertyName);
    if (property != null) {
      return SpringUtils.getListOrSetValues(property);
    }
    return Collections.emptySet();
  }

  // @see org.springframework.beans.propertyeditors.ClassArrayEditor.setAsText(String text)
  @Nonnull
  protected static Set<String> getTypesFromClassArrayProperty(@Nonnull SpringBean context, String propertyName) {
    SpringPropertyDefinition property = SpringUtils.findPropertyByName(context, propertyName);
    if (property != null) {
      String stringValue = SpringUtils.getStringPropertyValue(property);
      if (stringValue != null) {
        return splitAndTrim(stringValue, CLASS_ARRAY_EDITOR_SEPARATOR);
      } else {
        return SpringUtils.getListOrSetValues(property);
      }
    }
    return Collections.emptySet();
  }

  @Nonnull
  private static Set<String> splitAndTrim(@Nonnull String value, @Nonnull String separator) {
    List<String> parts = StringUtil.split(value, separator);
    Set<String> trimmedParts = new HashSet<String>(parts.size());
    for (String part : parts) {
      trimmedParts.add(part.trim());
    }
    return trimmedParts;
  }

  protected static boolean isBooleanProperySetAndTrue(@Nonnull SpringBean context, @Nonnull String propertyName) {
    String value = getPropertyValue(context, propertyName);
    return value != null && BooleanValueConverter.getInstance(true).isTrue(value);
  }

  protected static boolean isBooleanProperySetAndFalse(@Nonnull SpringBean context, @Nonnull String propertyName) {
    String value = getPropertyValue(context, propertyName);
    return value != null && !BooleanValueConverter.getInstance(true).isTrue(value);
  }

  @Nullable
  protected static PsiClassType getTypeFromProperty(@Nonnull SpringBean context, @Nonnull String propertyName) {
    SpringPropertyDefinition targetProperty = SpringUtils.findPropertyByName(context, propertyName);

    if (targetProperty != null) {
      if (targetProperty instanceof SpringProperty) {
        // support chained FactoryBean resolving only for inner beans
        SpringProperty property = (SpringProperty)targetProperty;
        SpringBean bean = property.getBean();
        if (DomUtil.hasXml(bean)) {
          PsiClass[] classes = SpringUtils.getEffectiveBeanTypes(bean);
          PsiManager psiManager = bean.getPsiManager();
          if (classes.length > 0 && psiManager != null) {
            return JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory().createType(classes[0]);
          }
        }
      }
      return getTypeFromNonFactoryBean(SpringUtils.getReferencedSpringBean(targetProperty));
    }
    return null;
  }

  @Nullable
  protected static PsiClassType getTypeFromBeanName(@Nonnull SpringBean context, @Nonnull String beanName) {
    SpringModel model = SpringUtils.getSpringModel(context);
    return getTypeFromNonFactoryBean(model.findBean(beanName));
  }

  @Nullable
  private static PsiClassType getTypeFromNonFactoryBean(@Nullable SpringBeanPointer bean) {
    // chained FactoryBean resolving is not supported for top-level beans (to avoid circularity handling)
    if (bean != null) {
      PsiClass targetBeanClass = bean.getBeanClass();
      if (targetBeanClass != null && !SpringFactoryBeansManager.isBeanFactory(targetBeanClass)) {
        PsiManager psiManager = bean.getPsiManager();
        if (psiManager != null) {
          return JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory().createType(targetBeanClass);
        }
      }
    }
    return null;
  }
}
