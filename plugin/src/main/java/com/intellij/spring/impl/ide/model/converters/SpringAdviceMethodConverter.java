/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.converters;

import com.intellij.aop.AopAdviceType;
import com.intellij.java.impl.codeInsight.daemon.impl.quickfix.CreateMethodQuickFix;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.spring.impl.ide.model.xml.aop.BasicAdvice;
import com.intellij.spring.impl.ide.model.xml.aop.SpringAspect;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.localize.LocalizeValue;
import consulo.xml.dom.ConvertContext;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.GenericDomValue;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public class SpringAdviceMethodConverter extends SpringBeanMethodConverter{
  protected boolean checkParameterList(PsiMethod method) {
    return true;
  }

  @Nonnull
  @Override
  public LocalizeValue buildUnresolvedMessage(@Nullable String s, ConvertContext context) {
    if (getPsiClass(context) == null) return LocalizeValue.empty();
    return super.buildUnresolvedMessage(s, context);
  }

  @Nonnull
  public PsiReference[] createReferences(GenericDomValue<PsiMethod> genericDomValue, PsiElement element, ConvertContext context) {
    if (getPsiClass(context) == null) return PsiReference.EMPTY_ARRAY;

    return super.createReferences(genericDomValue, element, context);
  }


  @Nullable
  protected PsiClass getPsiClass(ConvertContext context) {
    SpringAspect aspect = context.getInvocationElement().getParentOfType(SpringAspect.class, false);
    if (aspect != null) {
      SpringBeanPointer pointer = aspect.getRef().getValue();
      if (pointer != null) {
        return pointer.getBeanClass();
      }
    }
    return null;
  }

  public LocalQuickFix[] getQuickFixes(ConvertContext context) {
    GenericDomValue element = (GenericDomValue)context.getInvocationElement();
    String elementName = element.getStringValue();
    DomElement parent = element.getParent();
    PsiClass psiClass = getPsiClass(context);
    if (psiClass != null && elementName != null && parent instanceof BasicAdvice) {
      boolean isAround = ((BasicAdvice)parent).getAdviceType() == AopAdviceType.AROUND;
      @NonNls String signature = isAround ?
                                 "public Object " + elementName + "(org.aspectj.lang.ProceedingJoinPoint pjp)" :
                                 "public void " + elementName + "(org.aspectj.lang.JoinPoint jp)";
      signature += " throws java.lang.Throwable";
      @NonNls String body = isAround ? "return pjp.proceed();" : "";
      CreateMethodQuickFix fix = CreateMethodQuickFix.createFix(psiClass, signature, body);
      return fix == null ? LocalQuickFix.EMPTY_ARRAY : new LocalQuickFix[]{fix};
    }

    return super.getQuickFixes(context);
  }
}
