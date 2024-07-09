/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopBundle;
import com.intellij.aop.ArgNamesManipulator;
import com.intellij.aop.LocalAopModel;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.java.language.psi.*;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.function.Processor;
import consulo.document.util.TextRange;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiReference;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.ReferencesSearch;
import consulo.localize.LocalizeValue;
import consulo.xml.psi.xml.XmlElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author peter
 */
@ExtensionImpl
public class ArgNamesErrorsInspection extends AbstractArgNamesInspection {
  @Nonnull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

  protected void checkAnnotation(final PsiParameter[] parameters, final ProblemsHolder holder,
                                 final ArgNamesManipulator manipulator, final PsiMethod method) {
    final String names = manipulator.getArgNames();
    if (names != null) {
      final String[] strings = names.trim().split(",");
      for (int i = 0; i < strings.length; i++) {
        strings[i] = strings[i].trim();
      }
      final List<String> actualNames = getGeneralArgumentNames(parameters);
      if (!actualNames.equals(Arrays.asList(strings))) {
        holder.registerProblem(manipulator.getArgNamesProblemElement(),
                               AopBundle.message("error.argNames.should.match", manipulator.getArgNamesAttributeName()),
                               new SetArgNamesQuickFix(AopBundle.message("quickfix.name.arg.names.correct",
                                                                                manipulator.getArgNamesAttributeName()), true, manipulator, method),
                               new SetArgNamesQuickFix(AopBundle.message("quickfix.name.arg.names.remove",
                                                                                manipulator.getArgNamesAttributeName()), false, manipulator, method));
        return;
      }
    }
    final PsiReference returningReference = manipulator.getReturningReference();
    if (returningReference != null) {
      if (method != null && returningReference.resolve() == null) {
        addAnnoReferenceProblem(holder, returningReference);
        return;
      }
    }
    final PsiReference throwingReference = manipulator.getThrowingReference();
    if (throwingReference != null) {
      final PsiElement psiElement = throwingReference.resolve();
      if (method != null && psiElement == null) {
        addAnnoReferenceProblem(holder, throwingReference);
        return;
      }
      else if (psiElement instanceof PsiParameter) {
        final PsiManager psiManager = psiElement.getManager();
        final PsiClass throwableClass = JavaPsiFacade.getInstance(psiManager.getProject())
                                                     .findClass(CommonClassNames.JAVA_LANG_THROWABLE, GlobalSearchScope.allScope(psiElement.getProject()));
        if (throwableClass != null &&
            !JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory().createType(throwableClass).isAssignableFrom(((PsiParameter)psiElement).getType())) {
          holder.registerProblem(throwingReference.getElement(), AopBundle.message("error.throwable.expected"));
          return;
        }
      }
    }

    for (final PsiParameter parameter : parameters) {
      if (!LocalAopModel.isJoinPointParamer(parameter)) {
        boolean hasRef = !ReferencesSearch.search(parameter).forEach(new Processor<PsiReference>() {
          public boolean process(final PsiReference reference) {
            return !(reference.getElement().getContainingFile() instanceof AopPointcutExpressionFile);
          }
        });
        if (!hasRef && !parameter.equals(manipulator.getReturningParameter()) && !parameter.equals(manipulator.getThrowingParameter())) {
          holder.registerProblem(manipulator.getArgNamesProblemElement(), AopBundle.message("unbound.pointcut.parameter", parameter.getName()));
        }
      }
    }
  }

  @RequiredReadAction
  private static void addAnnoReferenceProblem(final ProblemsHolder holder, final PsiReference returningReference) {
    final PsiElement element = returningReference.getElement();
    TextRange range = returningReference.getRangeInElement();
    final boolean emptyRange = range.isEmpty();
    if (emptyRange) {
      range = TextRange.from(range.getStartOffset(), 1);
    }
    LocalizeValue message = ProblemsHolder.unresolvedReferenceMessage(returningReference);
    final ProblemHighlightType highlightType = emptyRange || !(element instanceof PsiLiteralExpression || element instanceof XmlElement)
                                               ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING : ProblemHighlightType.LIKE_UNKNOWN_SYMBOL;
    holder.registerProblem(InspectionManager.getInstance(element.getProject()).createProblemDescriptor(element, range, message.get(),
                                                                                                       highlightType));
  }

  public static List<String> getGeneralArgumentNames(final PsiParameter[] parameters) {
    final List<String> actualNames = new ArrayList<String>();
    for (PsiParameter parameter : parameters) {
      actualNames.add(parameter.getName());
    }
    return actualNames;
  }

  @Nls
  @Nonnull
  public String getDisplayName() {
    return AopBundle.message("inspection.display.name.argNames.errors");
  }

  @NonNls
  @Nonnull
  public String getShortName() {
    return "ArgNamesErrorsInspection";
  }
}
