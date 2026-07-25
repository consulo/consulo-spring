/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.beans;

import com.intellij.java.language.psi.*;
import com.intellij.spring.impl.ide.factories.SpringFactoryBeansManager;
import com.intellij.spring.impl.ide.model.ResolvedConstructorArgs;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.converters.ConstructorArgIndexConverter;
import com.intellij.spring.impl.ide.model.converters.SpringConverterUtil;
import com.intellij.spring.impl.ide.model.xml.beans.ConstructorArg;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.util.lang.ComparatorUtil;
import consulo.xml.dom.GenericAttributeValue;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class ConstructorArgImpl extends SpringInjectionImpl implements ConstructorArg {
  @Nonnull
  @Override
  public List<? extends PsiType> getRequiredTypes() {
    PsiType type = getType().getValue();
    if (type != null) {
      return Collections.singletonList(type);
    }
    SpringBean springBean = (SpringBean)getParent();
    assert springBean != null;
    GenericAttributeValue<Integer> index = getIndex();
    if (index.getValue() != null) {
      PsiParameter parameter = ConstructorArgIndexConverter.resolve(index, springBean);
      return parameter == null ? Collections.<PsiType>emptyList() : Collections.singletonList(parameter.getType());
    }
    ResolvedConstructorArgs resolvedArgs = springBean.getResolvedConstructorArgs();
    PsiMethod resolvedMethod = resolvedArgs.getResolvedMethod();
    if (resolvedMethod != null) {
      PsiParameter parameter = resolvedArgs.getResolvedArgs(resolvedMethod).get(this);
      if (parameter != null) {
        return Collections.singletonList(parameter.getType());
      }
    }
    return Collections.emptyList();
  }

  @Override
  public boolean isAssignable(@Nonnull PsiType to) {
    PsiType[] types = getTypesByValue();
    if (types == null) {
      return true;
    }
    for (PsiType typeByValue : types) {
      if (to instanceof PsiClassType toClassType && typeByValue instanceof PsiClassType classTypeByValue) {
        PsiClass psiClass = classTypeByValue.resolve();
        if (psiClass != null && SpringFactoryBeansManager.isBeanFactory(psiClass)) {
          SpringBean springBean = (SpringBean)getParent();
          assert springBean != null;
          PsiClass requiredClass = toClassType.resolve();
          SpringBeanPointer factoryBean = SpringUtils.getReferencedSpringBean(this);
          if (requiredClass != null && factoryBean != null) {
            return SpringFactoryBeansManager.getInstance().canProduce(psiClass, requiredClass, factoryBean.getSpringBean());
          }
        }
      }
      if (typeByValue.equals(SpringConverterUtil.findType(String.class, getManager().getProject()))
          || SpringConverterUtil.isConvertable(typeByValue, to, getManager().getProject())) {
        return true;
      }

    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getIndex().getValue());
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof ConstructorArg that)) return false;

    if (getXmlTag().equals(that.getXmlTag())) return true;
    
    Integer index = getIndex().getValue();
    return index != null && ComparatorUtil.equalsNullable(index, that.getIndex().getValue());
  }
}
