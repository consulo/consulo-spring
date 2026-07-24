package com.intellij.spring.impl.ide.model.highlighting.jam;

import com.intellij.java.language.codeInsight.AnnotationUtil;
import com.intellij.java.language.psi.*;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.constants.SpringAnnotationsConstants;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJamElement;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJavaBean;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.spring.localize.SpringLocalize;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class SpringJavaConfigInconsistencyInspection extends SpringJavaConfigInspectionBase {
    @Override
    @RequiredReadAction
    protected void checkClass(PsiClass aClass, ProblemsHolder holder, @Nonnull Module module) {
        SpringJamElement configuration = getJavaConfiguration(aClass, module);

        if (configuration != null) {
            checkJavaConfiguration(configuration, module, holder);
        }
        else {
            SpringModel model = SpringManager.getInstance(module.getProject()).getModel(module);
            if (model != null && model.isImplicitConfiguration(aClass)) {
                return;
            }

            for (PsiMethod psiMethod : aClass.getMethods()) {
                PsiAnnotation beanAnnotation = AnnotationUtil.findAnnotation(psiMethod, SpringAnnotationsConstants.SPRING_BEAN_ANNOTATION);
                if (beanAnnotation != null) {
                    holder.newProblem(SpringLocalize.javaConfigBeanMustBeDeclaredInsideConfiguration())
                        .range(beanAnnotation)
                        .create();
                }
            }
        }
    }

    @Override
    protected void checkJavaConfiguration(SpringJamElement javaConfiguration, Module module, ProblemsHolder holder) {
        checkJavaConfigurationClass(javaConfiguration, holder);

        for (SpringJavaBean springJavaBean : javaConfiguration.getBeans()) {
            checkJavaBeanInconsistency(springJavaBean, holder);
        }
    }

    private static void checkJavaConfigurationClass(SpringJamElement configuration, ProblemsHolder holder) {
        PsiClass psiClass = configuration.getPsiElement();

        checkConstructor(psiClass, configuration, holder);
        checkNonFinal(configuration, holder, psiClass);
    }

    private static void checkNonFinal(SpringJamElement configuration, ProblemsHolder holder, PsiClass psiClass) {
        if (psiClass.getModifierList().hasModifierProperty(PsiModifier.FINAL)) {
            holder.newProblem(SpringLocalize.javaConfigurationCannotBeFinal())
                .range(configuration.getAnnotation())
                .create();
        }
    }

    private static void checkConstructor(PsiClass psiClass, SpringJamElement configuration, ProblemsHolder holder) {
        PsiMethod[] constructors = psiClass.getConstructors();

        if (constructors.length != 0 && !hasDefaultConstructor(constructors)) {
            holder.newProblem(SpringLocalize.javaConfigurationMustHaveDefaultConstructor())
                .range(configuration.getAnnotation())
                .create();
        }
        for (PsiMethod constructor : constructors) {
            if (AnnotationUtil.isAnnotated(constructor, SpringAnnotationsConstants.AUTOWIRED_ANNOTATION, 0)) {
                holder.newProblem(SpringLocalize.javaConfigurationAutowiredConstructorParam())
                    .range(constructor.getNameIdentifier())
                    .create();
            }
        }
    }

    private static boolean hasDefaultConstructor(PsiMethod[] constructors) {
        for (PsiMethod constructor : constructors) {
            if (constructor.hasModifierProperty(PsiModifier.PUBLIC) && constructor.getParameterList().getParametersCount() == 0) {
                return true;
            }
        }
        return false;
    }

    private static void checkJavaBeanInconsistency(SpringJavaBean springJavaBean, ProblemsHolder holder) {
        checkReturnType(springJavaBean, holder);
        checkNonFinal(springJavaBean, holder);
        checkNonPrivate(springJavaBean, holder);
    }

    private static void checkNonPrivate(SpringJavaBean springJavaBean, ProblemsHolder holder) {
        if (springJavaBean.getPsiElement().getModifierList().hasExplicitModifier(PsiModifier.PRIVATE)) {
            holder.newProblem(SpringLocalize.javaConfigBeanMethodCannotBePrivate())
                .range(springJavaBean.getPsiAnnotation())
                .create();
        }
    }

    private static void checkNonFinal(SpringJavaBean springJavaBean, ProblemsHolder holder) {
        if (springJavaBean.getPsiElement().getModifierList().hasExplicitModifier(PsiModifier.FINAL)) {
            holder.newProblem(SpringLocalize.javaConfigBeanMethodCannotBeFinal())
                .range(springJavaBean.getPsiAnnotation())
                .create();
        }
    }

    private static void checkReturnType(SpringJavaBean springJavaBean, ProblemsHolder holder) {
        if (springJavaBean.getPsiElement().getReturnType().equals(PsiType.VOID)) {
            holder.newProblem(SpringLocalize.javaConfigBeanMethodCannotReturnVoid())
                .range(springJavaBean.getPsiAnnotation())
                .create();
        }
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.springJavaConfigurationInconsistencyInspectionName();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "SpringJavaConfigInconsistencyInspection";
    }

    @Nonnull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }
}