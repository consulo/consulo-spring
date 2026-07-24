/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import consulo.localize.LocalizeValue;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomFileElement;
import consulo.xml.dom.DomUtil;
import consulo.xml.dom.GenericDomValue;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import consulo.xml.dom.editor.DomElementsInspection;
import jakarta.annotation.Nonnull;

import java.util.function.Consumer;

/**
 * @author Dmitry Avdeev
 */
public abstract class DomSpringBeanInspectionBase<State> extends DomElementsInspection<Beans, State> {
    public DomSpringBeanInspectionBase() {
        super(Beans.class);
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return SpringLocalize.modelInspectionGroupName();
    }

    @Override
    public void checkFileElement(DomFileElement<Beans> domFileElement, final DomElementAnnotationHolder holder, State state) {
        XmlFile xmlFile = domFileElement.getFile();
        final Beans beans = domFileElement.getRootElement();
        final SpringModel model = SpringManager.getInstance(xmlFile.getProject()).getSpringModelByFile(xmlFile);
        Consumer<DomElement> consumer = new Consumer<DomElement>() {
            public void accept(DomElement element) {
                if (element instanceof DomSpringBean) {
                    checkBean((DomSpringBean) element, beans, holder, model);
                }
                else if (!(element instanceof GenericDomValue) && DomUtil.hasXml(element)) {
                    checkChildren(element, this);
                }
            }
        };
        consumer.accept(domFileElement.getRootElement());
    }

    protected void checkBean(
        DomSpringBean springBean,
        Beans beans,
        DomElementAnnotationHolder holder,
        SpringModel springModel
    ) {
    }
}