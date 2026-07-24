/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.jam;

import com.intellij.aop.LocalAopModel;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.aop.psi.PsiIfPointcutExpression;
import com.intellij.aop.psi.PsiPointcutExpression;
import com.intellij.java.language.psi.PsiCodeBlock;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiType;
import consulo.annotation.component.ExtensionImpl;
import consulo.aop.localize.AopLocalize;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
@ExtensionImpl
public class PointcutMethodStyleInspection extends AbstractAopInspection {
    @Override
    protected void checkAopMethod(
        PsiMethod pointcutMethod,
        LocalAopModel model,
        ProblemsHolder holder,
        AopPointcutExpressionFile aopFile
    ) {
        if (model.getArgNamesManipulator().getAdviceType() != null) {
            return;
        }

        PsiElement problemElement = model.getArgNamesManipulator().getCommonProblemElement();
        if (pointcutMethod.getReturnType() != PsiType.VOID) {
            PsiPointcutExpression expression = aopFile.getPointcutExpression();
            if (expression instanceof PsiIfPointcutExpression && PsiType.BOOLEAN.equals(pointcutMethod.getReturnType())) {
                return;
            }

            holder.newProblem(AopLocalize.pointcutMethodsShouldHaveVoidReturnType())
                .range(problemElement)
                .create();
            return;
        }

        if (pointcutMethod.getThrowsList().getTextLength() > 0) {
            holder.newProblem(AopLocalize.pointcutMethodsShouldHaveNoThrowsClause())
                .range(problemElement)
                .create();
            return;
        }

        PsiCodeBlock body = pointcutMethod.getBody();
        if (body != null && body.getStatements().length > 0) {
            holder.newProblem(AopLocalize.pointcutMethodsShouldHaveEmptyBody())
                .range(problemElement)
                .create();
        }
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return AopLocalize.pointcutMethodStyleInspection();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "PointcutMethodStyleInspection";
    }
}
