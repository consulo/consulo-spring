/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopPointcut;
import com.intellij.aop.jam.AopConstants;
import com.intellij.aop.jam.AopModuleService;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import com.intellij.java.language.psi.util.PsiUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.aop.localize.AopLocalize;
import consulo.document.util.TextRange;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiRecursiveElementVisitor;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.util.lang.ref.Ref;
import consulo.xml.language.psi.XmlElement;
import jakarta.annotation.Nonnull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author peter
 */
public class AopAnnotator implements Annotator {
  @Override
  @RequiredReadAction
  public void annotate(PsiElement psiElement, AnnotationHolder holder) {
    if (((AopPointcutExpressionFile)psiElement.getContainingFile()).getAopModel().getAdvisedElementsSearcher().shouldSuppressErrors())
      return;

    PsiElement parent = psiElement.getParent();
    if (psiElement instanceof AopReferenceExpression) {
      if (checkReference(psiElement, holder, parent)) return;
    }
    if (psiElement instanceof AopParameterList && !(parent instanceof PsiExecutionExpression)) {
      checkEllipsisAllowance(psiElement, holder);
    }
    if (psiElement instanceof AopArrayExpression arrayExpr) {
      if (arrayExpr.isVarargs()) {
        if (!(parent instanceof AopReferenceHolder)
            || parent.getParent() instanceof AopTypeParameterList
            || parent.getParent() instanceof AopParameterList && parent.getParent().getParent() instanceof PsiArgsExpression) {
          holder.newError(AopLocalize.errorVarargsNotAllowedHere()).range(psiElement.getLastChild()).create();
          return;
        }
        else if (parent.getParent() instanceof AopParameterList paramList) {
          PsiElement[] parameters = paramList.getParameters();
          if (parent != parameters[parameters.length - 1]) {
            holder.newError(AopLocalize.errorVarargsNotLast()).range(psiElement.getLastChild()).create();
            return;
          }
        }
      }

      PsiPointcutExpression expression = PsiTreeUtil.getParentOfType(psiElement, PsiPointcutExpression.class);
      if (expression instanceof PsiThisExpression || expression instanceof PsiTargetExpression || expression instanceof PsiWithinExpression) {
        holder.newError(AopLocalize.errorArraysNotAllowedHere()).range(psiElement.getLastChild()).create();
      }
    }
    if (psiElement instanceof AopGenericTypeExpression genericTypeExpr) {
      PsiPointcutExpression expression = PsiTreeUtil.getParentOfType(psiElement, PsiPointcutExpression.class);
      if (expression instanceof PsiThisExpression || expression instanceof PsiTargetExpression || expression instanceof PsiWithinExpression) {
        holder.newError(AopLocalize.errorGenericsNotAllowedHere()).range(genericTypeExpr.getTypeParameterList()).create();
      }
    }

    if ((psiElement instanceof AopSubtypeExpression || psiElement instanceof AopReferenceHolder && "*".equals(psiElement.getText())) &&
      PsiTreeUtil.getParentOfType(psiElement, PsiArgsExpression.class) != null &&
      PsiTreeUtil.getParentOfType(psiElement, AopTypeParameterList.class) != null &&
      PsiTreeUtil.getParentOfType(psiElement, AopParameterList.class) != null) {
      holder.newError(AopLocalize.errorWildcardsNotAllowedHere()).range(psiElement.getLastChild()).create();
    }
    else if (psiElement instanceof PsiPointcutReferenceExpression) {
      checkPointcutArgumentCount(psiElement, holder);
    }
    else if (!(psiElement.getContainingFile().getContext() instanceof XmlElement)) {
      checkAndOrNot(psiElement, holder);
    }
  }

