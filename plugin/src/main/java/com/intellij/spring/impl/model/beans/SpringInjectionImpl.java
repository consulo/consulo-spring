/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.java.language.psi.CommonClassNames;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringInjection;
import com.intellij.spring.impl.ide.model.xml.beans.SpringValue;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.xml.dom.DomUtil;
import consulo.xml.dom.GenericDomValue;

import jakarta.annotation.Nullable;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SpringInjectionImpl extends SpringValueHolderImpl implements SpringInjection {

  @Nullable
  public PsiType[] getTypesByValue() {

    Project project = getManager().getProject();
    PsiManager psiManager = PsiManager.getInstance(project);
    GlobalSearchScope scope = GlobalSearchScope.allScope(project);

    SpringBeanPointer refAttrPointer = getRefAttr().getValue();
    PsiClass psiClass;

    if (DomUtil.hasXml(getValueAttr())) {
      psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(CommonClassNames.JAVA_LANG_STRING, scope);
      return psiClass == null ? null : new PsiType[]{JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory().createType(
        psiClass)};
    }
    else if (refAttrPointer != null) {
      PsiClass[] classes = refAttrPointer.getEffectiveBeanType();
      return ContainerUtil.map2Array(classes,
                                     PsiType.class,
                                     psiClass1 -> JavaPsiFacade.getInstance(psiManager.getProject())
                                                               .getElementFactory()
                                                               .createType(psiClass1));
    }
    else {
      return super.getTypesByValue();
    }
  }

  public GenericDomValue<?> getValueElement() {
    SpringValue springValue = getValue();
    if (!DomUtil.hasXml(springValue)) return getValueAttr();
    return springValue;
  }

}
