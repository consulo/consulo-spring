/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: Sergey.Vasiliev
 * Date: Nov 15, 2006
 * Time: 1:06:27 PM
 */
package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.java.impl.util.xml.impl.ExtendsClassChecker;
import com.intellij.spring.impl.ide.SpringBundle;
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
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class SpringModelInspection extends BasicDomElementsInspection<Beans, Object> {
    protected boolean shouldCheckResolveProblems(GenericDomValue value) {
        return !SpringBeanScope.class.equals(DomUtil.getGenericValueParameter(value.getDomElementType()))
            && super.shouldCheckResolveProblems(value);

    }

    public SpringModelInspection() {
        super(Beans.class);
    }

    protected void checkDomElement(
        DomElement element,
        DomElementAnnotationHolder holder,
        DomHighlightingHelper helper,
        Object state
    ) {
        int oldSize = holder.getSize();
        super.checkDomElement(element, holder, helper, state);

        if (oldSize == holder.getSize() && element instanceof GenericDomValue) {
            ExtendsClassChecker.checkExtendsClassInReferences((GenericDomValue) element, holder);
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
        return SpringLocalize.modelInspectionDisplayName();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "SpringModelInspection";
    }
}