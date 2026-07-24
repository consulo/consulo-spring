/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.TypeHolder;
import consulo.codeEditor.Editor;
import consulo.language.editor.TargetElementUtil;
import consulo.language.psi.PsiElement;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomManager;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class SpringBeanUtil {

  private SpringBeanUtil() {
  }

  @Nullable
  public static PsiClass getInstantiationClass(SpringBean bean) {
    PsiMethod factoryMethod = bean.getFactoryMethod().getValue();
    if (factoryMethod != null) {
      return SpringBeanFactoryMethodConverter.getFactoryClass(bean);
    }
    return bean.getBeanClass();
  }

  public static boolean isInstantiatedByFactory(SpringBean bean) {
    return bean.getFactoryMethod().getValue() != null;
  }

  @Nonnull
  public static List<PsiMethod> getInstantiationMethods(SpringBean springBean) {
    String factoryMethod = springBean.getFactoryMethod().getStringValue();
    if (factoryMethod != null) {
      return SpringBeanFactoryMethodConverter.getFactoryMethodCandidates(springBean, factoryMethod);
    }
    PsiClass beanClass = springBean.getBeanClass();
    if (beanClass != null) {
      return Arrays.asList(beanClass.getConstructors()); //private constructor is accepted
    }
    return Collections.emptyList();
  }

  @Nullable
  public static PsiType getRequiredType(@Nonnull TypeHolder valueHolder) {
    List<? extends PsiType> psiTypes = valueHolder.getRequiredTypes();
    return psiTypes.isEmpty() ? null : psiTypes.get(0);
  }

  @Nullable
  public static PsiClassType getRequiredClass(@Nonnull TypeHolder valueHolder) {
    PsiType injectionType = getRequiredType(valueHolder);
    if (injectionType instanceof PsiClassType) {
      return (PsiClassType)injectionType;
    }
    return null;
  }

  @Nullable
  public static DomSpringBean getTargetSpringBean(Editor editor) {
    if (editor != null) {
      PsiElement targetPsiElement = TargetElementUtil.findTargetElement(editor, TargetElementUtil.getReferenceSearchFlags());
      if (targetPsiElement instanceof XmlTag) {
        DomElement value = DomManager.getDomManager(targetPsiElement.getProject()).getDomElement((XmlTag)targetPsiElement);
        if (value instanceof DomSpringBean) {
          return (DomSpringBean)value;
        }
      }
    }
    return null;
  }

  public static DomSpringBean getTargetSpringBean(PsiElement element) {

    if (element instanceof XmlTag) {
      DomElement value = DomManager.getDomManager(element.getProject()).getDomElement((XmlTag)element);
      if (value instanceof DomSpringBean) {
        return (DomSpringBean)value;
      }
    }
    return null;
  }

}
