/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.java.language.psi.PsiMethod;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.converters.SpringBeanFactoryMethodConverter;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import jakarta.annotation.Nonnull;

/**
 * @author Dmitry Avdeev
 */
@ExtensionImpl
public class FactoryMethodInspection extends SpringBeanInspectionBase {
    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.modelInspectionBeanFactoryMethod();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "SpringFactoryMethodInspection";
    }

    protected void checkBean(
        SpringBean springBean,
        Beans beans,
        DomElementAnnotationHolder holder,
        SpringModel model,
        Object state
    ) {
        PsiMethod factoryMethod = springBean.getFactoryMethod().getValue();
        if (factoryMethod != null) {
            if (!SpringBeanFactoryMethodConverter.isPublic(factoryMethod)) {
                holder.createProblem(springBean.getFactoryMethod(), SpringLocalize.methodMustBePublic(factoryMethod.getName()).get());
            }
            boolean isStatic = SpringBeanFactoryMethodConverter.isStatic(factoryMethod);
            SpringBeanPointer factoryBean = springBean.getFactoryBean().getValue();
            if (!isStatic && factoryBean == null) {
                holder.createProblem(springBean.getFactoryMethod(), SpringLocalize.methodMustBeStatic(factoryMethod.getName()).get());
            }
            else if (isStatic && factoryBean != null) {
                holder.createProblem(
                    springBean.getFactoryMethod(),
                    SpringLocalize.methodMustNotBeStatic(factoryMethod.getName()).get()
                );
            }
            if (!SpringBeanFactoryMethodConverter.isProperReturnType(factoryMethod)) {
                holder.createProblem(
                    springBean.getFactoryMethod(),
                    SpringLocalize.methodCannotInstantiateBean(factoryMethod.getName()).get()
                );
            }
        }
    }
}
