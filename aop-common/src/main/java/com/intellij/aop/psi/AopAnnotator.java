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
  public void annotate(final PsiElement psiElement, final AnnotationHolder holder) {
    if (((AopPointcutExpressionFile)psiElement.getContainingFile()).getAopModel().getAdvisedElementsSearcher().shouldSuppressErrors())
      return;

    final PsiElement parent = psiElement.getParent();
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

      final PsiPointcutExpression expression = PsiTreeUtil.getParentOfType(psiElement, PsiPointcutExpression.class);
      if (expression instanceof PsiThisExpression || expression instanceof PsiTargetExpression || expression instanceof PsiWithinExpression) {
        holder.newError(AopLocalize.errorArraysNotAllowedHere()).range(psiElement.getLastChild()).create();
      }
    }
    if (psiElement instanceof AopGenericTypeExpression genericTypeExpr) {
      final PsiPointcutExpression expression = PsiTreeUtil.getParentOfType(psiElement, PsiPointcutExpression.class);
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
  private static boolean checkReference(final PsiElement psiElement, final AnnotationHolder holder, final PsiElement parent) {
    final AopReferenceExpression referenceExpression = (AopReferenceExpression)psiElement;
    if (referenceExpression.getResolvability() != AopReferenceExpression.Resolvability.PLAIN) return true;

    final TextRange range = referenceExpression.getRangeInElement().shiftRight(referenceExpression.getTextRange().getStartOffset());
    final ResolveResult[] results = referenceExpression.multiResolve(false);
    if (results.length > 0) {
      for (final ResolveResult result : results) {
        final PsiElement target = result.getElement();
        if (referenceExpression.isPointcutReference()) {
          if (!(target instanceof PsiMethod method) || method.getModifierList().findAnnotation(AopConstants.POINTCUT_ANNO) == null) {
            holder.newError(AopLocalize.errorCannotResolvePointcut(referenceExpression.getReferenceName())).range(range).create();
            return true;
          }

          final PsiMethod pointcutMethod = referenceExpression.getContainingFile().getAopModel().getPointcutMethod();
          if (pointcutMethod != null) {
            final AopPointcut pointcut = AopModuleService.getPointcut(pointcutMethod);
            if (pointcut != null && isRecursivePointcutRef(referenceExpression, pointcut, 3)) {
              holder.newError(AopLocalize.errorRecursivePointcutReference()).range(range).create();
              return true;
            }
          }
        }

        if (referenceExpression.isAnnotationReference()) {
          final boolean error;
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

    final AopReferenceExpression qualifier = referenceExpression.getQualifier();
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

  private static boolean isRecursivePointcutRef(@Nonnull final AopReferenceExpression aopReferenceExpression,
                                                @Nonnull final AopPointcut startPointcut,
                                                final int depth) {
    final AopPointcut pointcut = aopReferenceExpression.resolvePointcut();
    if (pointcut == null) return false;
    if (pointcut.equals(startPointcut)) return true;
    if (depth == 0) return false;

    final PsiPointcutExpression expression = pointcut.getExpression().getValue();
    final Ref<Boolean> result = Ref.create(false);
    if (expression != null) {
      expression.accept(new PsiRecursiveElementVisitor() {
        @Override
        public void visitElement(final PsiElement element) {
          if (result.get()) return;
          if (element instanceof PsiPointcutExpression) super.visitElement(element);

          if (element instanceof PsiPointcutReferenceExpression) {
            final PsiPointcutReferenceExpression pointcutReferenceExpression = (PsiPointcutReferenceExpression)element;
            final AopReferenceExpression referenceExpression = pointcutReferenceExpression.getReferenceExpression();
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
  private static void checkEllipsisAllowance(final PsiElement psiElement, final AnnotationHolder holder) {
    final AopParameterList list = (AopParameterList)psiElement;
    Set<PsiElement> ellipses = new HashSet<>();
    for (final PsiElement parameter : list.getParameters()) {
      if (parameter.getNode().getElementType() == AopElementTypes.AOP_DOT_DOT) {
        ellipses.add(parameter);
      }
    }
    if (ellipses.size() > 1) {
      for (final PsiElement ellipsis : ellipses) {
        holder.newError(AopLocalize.errorDoubleEllipsisProhibited()).range(ellipsis).create();
      }
    }
  }

  @RequiredReadAction
  private static void checkPointcutArgumentCount(final PsiElement psiElement, final AnnotationHolder holder) {
    final PsiPointcutReferenceExpression expression = (PsiPointcutReferenceExpression)psiElement;
    final AopReferenceExpression referenceExpression = expression.getReferenceExpression();
    if (referenceExpression != null) {
      final AopPointcut aopPointcut = referenceExpression.resolvePointcut();
      if (aopPointcut != null) {
        final int expected = aopPointcut.getParameterCount();
        if (expected >= 0) {
          final AopParameterList parameterList = expression.getParameterList();
          if (parameterList != null) {
            final PsiElement[] elements = parameterList.getParameters();
            final int actual = elements.length;
            if (actual != expected) {
              holder.newError(AopLocalize.errorInvalidNumberOfArguments(expected, actual)).range(parameterList).create();
            }
          }
        }
      }
    }
  }

  @RequiredReadAction
  private static void checkAndOrNot(final PsiElement psiElement, final AnnotationHolder holder) {
    if (psiElement instanceof AopBinaryExpression) {
      final PsiElement token = ((AopBinaryExpression)psiElement).getOpToken();
      if (token != null) {
        String text = token.getText();
        if ("and".equals(text) || "or".equals(text)) {
          holder.newError(AopLocalize.error0Or1Expected("&&", "||")).range(token).create();
        }
      }
    }
    else if (psiElement instanceof AopNotExpression) {
      final AopNotExpression expression = (AopNotExpression)psiElement;
      final PsiElement token = expression.getNotToken();
      if ("not".equals(token.getText())) {
        holder.newError(AopLocalize.error0Expected("!")).range(token).create();
      }
    }
  }
}
