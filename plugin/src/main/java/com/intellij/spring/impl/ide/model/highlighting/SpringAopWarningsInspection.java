/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: Sergey.Vasiliev
 * Date: Nov 15, 2006
 * Time: 1:06:27 PM
 */
package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.aop.jam.AopJavaAnnotator;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.model.xml.aop.SpringAspect;
import com.intellij.spring.impl.ide.model.xml.aop.SpringPointcut;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.annotation.component.ExtensionImpl;
import consulo.aop.localize.AopLocalize;
import consulo.ide.localize.IdeLocalize;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.localize.LocalizeValue;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.GenericAttributeValue;
import consulo.xml.dom.editor.BasicDomElementsInspection;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import consulo.xml.dom.editor.DomHighlightingHelper;
import jakarta.annotation.Nonnull;

@ExtensionImpl
public class SpringAopWarningsInspection extends BasicDomElementsInspection<Beans, Object> {
    public SpringAopWarningsInspection() {
        super(Beans.class);
    }

    @Nonnull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    protected void checkDomElement(DomElement element, DomElementAnnotationHolder holder, DomHighlightingHelper helper) {
        if (element.getXmlTag() == null) {
            return;
        }

        if (element instanceof SpringPointcut && ((SpringPointcut) element).getId().getXmlAttribute() == null) {
            holder.createProblem(
                element,
                HighlightSeverity.WARNING,
                SpringLocalize.warningPointcutShouldHaveId().get(),
                new DefineAttributeQuickFix("id")
            );
        }
        else if (element instanceof SpringAspect) {
            GenericAttributeValue<SpringBeanPointer> ref = ((SpringAspect) element).getRef();
            if (ref.getXmlAttribute() == null) {
                holder.createProblem(
                    element,
                    HighlightSeverity.WARNING,
                    IdeLocalize.attribute0ShouldBeDefined("ref").get(),
                    new DefineAttributeQuickFix("ref")
                );
                return;
            }

            SpringBeanPointer pointer = ref.getValue();
            if (pointer != null) {
                PsiClass beanClass = pointer.getBeanClass();
                if (beanClass != null
                    && (!AopJavaAnnotator.getBoundAdvices(beanClass).isEmpty()
                    || !AopJavaAnnotator.getBoundIntroductions(beanClass).isEmpty())) {
                    holder.createProblem(ref, HighlightSeverity.WARNING, SpringLocalize.aopAdviceMatchingAspectBean().get());
                }
            }
        }
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return AopLocalize.inspectionGroupDisplayNameAop();
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.aopWarningsInspectionDisplayName();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "SpringAopWarningsInspection";
    }
}
