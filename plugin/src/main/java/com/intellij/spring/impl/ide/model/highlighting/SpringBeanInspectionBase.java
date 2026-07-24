/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringModelVisitor;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import consulo.localize.LocalizeValue;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.dom.DomFileElement;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import consulo.xml.dom.editor.DomElementsInspection;
import jakarta.annotation.Nonnull;

/**
 * @author Dmitry Avdeev
 */
public abstract class SpringBeanInspectionBase<State> extends DomElementsInspection<Beans, State> {
    public SpringBeanInspectionBase() {
        super(Beans.class);
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return SpringLocalize.modelInspectionGroupName();
    }

    @Override
    public void checkFileElement(DomFileElement<Beans> domFileElement, DomElementAnnotationHolder holder, State state) {
        XmlFile xmlFile = domFileElement.getFile();
        Beans beans = domFileElement.getRootElement();
        SpringModel model = SpringManager.getInstance(xmlFile.getProject()).getSpringModelByFile(xmlFile);
        SpringModelVisitor visitor = createVisitor(holder, beans, model, state);
        SpringModelVisitor.visitBeans(visitor, beans);
    }

    protected SpringModelVisitor createVisitor(
        final DomElementAnnotationHolder holder,
        final Beans beans,
        final SpringModel model,
        State state
    ) {
        return new SpringModelVisitor() {

            @Override
            protected boolean visitBean(CommonSpringBean bean) {
                if (bean instanceof SpringBean) {
                    checkBean((SpringBean) bean, beans, holder, model, state);
                }
                return true;
            }
        };
    }

    protected void checkBean(
        SpringBean springBean,
        Beans beans,
        DomElementAnnotationHolder holder,
        SpringModel springModel,
        State state
    ) {
    }
}
