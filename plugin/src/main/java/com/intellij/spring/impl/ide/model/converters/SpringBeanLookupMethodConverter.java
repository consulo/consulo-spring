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
import consulo.xml.util.xml.ConvertContext;
import consulo.xml.util.xml.GenericDomValue;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

// 3.3.8.1. Lookup method injection
public class SpringBeanLookupMethodConverter extends SpringBeanMethodConverter {


  protected boolean checkModifiers(final PsiMethod method) {
    return method.hasModifierProperty(PsiModifier.PUBLIC) || method.hasModifierProperty(PsiModifier.PROTECTED);
  }

  protected boolean checkReturnType(final ConvertContext context, final PsiMethod method, final boolean forCompletion) {
    final PsiType returnType = method.getReturnType();
    if (PsiType.VOID.equals(returnType) || returnType instanceof PsiPrimitiveType) return false;

    if (forCompletion) {
      final PsiClass[] possibleReturnTypes = getValidReturnTypes(context);
      if (possibleReturnTypes.length > 0 && returnType != null) {
        for (PsiClass possibleReturnType : possibleReturnTypes) {
          final PsiClassType classType = JavaPsiFacade.getInstance(possibleReturnType.getProject()).getElementFactory().createType(possibleReturnType);
          if(classType.isAssignableFrom(returnType)) return true;
        }
        return false;
      }
    }
    return super.checkReturnType(context, method, forCompletion);
  }


  public LocalQuickFix[] getQuickFixes(final ConvertContext context) {
    final PsiClass[] validReturnTypes = getValidReturnTypes(context);
    if (validReturnTypes.length == 0) return LocalQuickFix.EMPTY_ARRAY;

    final DomSpringBean springBean = SpringConverterUtil.getCurrentBean(context);
    final GenericDomValue element = (GenericDomValue)context.getInvocationElement();
    final String elementName = element.getStringValue();
    final PsiClass psiClass = springBean.getBeanClass();

    final List<LocalQuickFix> fixes = new ArrayList<LocalQuickFix>();
    for (final PsiClass returnType : validReturnTypes) {
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
  private static String getNewMethodSignature(@Nonnull final String elementName, @Nonnull final PsiClass psiClass) {
    return "public " + psiClass.getQualifiedName() + " " + elementName + "()";
  }

  @Nonnull
  private static PsiClass[] getValidReturnTypes(final ConvertContext context) {
    final LookupMethod lookupMethod = context.getInvocationElement().getParentOfType(LookupMethod.class, false);
    if (lookupMethod != null) {
      final SpringBeanPointer beanPointer = lookupMethod.getBean().getValue();
      if (beanPointer != null) {
        return beanPointer.getEffectiveBeanType();
      }
    }
    return PsiClass.EMPTY_ARRAY;
  }
}
