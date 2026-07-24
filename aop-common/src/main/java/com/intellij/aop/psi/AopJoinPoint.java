/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.*;
import consulo.language.psi.PsiElement;

/**
 * @author peter
 */
public abstract class AopJoinPoint<Subject extends PsiMember, Point extends PsiElement> {
  private final Subject mySubject;
  private final Point myPoint;

  protected AopJoinPoint(Subject subject, Point point) {
    mySubject = subject;
    myPoint = point;
  }

  public Subject getSubject() {
    return mySubject;
  }

  public Point getPoint() {
    return myPoint;
  }

  public static class FieldGet extends AopJoinPoint<PsiField, PsiReferenceExpression> {

    protected FieldGet(PsiField psiField, PsiReferenceExpression psiReferenceExpression) {
      super(psiField, psiReferenceExpression);
    }
  }
  public static class FieldSet extends AopJoinPoint<PsiField, PsiAssignmentExpression> {

    protected FieldSet(PsiField psiField, PsiAssignmentExpression assignmentExpression) {
      super(psiField, assignmentExpression);
    }
  }
  public static class MethodCall extends AopJoinPoint<PsiMethod, PsiCallExpression> {

    protected MethodCall(PsiMethod method, PsiCallExpression callExpression) {
      super(method, callExpression);
    }
  }
  public static class MethodExecution extends AopJoinPoint<PsiMethod, PsiMethod> {

    protected MethodExecution(PsiMethod method) {
      super(method, method);
    }
  }
  public static class ExceptionHandler extends AopJoinPoint<PsiClass, PsiCatchSection> {

    protected ExceptionHandler(PsiClass psiClass, PsiCatchSection psiCatchSection) {
      super(psiClass, psiCatchSection);
    }
  }
  public static class AdviceExecution extends AopJoinPoint<PsiMethod, PsiMethod> {

    protected AdviceExecution(PsiMethod method) {
      super(method, method);
    }
  }
  public static class InstanceInitialization extends AopJoinPoint<PsiMethod, PsiMethod> {

    protected InstanceInitialization(PsiMethod method) {
      super(method, method);
    }
  }
  public static class StaticInitialization extends AopJoinPoint<PsiClass, PsiClass> {

    protected StaticInitialization(PsiClass aClass) {
      super(aClass, aClass);
    }
  }

}
