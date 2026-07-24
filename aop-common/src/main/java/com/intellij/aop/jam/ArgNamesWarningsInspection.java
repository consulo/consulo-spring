/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopAdviceType;
import com.intellij.aop.ArgNamesManipulator;
import com.intellij.aop.LocalAopModel;
import com.intellij.aop.psi.PsiThisExpression;
import com.intellij.aop.psi.*;
import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.aop.localize.AopLocalize;
import consulo.application.util.function.Processor;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiReference;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.function.Condition;
import jakarta.annotation.Nonnull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author peter
 */
@ExtensionImpl
public class ArgNamesWarningsInspection extends AbstractArgNamesInspection {
    @Nonnull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    protected void checkAnnotation(
        PsiParameter[] parameters,
        ProblemsHolder holder,
        ArgNamesManipulator manipulator,
        PsiMethod method
    ) {
        if (manipulator.getArgNames() == null) {
            if (!canInferParameters(parameters, manipulator, method)) {
                holder.newProblem(AopLocalize.warningArgnamesShouldBeDefined(manipulator.getArgNamesAttributeName()))
                    .range(manipulator.getArgNamesProblemElement())
                    .withFix(new SetArgNamesQuickFix(
                        AopLocalize.quickfixNameDefineAttribute(manipulator.getArgNamesAttributeName()),
                        true,
                        manipulator,
                        method
                    ))
                    .create();
                return;
            }
        }

        AopAdviceType adviceType = manipulator.getAdviceType();
        if (parameters.length > 0 && adviceType != null && adviceType != AopAdviceType.AROUND) {
            if (parameters[0].getType().equalsToText(AopConstants.PROCEEDING_JOIN_POINT)) {
                holder.newProblem(AopLocalize.errorPjpNotAllowed())
                    .range(manipulator.getArgNamesProblemElement())
                    .create();
            }
        }
    }

    private static boolean canInferParameters(PsiParameter[] parameters, ArgNamesManipulator manipulator, PsiMethod method) {
        if (parameters.length == 0) {
            return true;
        }

        Set<PsiParameter> set = new HashSet<>(Set.of(parameters));
        if (LocalAopModel.isJoinPointParamer(parameters[0])) {
            set.remove(parameters[0]);
            if (set.isEmpty()) {
                return true;
            }
        }

        if (manipulator.getThrowingReference() != null && containsOnlyOneParameter(method, set, CommonClassNames.JAVA_LANG_THROWABLE)) {
            return true;
        }

        Class<PsiAtPointcutDesignator> designatorClass = PsiAtPointcutDesignator.class;
        List<PsiParameter> shouldBeAnnos = findParametersUsedInPointcuts(set, designatorClass);
        if (shouldBeAnnos.size() == 1 && containsOnlyOneParameter(method, set, CommonClassNames.JAVA_LANG_ANNOTATION_ANNOTATION)) {
            return true;
        }

        if (manipulator.getReturningReference() != null) {
            return set.size() == 1;
        }

        List<PsiParameter> canBePrimitive = findParametersUsedInPointcuts(set, PsiArgsExpression.class);
        if (canBePrimitive.size() == 1) {
            List<PsiParameter> primitives = ContainerUtil.findAll(set, new Condition<PsiParameter>() {
                public boolean value(PsiParameter psiParameter) {
                    return psiParameter.getType() instanceof PsiPrimitiveType;
                }
            });
            if (primitives.size() == 1) {
                set.removeAll(primitives);

                if (set.isEmpty()) {
                    return true;
                }
            }
        }

        if (set.size() == 1) {
            if (canBePrimitive.size() == 1) {
                return true;
            }
            if (findParametersUsedInPointcuts(set, PsiThisExpression.class).size() == 1) {
                return true;
            }
            if (findParametersUsedInPointcuts(set, PsiTargetExpression.class).size() == 1) {
                return true;
            }
        }

        return false;
    }

    private static List<PsiParameter> findParametersUsedInPointcuts(Set<PsiParameter> set, final Class<?> designatorClass) {
        return ContainerUtil.findAll(set, new Condition<PsiParameter>() {
            public boolean value(PsiParameter psiParameter) {
                return !ReferencesSearch.search(psiParameter).forEach(new Processor<PsiReference>() {
                    public boolean process(PsiReference reference) {
                        if (reference instanceof AopReferenceExpression) {
                            if (designatorClass.isInstance(PsiTreeUtil.getParentOfType(
                                (AopReferenceExpression) reference,
                                PsiPointcutExpression.class
                            ))) {
                                return false;
                            }
                        }
                        return true;
                    }
                });
            }
        });
    }

    private static boolean containsOnlyOneParameter(PsiMethod method, Set<PsiParameter> set, String className) {
        final PsiClassType baseType = JavaPsiFacade.getInstance(method.getManager().getProject()).getElementFactory()
            .createTypeByFQClassName(className, method.getResolveScope());
        List<PsiParameter> instanceofs = ContainerUtil.findAll(set, new Condition<PsiParameter>() {
            public boolean value(PsiParameter psiParameter) {
                return baseType.isAssignableFrom(psiParameter.getType());
            }
        });
        if (instanceofs.size() == 1) {
            set.removeAll(instanceofs);
            if (set.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return AopLocalize.inspectionDisplayNameArgnamesWarnings();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "ArgNamesWarningsInspection";
    }
}