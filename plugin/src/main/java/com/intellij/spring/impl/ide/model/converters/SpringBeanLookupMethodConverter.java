/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.impl.codeInsight.daemon.impl.quickfix.CreateMethodQuickFix;
import com.intellij.java.language.psi.*;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.LookupMethod;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.xml.dom.ConvertContext;
import consulo.xml.dom.GenericDomValue;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

// 3.3.8.1. Lookup method injection
public class SpringBeanLookupMethodConverter extends SpringBeanMethodConverter {


  protected boolean checkModifiers(PsiMethod method) {
    return method.hasModifierProperty(PsiModifier.PUBLIC) || method.hasModifierProperty(PsiModifier.PROTECTED);
  }

  protected boolean checkReturnType(ConvertContext context, PsiMethod method, boolean forCompletion) {
    PsiType returnType = method.getReturnType();
    if (PsiType.VOID.equals(returnType) || returnType instanceof PsiPrimitiveType) return false;

    if (forCompletion) {
      PsiClass[] possibleReturnTypes = getValidReturnTypes(context);
      if (possibleReturnTypes.length > 0 && returnType != null) {
        for (PsiClass possibleReturnType : possibleReturnTypes) {
          PsiClassType classType = JavaPsiFacade.getInstance(possibleReturnType.getProject()).getElementFactory().createType(possibleReturnType);
          if(classType.isAssignableFrom(returnType)) return true;
        }
        return false;
      }
    }
    return super.checkReturnType(context, method, forCompletion);
  }


  public LocalQuickFix[] getQuickFixes(ConvertContext context) {
    PsiClass[] validReturnTypes = getValidReturnTypes(context);
    if (validReturnTypes.length == 0) return LocalQuickFix.EMPTY_ARRAY;

    DomSpringBean springBean = SpringConverterUtil.getCurrentBean(context);
    GenericDomValue element = (GenericDomValue)context.getInvocationElement();
    String elementName = element.getStringValue();
    PsiClass psiClass = springBean.getBeanClass();

    List<LocalQuickFix> fixes = new ArrayList<LocalQuickFix>();
    for (PsiClass returnType : validReturnTypes) {
      if(elementName != null && elementName.length() > 0) {
        CreateMethodQuickFix fix =
          CreateMethodQuickFix.createFix(psiClass, getNewMethodSignature(elementName, returnType), getNewMethodBody());
        if (fix != null) {
          fixes.add(fix);
        }
      }
    }

    return fixes.toArray(new LocalQuickFix[fixes.size()]);
  }

  @NonNls
  private static String getNewMethodBody() {
    return "return null;";
  }

  @NonNls
  private static String getNewMethodSignature(@Nonnull String elementName, @Nonnull PsiClass psiClass) {
    return "public " + psiClass.getQualifiedName() + " " + elementName + "()";
  }

  @Nonnull
  private static PsiClass[] getValidReturnTypes(ConvertContext context) {
    LookupMethod lookupMethod = context.getInvocationElement().getParentOfType(LookupMethod.class, false);
    if (lookupMethod != null) {
      SpringBeanPointer beanPointer = lookupMethod.getBean().getValue();
      if (beanPointer != null) {
        return beanPointer.getEffectiveBeanType();
      }
    }
    return PsiClass.EMPTY_ARRAY;
  }
}
