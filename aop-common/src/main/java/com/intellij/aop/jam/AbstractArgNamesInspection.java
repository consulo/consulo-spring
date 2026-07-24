/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopProvider;
import com.intellij.aop.ArgNamesManipulator;
import com.intellij.aop.LocalAopModel;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import consulo.application.Application;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.util.lang.Pair;

/**
 * @author peter
 */
public abstract class AbstractArgNamesInspection extends AbstractAopInspection {
    @Override
    protected void checkAopMethod(
        PsiMethod pointcutMethod,
        LocalAopModel model,
        ProblemsHolder holder,
        AopPointcutExpressionFile aopFile
    ) {
        checkAnnotation(pointcutMethod.getParameterList().getParameters(), holder, model.getArgNamesManipulator(), pointcutMethod);
    }

    @Override
    protected void checkElement(PsiElement element, ProblemsHolder holder) {
        super.checkElement(element, holder);
        Application.get().getExtensionPoint(AopProvider.class).forEach(provider -> {
            Pair<? extends ArgNamesManipulator, PsiMethod> pair = provider.getCustomArgNamesManipulator(element);
            if (pair != null) {
                PsiMethod method = pair.second;
                checkAnnotation(method.getParameterList().getParameters(), holder, pair.first, method);
            }
        });
    }

    protected abstract void checkAnnotation(
        PsiParameter[] parameters,
        ProblemsHolder holder,
        ArgNamesManipulator manipulator,
        PsiMethod method
    );
}
