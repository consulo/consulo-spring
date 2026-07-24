/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.java.impl.util.xml.DomJavaUtil;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.spring.impl.ide.model.xml.aop.RequiredBeanType;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.GenericAttributeValue;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import consulo.xml.dom.editor.DomElementsInspection;
import consulo.xml.dom.editor.DomHighlightingHelper;
import jakarta.annotation.Nonnull;

@ExtensionImpl
public class RequiredBeanTypeInspection extends DomElementsInspection<Beans, Object> {
    public RequiredBeanTypeInspection() {
        super(Beans.class);
    }

    @Override
    protected void checkDomElement(DomElement element, DomElementAnnotationHolder holder, DomHighlightingHelper helper, Object state) {
        if (element instanceof GenericAttributeValue) {
            Object value = ((GenericAttributeValue) element).getValue();

            if (value instanceof SpringBeanPointer) {
                RequiredBeanType type = element.getAnnotation(RequiredBeanType.class);
                if (type != null) {
                    SpringBeanPointer springBeanPointer = (SpringBeanPointer) value;

                    PsiClass[] classes = springBeanPointer.getEffectiveBeanType();
                    PsiClass requiredClass = DomJavaUtil.findClass(type.value(), element);
                    if (requiredClass != null) {
                        boolean isAssignable = false;
                        for (PsiClass psiClass : classes) {
                            if (InheritanceUtil.isInheritorOrSelf(psiClass, requiredClass, true)) {
                                isAssignable = true;
                                break;
                            }
                        }
                        if (!isAssignable) {
                            if (holder != null) {
                                LocalizeValue message = SpringLocalize.beanMustBeOfType(requiredClass.getQualifiedName());
                                holder.createProblem(element, message.get());
                            }
                        }
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return SpringLocalize.modelInspectionGroupName();
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.requiredSpringBeanTypeInspection();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "RequiredBeanTypeInspection";
    }
}