/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringModelVisitor;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.CustomBeanWrapper;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.localize.LocalizeValue;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import jakarta.annotation.Nonnull;

/**
 * @author Dmitry Avdeev
 */
@ExtensionImpl
public class UnparsedCustomBeanInspection extends SpringBeanInspectionBase {

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.unparsedCustomBeanInspection();
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @Nonnull
    public String getShortName() {
        return "UnparsedCustomBeanInspection";
    }

    protected SpringModelVisitor createVisitor(
        final DomElementAnnotationHolder holder,
        Beans beans,
        SpringModel model,
        Object state
    ) {
        return new SpringModelVisitor() {

            protected boolean visitBean(CommonSpringBean bean) {
                if (bean instanceof CustomBeanWrapper) {
                    CustomBeanWrapper wrapper = (CustomBeanWrapper) bean;
                    if (!wrapper.isParsed()) {
                        holder.createProblem(
                            wrapper,
                            HighlightSeverity.GENERIC_SERVER_ERROR_OR_WARNING,
                            SpringLocalize.unparsedCustomBeanMessage().get()
                        );
                    }
                }
                return true;
            }
        };
    }


}