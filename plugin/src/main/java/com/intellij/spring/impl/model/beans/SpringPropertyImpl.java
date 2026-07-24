/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.java.impl.psi.impl.beanProperties.BeanProperty;
import com.intellij.java.language.impl.psi.impl.source.PsiClassReferenceType;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiSubstitutor;
import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.util.TypeConversionUtil;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringProperty;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.ComparatorUtil;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SpringPropertyImpl extends SpringInjectionImpl implements SpringProperty {

  @Nonnull
  public List<? extends PsiType> getRequiredTypes() {
    List<BeanProperty> properties = getName().getValue();
    if (properties == null) return Collections.emptyList();
    ArrayList<PsiType> list = new ArrayList<PsiType>(properties.size());
    for (BeanProperty property : properties) {
      PsiType psiType = property.getPropertyType();

      if (psiType instanceof PsiClassReferenceType) {
        DomSpringBean bean = getParentOfType(DomSpringBean.class, false);
        if (bean != null) {
          PsiClass derivedClass = bean.getBeanClass();
          if (derivedClass != null) {
            PsiClass superClass = PsiTreeUtil.getParentOfType(property.getMethod(), PsiClass.class);
            if (superClass != null && derivedClass.isInheritor(superClass, true)) {
              PsiSubstitutor superClassSubstitutor =
                TypeConversionUtil.getSuperClassSubstitutor(superClass, derivedClass, PsiSubstitutor.EMPTY);

              list.add(superClassSubstitutor.substitute(psiType));
            }
          }
        }
      }
      list.add(psiType);
    }

    return list;
  }

  @NonNls
  public String getPropertyName() {
    return getName().getStringValue();
  }

  public int hashCode() {
    String name = getPropertyName();
    return name == null ? 0 : name.hashCode();
  }

  public boolean equals(Object obj) {
    return obj instanceof SpringProperty && ComparatorUtil.equalsNullable(getPropertyName(), ((SpringProperty)obj).getPropertyName());
  }
}