  @RequiredReadAction
  private static boolean checkReference(PsiElement psiElement, AnnotationHolder holder, PsiElement parent) {
    AopReferenceExpression referenceExpression = (AopReferenceExpression)psiElement;
    if (referenceExpression.getResolvability() != AopReferenceExpression.Resolvability.PLAIN) return true;

    TextRange range = referenceExpression.getRangeInElement().shiftRight(referenceExpression.getTextRange().getStartOffset());
    ResolveResult[] results = referenceExpression.multiResolve(false);
    if (results.length > 0) {
      for (ResolveResult result : results) {
        PsiElement target = result.getElement();
        if (referenceExpression.isPointcutReference()) {
          if (!(target instanceof PsiMethod method) || method.getModifierList().findAnnotation(AopConstants.POINTCUT_ANNO) == null) {
            holder.newError(AopLocalize.errorCannotResolvePointcut(referenceExpression.getReferenceName())).range(range).create();
            return true;
          }

          PsiMethod pointcutMethod = referenceExpression.getContainingFile().getAopModel().getPointcutMethod();
          if (pointcutMethod != null) {
            AopPointcut pointcut = AopModuleService.getPointcut(pointcutMethod);
            if (pointcut != null && isRecursivePointcutRef(referenceExpression, pointcut, 3)) {
              holder.newError(AopLocalize.errorRecursivePointcutReference()).range(range).create();
              return true;
            }
          }
        }

        if (referenceExpression.isAnnotationReference()) {
          boolean error;
          if (target instanceof PsiClass psiClass) {
            error = !psiClass.isAnnotationType();
          }
          else if (target instanceof PsiParameter param) {
            PsiClass psiClass = PsiUtil.resolveClassInType(param.getType());
            error = psiClass == null || !psiClass.isAnnotationType();
          }
          else {
            error = target != null;
          }

          if (error) {
            holder.newError(AopLocalize.errorAnnoExpected()).range(range).create();
            return true;
          }
        }
      }
      return true;
    }

    AopReferenceExpression qualifier = referenceExpression.getQualifier();
    if (qualifier != null && qualifier.resolve() == null) return true;
    if (qualifier == null && !(referenceExpression.getParent() instanceof AopReferenceQualifier)) {
      if (parent instanceof AopMemberReferenceExpression) return true;
    }

    LocalizeValue message;
    if (referenceExpression.isPointcutReference()) {
      message = AopLocalize.errorCannotResolvePointcut(referenceExpression.getReferenceName());
    }
    else {
      message = AopLocalize.errorCannotResolveSymbol(referenceExpression.getReferenceName());
    }
    holder.newError(message).range(range).create();
    return false;
  }

  private static boolean isRecursivePointcutRef(@Nonnull AopReferenceExpression aopReferenceExpression,
                                                @Nonnull final AopPointcut startPointcut,
                                                final int depth) {
    AopPointcut pointcut = aopReferenceExpression.resolvePointcut();
    if (pointcut == null) return false;
    if (pointcut.equals(startPointcut)) return true;
    if (depth == 0) return false;

    PsiPointcutExpression expression = pointcut.getExpression().getValue();
    final Ref<Boolean> result = Ref.create(false);
    if (expression != null) {
      expression.accept(new PsiRecursiveElementVisitor() {
        @Override
        public void visitElement(PsiElement element) {
          if (result.get()) return;
          if (element instanceof PsiPointcutExpression) super.visitElement(element);

          if (element instanceof PsiPointcutReferenceExpression) {
            PsiPointcutReferenceExpression pointcutReferenceExpression = (PsiPointcutReferenceExpression)element;
            AopReferenceExpression referenceExpression = pointcutReferenceExpression.getReferenceExpression();
            if (referenceExpression != null && isRecursivePointcutRef(referenceExpression, startPointcut, depth - 1)) {
              result.set(true);
            }
          }
        }
      });
    }
    return result.get();
  }

  @RequiredReadAction
  private static void checkEllipsisAllowance(PsiElement psiElement, AnnotationHolder holder) {
    AopParameterList list = (AopParameterList)psiElement;
    Set<PsiElement> ellipses = new HashSet<>();
    for (PsiElement parameter : list.getParameters()) {
      if (parameter.getNode().getElementType() == AopElementTypes.AOP_DOT_DOT) {
        ellipses.add(parameter);
      }
    }
    if (ellipses.size() > 1) {
      for (PsiElement ellipsis : ellipses) {
        holder.newError(AopLocalize.errorDoubleEllipsisProhibited()).range(ellipsis).create();
      }
    }
  }

  @RequiredReadAction
  private static void checkPointcutArgumentCount(PsiElement psiElement, AnnotationHolder holder) {
    PsiPointcutReferenceExpression expression = (PsiPointcutReferenceExpression)psiElement;
    AopReferenceExpression referenceExpression = expression.getReferenceExpression();
    if (referenceExpression != null) {
      AopPointcut aopPointcut = referenceExpression.resolvePointcut();
      if (aopPointcut != null) {
        int expected = aopPointcut.getParameterCount();
        if (expected >= 0) {
          AopParameterList parameterList = expression.getParameterList();
          if (parameterList != null) {
            PsiElement[] elements = parameterList.getParameters();
            int actual = elements.length;
            if (actual != expected) {
              holder.newError(AopLocalize.errorInvalidNumberOfArguments(expected, actual)).range(parameterList).create();
            }
          }
        }
      }
    }
  }

  @RequiredReadAction
  private static void checkAndOrNot(PsiElement psiElement, AnnotationHolder holder) {
    if (psiElement instanceof AopBinaryExpression) {
      PsiElement token = ((AopBinaryExpression)psiElement).getOpToken();
      if (token != null) {
        String text = token.getText();
        if ("and".equals(text) || "or".equals(text)) {
          holder.newError(AopLocalize.error0Or1Expected("&&", "||")).range(token).create();
        }
      }
    }
    else if (psiElement instanceof AopNotExpression) {
      AopNotExpression expression = (AopNotExpression)psiElement;
      PsiElement token = expression.getNotToken();
      if ("not".equals(token.getText())) {
        holder.newError(AopLocalize.error0Expected("!")).range(token).create();
      }
    }
  }
}
