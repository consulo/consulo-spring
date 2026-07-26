/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.AopProvider;
import com.intellij.aop.jam.AopConstants;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiJavaFile;
import com.intellij.java.language.psi.PsiModifierList;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.impl.ide.aop.SpringAopProvider;
import com.intellij.spring.impl.ide.constants.SpringConstants;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.application.util.NotNullLazyValue;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.dom.DomFileElement;
import jakarta.annotation.Nonnull;

import java.util.List;

/**
 * @author peter
 */
@ExtensionImpl
public class MissingAspectjAutoproxyInspection extends AOPLocalInspectionTool {
    private final NotNullLazyValue<SpringAopProvider> mySpringAopProvider = new NotNullLazyValue<>() {
        @Nonnull
        @Override
        protected SpringAopProvider compute() {
            SpringAopProvider provider = Application.get().getExtensionPoint(AopProvider.class).findExtension(SpringAopProvider.class);
            if (provider != null) {
                return provider;
            }
            throw new AssertionError();
        }
    };

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    @RequiredReadAction
    public ProblemDescriptor[] checkFile(@Nonnull PsiFile file, @Nonnull InspectionManager manager, boolean isOnTheFly) {
        if (file instanceof PsiJavaFile javaFile) {
            Module module = file.getModule();
            if (module == null || SpringUtils.isSpring25(module)) {
                return null;
            }

            for (PsiClass aClass : javaFile.getClasses()) {
                PsiModifierList modifierList = aClass.getModifierList();
                if (modifierList != null) {
                    PsiAnnotation annotation = modifierList.findAnnotation(AopConstants.ASPECT_ANNO);
                    if (annotation != null) {
                        AopAdvisedElementsSearcher searcher = mySpringAopProvider.getValue().getAdvisedElementsSearcher(aClass);
                        if (searcher instanceof SpringAdvisedElementsSearcher advisedElementsSearcher) {
                            List<SpringModel> models = advisedElementsSearcher.getSpringModels();
                            if (!models.isEmpty() && !isAspectJSupportEnabled(models)) {
                                return new ProblemDescriptor[]{
                                    manager.newProblemDescriptor(SpringLocalize.aopWarningAspectjIsntEnabled())
                                        .range(annotation.getNameReferenceElement())
                                        .withOptionalFix(models.isEmpty() ? null : new EnableAspectJQuickFix(models.get(0)))
                                        .create()};
                            }
                        }
                    }
                }
            }
        }

        return super.checkFile(file, manager, isOnTheFly);
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.missingAspectjAutoproxyInspectionDisplayName();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "MissingAspectjAutoproxyInspection";
    }

    public static boolean isAspectJSupportEnabled(List<SpringModel> models) {
        for (SpringModel model : models) {
            for (DomFileElement<Beans> fileElement : model.getRoots()) {
                for (CommonSpringBean springBean : SpringUtils.getChildBeans(fileElement.getRootElement(), false)) {
                    PsiClass beanClass = springBean.getBeanClass();
                    if (beanClass != null && SpringConstants.ASPECTJ_AUTOPROXY_BEAN_CLASS.equals(beanClass.getQualifiedName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
