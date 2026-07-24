/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.jam;

import com.intellij.aop.AopAdviceType;
import com.intellij.aop.AopBundle;
import com.intellij.aop.LocalAopModel;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.aop.localize.AopLocalize;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
@ExtensionImpl
public class AroundAdviceStyleInspection extends AbstractAopInspection {
    private static final Logger LOG = Logger.getInstance(AroundAdviceStyleInspection.class);

    protected void checkAopMethod(
        final PsiMethod pointcutMethod,
        LocalAopModel model,
        ProblemsHolder holder,
        AopPointcutExpressionFile aopFile
    ) {
        if (model.getArgNamesManipulator().getAdviceType() != AopAdviceType.AROUND) {
            return;
        }

        final PsiElementFactory factory = JavaPsiFacade.getInstance(pointcutMethod.getProject()).getElementFactory();

        if (PsiType.VOID.equals(pointcutMethod.getReturnType()) && pointcutMethod.getReturnTypeElement() != null) {
            holder.newProblem(AopLocalize.aroundAdviceShouldReturnSomething())
                .range(model.getArgNamesManipulator().getCommonProblemElement())
                .withFix(new LocalQuickFix() {
                    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
                        if (!pointcutMethod.isValid()) {
                            return;
                        }

                        try {
                            PsiClassType object =
                                PsiType.getJavaLangObject(pointcutMethod.getManager(), pointcutMethod.getResolveScope());
                            pointcutMethod.getReturnTypeElement().replace(factory.createTypeElement(object));
                        }
                        catch (IncorrectOperationException e) {
                            LOG.error(e);
                        }
                    }

                    @Nonnull
                    @Override
                    public LocalizeValue getName() {
                        return AopLocalize.changeReturnTypeToObject();
                    }
                })
                .create();
            return;
        }

        PsiParameter[] parameters = pointcutMethod.getParameterList().getParameters();
        if (parameters.length == 0 || !parameters[0].getType().equalsToText(AopConstants.PROCEEDING_JOIN_POINT)) {
            holder.newProblem(AopLocalize.aroundAdviceCallCannotProceed())
                .range(model.getArgNamesManipulator().getCommonProblemElement())
                .withFix(new LocalQuickFix() {
                    @Nonnull
                    public LocalizeValue getName() {
                        return AopLocalize.addPjpParameter();
                    }

                    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
                        if (!pointcutMethod.isValid()) {
                            return;
                        }

                        try {
                            PsiParameter newParameter = factory.createParameter(
                                "pjp",
                                factory.createTypeFromText(AopConstants.PROCEEDING_JOIN_POINT, pointcutMethod)
                            );
                            PsiParameterList list = pointcutMethod.getParameterList();
                            if (list.getParametersCount() == 0) {
                                list.add(newParameter);
                            }
                            else {
                                list.addBefore(newParameter, list.getParameters()[0]);
                            }
                        }
                        catch (IncorrectOperationException e) {
                            LOG.error(e);
                        }
                    }
                })
                .create();
        }
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return AopLocalize.aroundAdviceStyleInspection();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "AroundAdviceStyleInspection";
    }
}