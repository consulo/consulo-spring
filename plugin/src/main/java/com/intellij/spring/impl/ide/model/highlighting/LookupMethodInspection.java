/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.LookupMethod;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.spring.localize.SpringLocalize;
import consulo.util.lang.StringUtil;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class LookupMethodInspection extends SpringBeanInspectionBase {
    private static void checkLookupMethodReturnType(
        LookupMethod lookupMethod,
        PsiMethod method,
        DomElementAnnotationHolder holder
    ) {

        PsiType returnType = method.getReturnType();
        if (returnType == null) {
            holder.createProblem(
                lookupMethod.getName(),
                SpringLocalize.springBeanLookupMethodConstructorNotAllowed().get()
            );
        }
        else if (!(returnType instanceof PsiClassType) || ((PsiClassType) returnType).resolve() == null) {
            holder.createProblem(
                lookupMethod.getName(),
                SpringLocalize.springBeanLookupMethodIncorrectReturnType().get()
            );
        }
        else {
            SpringBeanPointer beanPointer = lookupMethod.getBean().getValue();
            if (beanPointer != null) {
                PsiClass beanClass = beanPointer.getBeanClass();
                if (beanClass == null) {
                    holder.createProblem(
                        lookupMethod.getBean(),
                        SpringLocalize.springBeanLookupMethodBeanHasNoClass().get()
                    );

                }
                else {
                    PsiClass returnClass = ((PsiClassType) returnType).resolve();
                    assert returnClass != null;
                    if (!SpringUtils.isEffectiveClassType(returnType, beanPointer.getSpringBean())
                        && !InheritanceUtil.isInheritorOrSelf(beanClass, returnClass, true)) {
                        String beanName = beanPointer.getName();
                        if (StringUtil.isEmpty(beanName)) {
                            beanName = "unknown";
                        }
                        LocalizeValue message = SpringLocalize.springBeanLookupMethodReturnTypeMismatch(beanName);
                        holder.createProblem(lookupMethod.getName(), message.get());
                        holder.createProblem(lookupMethod.getBean(), message.get());
                    }
                }
            }
        }
    }

    private static void checkLookupMethodIdentifiers(
        LookupMethod lookupMethod,
        DomElementAnnotationHolder holder,
        PsiMethod method
    ) {

        if (!(method.hasModifierProperty(PsiModifier.PUBLIC) || method.hasModifierProperty(PsiModifier.PROTECTED))) {
            holder.createProblem(lookupMethod.getName(), SpringLocalize.springBeanLookupMethodMustBePublicOrProtected().get());
        }
        if (method.hasModifierProperty(PsiModifier.STATIC)) {
            holder.createProblem(lookupMethod.getName(), SpringLocalize.springBeanLookupMethodMustBeNotStatic().get());
        }
        if (method.getParameterList().getParametersCount() > 0) {
            holder.createProblem(lookupMethod.getName(), SpringLocalize.springBeanLookupMethodMustHaveNoParameters().get());
        }
    }

    protected void checkBean(
        SpringBean springBean,
        Beans beans,
        DomElementAnnotationHolder holder,
        SpringModel springModel, Object state
    ) {
        for (LookupMethod lookupMethod : springBean.getLookupMethods()) {
            PsiMethod method = lookupMethod.getName().getValue();
            if (method != null) {
                checkLookupMethodIdentifiers(lookupMethod, holder, method);
                checkLookupMethodReturnType(lookupMethod, method, holder);
            }
        }
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.springBeanLookupMethodInspection();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "SpringBeanLookupMethodInspection";
    }
}
