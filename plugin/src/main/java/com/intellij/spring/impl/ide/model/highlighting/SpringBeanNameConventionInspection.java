/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiNameHelper;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.values.converters.FieldRetrievingFactoryBeanConverter;
import com.intellij.spring.impl.ide.model.xml.beans.Alias;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.localize.LocalizeValue;
import consulo.spring.localize.SpringLocalize;
import consulo.util.lang.StringUtil;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class SpringBeanNameConventionInspection extends SpringBeanInspectionBase {
    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.modelInspectionDisplayBeanNameConvention();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "SpringBeanNameConventionInspection";
    }

    protected void checkBean(
        SpringBean springBean,
        Beans beans,
        DomElementAnnotationHolder holder,
        SpringModel springModel,
        Object state
    ) {
        String beanId = springBean.getId().getStringValue();

        if (acceptBean(springBean, beanId)) {
            checkName(springBean.getId(), beanId, holder);
        }
    }

    private static boolean acceptBean(SpringBean springBean, String beanId) {
        return !StringUtil.isEmpty(beanId) && (!FieldRetrievingFactoryBeanConverter.isFieldRetrivingFactoryBean(springBean) ||
            !FieldRetrievingFactoryBeanConverter.isResolved(springBean.getManager().getProject(), beanId));

    }

    @Nonnull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    private static void checkAlias(Alias alias, DomElementAnnotationHolder holder) {
        String aliasName = alias.getAlias().getStringValue();

        checkName(alias.getAlias(), aliasName, holder);
    }

    private static void checkName(DomElement domElement, @Nonnull String name, DomElementAnnotationHolder holder) {
        PsiNameHelper psiNameHelper = JavaPsiFacade.getInstance(domElement.getManager().getProject()).getNameHelper();
        boolean identifier = psiNameHelper.isIdentifier(name);
        if (!identifier) {
            boolean keyword = psiNameHelper.isKeyword(name); // IDEADEV-15506
            if (!keyword) {
                holder.createProblem(domElement, SpringLocalize.modelInspectionInvalidIdentifierMessage(name).get());
            }
        }

        if (Character.isUpperCase(name.charAt(0))) {
            holder.createProblem(domElement, SpringLocalize.modelInspectionInvalidLowercaseNameMessage(name).get());
        }
    }
}