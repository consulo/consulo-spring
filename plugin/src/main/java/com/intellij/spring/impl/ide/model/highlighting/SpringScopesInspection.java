/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanScope;
import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomUtil;
import consulo.xml.dom.GenericDomValue;
import consulo.xml.dom.editor.BasicDomElementsInspection;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import consulo.xml.dom.editor.DomHighlightingHelper;
import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
@ExtensionImpl
public class SpringScopesInspection extends BasicDomElementsInspection<Beans, Object> {
    protected void checkDomElement(DomElement element, DomElementAnnotationHolder holder, DomHighlightingHelper helper) {
        if (element instanceof GenericDomValue) {
            GenericDomValue value = (GenericDomValue) element;
            if (SpringBeanScope.class.equals(DomUtil.getGenericValueParameter(value.getDomElementType()))) {
                helper.checkResolveProblems(value, holder);
            }
        }
    }

    public SpringScopesInspection() {
        super(Beans.class);
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return SpringLocalize.modelInspectionGroupName();
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.scopesInspectionDisplayName();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "SpringScopesInspection";
    }
}
