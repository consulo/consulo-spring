/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiModifier;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.jam.SpringJamModel;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJamElement;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.CodeInsightUtilCore;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.language.psi.XmlElement;
import consulo.xml.dom.DomUtil;
import consulo.xml.dom.GenericAttributeValue;
import consulo.xml.util.xml.highlighting.AddDomElementQuickFix;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import jakarta.annotation.Nonnull;

/**
 * @author Dmitry Avdeev
 */
@ExtensionImpl
public class SpringBeanInstantiationInspection extends SpringBeanInspectionBase {
    protected void checkBean(
        SpringBean springBean,
        Beans beans,
        DomElementAnnotationHolder holder,
        SpringModel springModel,
        Object state
    ) {
        PsiClass psiClass = springBean.getClazz().getValue();
        if (psiClass != null && !springBean.isAbstract()) {
            if (psiClass.isInterface()) {
                return;
            }
            boolean factory = DomUtil.hasXml(springBean.getFactoryMethod());
            boolean lookup = springBean.getLookupMethods().size() > 0;
            if ((psiClass.hasModifierProperty(PsiModifier.ABSTRACT) && !factory && !lookup && !isJavaConfiBean(springBean))) {
                holder.createProblem(
                    springBean.getClazz(),
                    HighlightSeverity.WARNING,
                    SpringLocalize.abstractClassNotAllowed().get(),
                    new MarkAbstractFix(springBean.getAbstract())
                );
            }
        }
    }

    private static boolean isJavaConfiBean(SpringBean springBean) {
        XmlElement xmlElement = springBean.getXmlElement();
        PsiClass beanClass = springBean.getBeanClass();

        if (xmlElement != null && beanClass != null) {
            Module module = ModuleUtilCore.findModuleForPsiElement(xmlElement);
            if (module != null) {
                for (SpringJamElement javaConfiguration : SpringJamModel.getModel(module).getConfigurations()) {
                    if (beanClass.equals(javaConfiguration.getPsiClass())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.springBeanInstantiationInspection();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "SpringBeanInstantiationInspection";
    }

    private static class MarkAbstractFix extends AddDomElementQuickFix<GenericAttributeValue<Boolean>> {
        public MarkAbstractFix(GenericAttributeValue<Boolean> value) {
            super(value);
        }

        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            if (CodeInsightUtilCore.getInstance().preparePsiElementForWrite(descriptor.getPsiElement())) {
                myElement.setValue(Boolean.TRUE);
            }
        }

        @Nonnull
        @Override
        public LocalizeValue getName() {
            return SpringLocalize.markBeanAsAbstract();
        }
    }
}
