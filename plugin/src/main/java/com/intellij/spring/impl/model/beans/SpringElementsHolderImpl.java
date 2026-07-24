/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringElementsHolder;
import com.intellij.spring.impl.ide.model.xml.beans.SpringRef;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.xml.dom.DomUtil;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SpringElementsHolderImpl implements SpringElementsHolder {

  @Nullable
  public PsiType[] getTypesByValue() {
    //todo: this method can return PsiType[] if value type is FactoryBean which produces multiple product types 
    Project project = getManager().getProject();
    PsiManager psiManager = PsiManager.getInstance(project);
    GlobalSearchScope scope = GlobalSearchScope.allScope(project);

    PsiClass psiClass = null;

    if (DomUtil.hasXml(getValue())) {
      PsiType type = getValue().getType().getValue();
      if (type != null) {
        return new PsiType[]{type};
      }
      else {
        psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(String.class.getCanonicalName(), scope);
      }
    }
    else if (DomUtil.hasXml(getBean())) {
      PsiClass[] classes = SpringUtils.getEffectiveBeanTypes(getBean());
      if (classes.length > 0) {
        psiClass = classes[0];
      }
    }
    else if (DomUtil.hasXml(getRef())) {
      SpringRef springRef = getRef();

      SpringBeanPointer beanPointer = springRef.getBean().getValue();
      if (beanPointer != null) {
        PsiClass[] classes = beanPointer.getEffectiveBeanType();
        if (classes.length > 0) {
          psiClass = classes[0];
        }
      }
      else {
        SpringBeanPointer localPointer = springRef.getLocal().getValue();
        if (localPointer != null) {
          PsiClass[] classes = localPointer.getEffectiveBeanType();
          if (classes.length > 0) {
            psiClass = classes[0];
          }
        }
        else {
          SpringBeanPointer parentPointer = springRef.getParentAttr().getValue();
          if (parentPointer != null) {
            psiClass = parentPointer.getBeanClass();
          }
        }
      }
    }
    else if (DomUtil.hasXml(getIdref())) {
      psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(String.class.getCanonicalName(), scope);
    }
    else if (DomUtil.hasXml(getList())) {
      psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(List.class.getCanonicalName(), scope);
    }
    else if (DomUtil.hasXml(getMap())) {
      psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(Map.class.getCanonicalName(), scope);
    }
    else if (DomUtil.hasXml(getSet())) {
      psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(Set.class.getCanonicalName(), scope);
    }
    else if (DomUtil.hasXml(getProps())) {
      psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(Properties.class.getCanonicalName(), scope);
    }
    else if (DomUtil.hasXml(getNull())) {
      return new PsiType[]{PsiType.NULL};
    }

    return psiClass == null ? null : new PsiType[] {JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory().createType(psiClass)};
  }


}
