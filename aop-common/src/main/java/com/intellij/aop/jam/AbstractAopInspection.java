/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.jam;

import com.intellij.aop.LocalAopModel;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.aop.psi.AopPointcutExpressionLanguage;
import com.intellij.java.language.psi.PsiLiteralExpression;
import com.intellij.java.language.psi.PsiMethod;
import consulo.language.Language;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import consulo.xml.editor.XmlSuppressableInspectionTool;
import consulo.xml.language.psi.XmlAttributeValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public abstract class AbstractAopInspection extends XmlSuppressableInspectionTool {
    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @Nullable
    @Override
    public Language getLanguage() {
        return AopPointcutExpressionLanguage.getInstance();
    }

    @Override
    @Nonnull
    public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(final PsiElement element) {
                if (element instanceof PsiLiteralExpression || element instanceof XmlAttributeValue) {
                    checkElement(element, holder);
                }
            }
        };
    }

    protected void checkElement(final PsiElement element, final ProblemsHolder holder) {
        InjectedLanguageManager.getInstance(element.getProject()).enumerate(element, (file, places) -> {
            if (file instanceof AopPointcutExpressionFile && file.getContext() == element) {
                AopPointcutExpressionFile aopFile = (AopPointcutExpressionFile) file;
                LocalAopModel model = aopFile.getAopModel();
                PsiMethod method = model.getPointcutMethod();
                if (method != null) {
                    checkAopMethod(method, model, holder, aopFile);
                }
            }
        });
    }

    protected abstract void checkAopMethod(
        PsiMethod pointcutMethod,
        LocalAopModel model,
        ProblemsHolder holder,
        AopPointcutExpressionFile aopFile
    );

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return LocalizeValue.empty();
    }
}
