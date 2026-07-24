/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.Alias;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.localize.LocalizeValue;
import consulo.spring.localize.SpringLocalize;
import consulo.util.lang.text.StringTokenizer;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.dom.DomFileElement;
import consulo.xml.dom.GenericAttributeValue;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import jakarta.annotation.Nonnull;

import java.util.List;

@ExtensionImpl
public class DuplicatedBeanNamesInspection extends SpringBeanInspectionBase<Object> {
    @Override
    public void checkFileElement(DomFileElement<Beans> domFileElement, DomElementAnnotationHolder holder, Object state) {
        XmlFile xmlFile = domFileElement.getFile();
        SpringModel model = SpringManager.getInstance(xmlFile.getProject()).getSpringModelByFile(xmlFile);
        Beans beans = domFileElement.getRootElement();
        for (CommonSpringBean bean : SpringUtils.getChildBeans(beans, false)) {
            if (bean instanceof DomSpringBean) {
                checkBean((DomSpringBean) bean, holder, model);
            }
        }
        for (Alias alias : beans.getAliases()) {
            checkAlias(alias, holder, model);
        }
    }

    private static void checkBean(DomSpringBean bean, DomElementAnnotationHolder holder, SpringModel springModel) {
        String id = bean.getId().getStringValue();
        if (id != null && springModel.isNameDuplicated(id)) {
            holder.createProblem(bean.getId(), HighlightSeverity.ERROR, SpringLocalize.springBeanDublicateBeanName().get());
        }

        if (bean instanceof SpringBean) {
            SpringBean springBean = (SpringBean) bean;
            GenericAttributeValue<List<String>> name = springBean.getName();
            String value = name.getStringValue();
            if (value != null) {
                StringTokenizer tokenizer = new StringTokenizer(value, SpringUtils.SPRING_DELIMITERS);
                while (tokenizer.hasMoreTokens()) {
                    String s = tokenizer.nextToken();
                    if (springModel.isNameDuplicated(s)) {
                        holder.createProblem(
                            name,
                            HighlightSeverity.ERROR,
                            SpringLocalize.springBeanDublicateBeanName().get(),
                            TextRange.from(tokenizer.getCurrentPosition() - s.length() + 1, s.length())
                        );
                    }
                }
            }
        }
    }

    private static void checkAlias(Alias alias, DomElementAnnotationHolder holder, SpringModel model) {
        GenericAttributeValue<String> value = alias.getAlias();
        String aliasName = value.getStringValue();
        if (aliasName != null && model.isNameDuplicated(aliasName)) {
            holder.createProblem(value, HighlightSeverity.ERROR, SpringLocalize.springBeanDublicateBeanName().get());
        }
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.springBeanDuplicatedBeanNameInspection();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "DuplicatedBeanNamesInspection";
    }
}