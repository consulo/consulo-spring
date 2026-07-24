/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.impl.ide;

import com.intellij.java.impl.psi.impl.beanProperties.BeanProperty;
import com.intellij.java.impl.psi.impl.beanProperties.BeanPropertyFindUsagesHandler;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.spring.impl.ide.model.converters.SpringBeanUtil;
import com.intellij.spring.impl.ide.model.highlighting.jam.SpringJavaBeanReferencesFindUsagesHandler;
import com.intellij.spring.impl.ide.model.jam.utils.SpringJamUtils;
import com.intellij.spring.impl.ide.model.properties.SpringPropertiesUtil;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import consulo.annotation.component.ExtensionImpl;
import consulo.find.FindUsagesHandler;
import consulo.find.FindUsagesHandlerFactory;
import consulo.language.psi.PsiElement;

/**
 * @author peter
 */
@ExtensionImpl
public class SpringFindUsagesHandlerFactory extends FindUsagesHandlerFactory {
  @Override
  public boolean canFindUsages(PsiElement psiElement) {
    BeanProperty property = SpringPropertiesUtil.getBeanProperty(psiElement);
    if (property != null) return true;

    if (psiElement instanceof PsiMethod && SpringJamUtils.isBean((PsiMethod)psiElement)) return true;

    return SpringBeanUtil.getTargetSpringBean(psiElement) != null;
  }

  @Override
  public FindUsagesHandler createFindUsagesHandler(PsiElement element, boolean forHighlightUsages) {
    BeanProperty property = SpringPropertiesUtil.getBeanProperty(element);
    if (property != null) {
      return new BeanPropertyFindUsagesHandler(property);
    }

    DomSpringBean bean = SpringBeanUtil.getTargetSpringBean(element);
    if (bean != null) {
      return new SpringJavaBeanReferencesFindUsagesHandler(bean);
    }
    return null;
  }
}
