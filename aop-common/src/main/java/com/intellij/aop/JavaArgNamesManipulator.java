/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop;

import com.intellij.aop.jam.AopAfterReturningAdviceImpl;
import com.intellij.aop.jam.AopAfterThrowingAdviceImpl;
import com.intellij.aop.jam.AopConstants;
import com.intellij.aop.jam.PointcutContainer;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.java.language.psi.PsiAnnotationMemberValue;
import com.intellij.java.language.psi.PsiParameter;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public class JavaArgNamesManipulator extends ArgNamesManipulator {
  @Nonnull
  private final PointcutContainer myContainer;

  public JavaArgNamesManipulator(@Nonnull PointcutContainer advice) {
    myContainer = advice;
  }

  @Nullable
  public String getArgNames() {
    return myContainer.getArgNames().getStringValue();
  }

  @Nonnull
  public PsiElement getArgNamesProblemElement() {
    PsiAnnotationMemberValue value = myContainer.getArgNames().getPsiElement();
    return value == null ? getCommonProblemElement() : value;
  }

  @Nonnull
  public PsiElement getCommonProblemElement() {
    return myContainer.getAnnotation().getNameReferenceElement();
  }

  @Override
  public PsiParameter getReturningParameter() {
    return myContainer instanceof AopAfterReturningAdviceImpl ? ((AopAfterReturningAdviceImpl)myContainer).getReturning().getValue() : null;

  }

  @Override
  public PsiParameter getThrowingParameter() {
    return myContainer instanceof AopAfterThrowingAdviceImpl ? ((AopAfterThrowingAdviceImpl)myContainer).getThrowing().getValue() : null;
  }

  @Nonnull
  @NonNls
  public String getArgNamesAttributeName() {
    return AopConstants.ARG_NAMES_PARAM;
  }

  @Nullable
  public PsiReference getReturningReference() {
    if (!(myContainer instanceof AopAfterReturningAdviceImpl)) return null;

    JamStringAttributeElement<PsiParameter> returning = ((AopAfterReturningAdviceImpl)myContainer).getReturning();
    PsiReference[] references = returning.getConverter().createReferences(returning);
    return references.length == 0 ? null : references[0];
  }

  @Nullable
  public PsiReference getThrowingReference() {
    if (!(myContainer instanceof AopAfterThrowingAdviceImpl)) return null;

    JamStringAttributeElement<PsiParameter> throwing = ((AopAfterThrowingAdviceImpl)myContainer).getThrowing();
    PsiReference[] references = throwing.getConverter().createReferences(throwing);
    return references.length == 0 ? null : references[0];

  }

  public AopAdviceType getAdviceType() {
    return myContainer instanceof AopAdvice ? ((AopAdvice)myContainer).getAdviceType() : null;
  }

  public void setArgNames(@Nullable String argNames) throws IncorrectOperationException
  {
    myContainer.getArgNames().setStringValue(argNames);
  }
}
