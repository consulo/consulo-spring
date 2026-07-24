/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.util.PropertyUtil;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.Result;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.dom.DomFileElement;
import consulo.xml.dom.DomUtil;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Map;

@ExtensionImpl
public class AutowiredDependenciesInspection extends SpringBeanInspectionBase<Object> {
    @Override
    public void checkFileElement(DomFileElement<Beans> domFileElement, DomElementAnnotationHolder holder, Object state) {
        super.checkFileElement(domFileElement, holder, state);

        Beans beans = domFileElement.getRootElement();
        DefaultAutowire defaultAutowire = beans.getDefaultAutowire().getValue();

        if (defaultAutowire != null && !DefaultAutowire.NO.equals(defaultAutowire)) {
            holder.createProblem(
                beans.getDefaultAutowire(),
                HighlightSeverity.WARNING,
                SpringLocalize.springBeanAutowireEscape().get(),
                createDefaultAutowireEscapeQuickFixes(beans.<Beans>createStableCopy(), defaultAutowire)
            );
        }
    }

    private static LocalQuickFix createDefaultAutowireEscapeQuickFixes(
        final Beans beans,
        final DefaultAutowire defaultAutowire
    ) {
        return new LocalQuickFix() {
            @Nonnull
            @Override
            public LocalizeValue getName() {
                return SpringLocalize.springBeanAutowireEscape();
            }

            public void applyFix(@Nonnull final Project project, @Nonnull ProblemDescriptor descriptor) {
                if (!beans.isValid()) {
                    return;
                }

                new WriteCommandAction(project, DomUtil.getFile(beans)) {
                    protected void run(Result result) throws Throwable {
                        for (SpringBean bean : beans.getBeans()) {
                            if (isAutowireCandidate(bean)) {
                                Autowire autowire = bean.getAutowire().getValue();

                                if (autowire == null
                                    || autowire.getValue().equals(defaultAutowire.getValue())
                                    || autowire.equals(Autowire.DEFAULT)) {
                                    escapeAutowire(defaultAutowire.getValue(), bean);
                                }
                            }
                        }
                        beans.getDefaultAutowire().undefine();
                    }
                }.execute();
            }
        };
    }

    protected void checkBean(
        SpringBean springBean,
        Beans beans,
        DomElementAnnotationHolder holder,
        SpringModel model,
        Object state
    ) {
        if (isAutowireCandidate(springBean)) {
            if (model != null) {
                addAutowireEscapeWarning(springBean, holder);
            }
        }
    }

    private static boolean isAutowireCandidate(SpringBean springBean) {
        Boolean autoWireCandidate = springBean.getAutowireCandidate().getValue();

        return autoWireCandidate == null || autoWireCandidate.booleanValue();

    }

    private static void addAutowireEscapeWarning(SpringBean springBean, DomElementAnnotationHolder holder) {
        Autowire autowire = springBean.getAutowire().getValue();
        if (autowire != null && !Autowire.NO.equals(autowire)) {
            holder.createProblem(
                springBean.getAutowire(),
                HighlightSeverity.WARNING,
                SpringLocalize.springBeanUseAutowire().get(),
                createEscapeAutowireQuickFixes(springBean.<SpringBean>createStableCopy(), autowire)
            );
        }
    }

    private static LocalQuickFix createEscapeAutowireQuickFixes(final SpringBean springBean, @Nonnull final Autowire autowire) {
        return new LocalQuickFix() {
            @Nonnull
            @Override
            public LocalizeValue getName() {
                return SpringLocalize.springBeanAutowireEscape();
            }

            public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
                if (!springBean.isValid()) {
                    return;
                }

                new WriteCommandAction(springBean.getManager().getProject(), DomUtil.getFile(springBean)) {
                    protected void run(Result result) throws Throwable {
                        escapeAutowire(autowire.getValue(), springBean);
                    }
                }.execute();
            }
        };
    }

    private static void escapeAutowire(@Nonnull String autowire, SpringBean springBean) {
        SpringModel springModel = SpringUtils.getSpringModel(springBean);

        if (autowire.equals(Autowire.BY_TYPE.getValue())) {
            escapeByTypeAutowire(springBean, springModel);
        }
        else if (autowire.equals(Autowire.BY_NAME.getValue())) {
            escapeByNameAutowire(springBean);
        }
        else if (autowire.equals(Autowire.CONSTRUCTOR.getValue())) {
            escapeConstructorAutowire(springBean, springModel);
        }
        else if (autowire.equals(Autowire.AUTODETECT.getValue())) {
            if (SpringConstructorArgResolveUtil.hasEmptyConstructor(springBean) &&
                !SpringConstructorArgResolveUtil.isInstantiatedByFactory(springBean)) {
                escapeByTypeAutowire(springBean, springModel);
            }
            else {
                escapeConstructorAutowire(springBean, springModel);
            }
        }
    }

    private static void escapeConstructorAutowire(SpringBean springBean, SpringModel springModel) {
        Map<PsiType, Collection<SpringBaseBeanPointer>> map =
            SpringAutowireUtil.getConstructorAutowiredProperties(springBean, springModel);
        for (PsiType psiType : map.keySet()) {
            ConstructorArg arg = springBean.addConstructorArg();
            arg.getType().setStringValue(psiType.getCanonicalText());
            arg.getRefAttr().setStringValue(chooseReferencedBeanName(map.get(psiType)));
        }

        springBean.getAutowire().undefine();

    }

    private static void escapeByNameAutowire(SpringBean springBean) {
        Map<PsiMethod, SpringBaseBeanPointer> autowiredProperties = SpringAutowireUtil.getByNameAutowiredProperties(springBean);
        for (PsiMethod psiMethod : autowiredProperties.keySet()) {
            SpringProperty springProperty = springBean.addProperty();
            SpringBaseBeanPointer autowiredBean = autowiredProperties.get(psiMethod);
            String refBeanName = autowiredBean != null && autowiredBean.getName() != null ? autowiredBean.getName() : "";

            springProperty.getName().setStringValue(PropertyUtil.getPropertyNameBySetter(psiMethod));
            springProperty.getRefAttr().setStringValue(refBeanName);
        }

        springBean.getAutowire().undefine();
    }

    private static void escapeByTypeAutowire(SpringBean springBean, SpringModel springModel) {
        Map<PsiMethod, Collection<SpringBaseBeanPointer>> autowiredProperties =
            SpringAutowireUtil.getByTypeAutowiredProperties(springBean, springModel);

        for (PsiMethod psiMethod : autowiredProperties.keySet()) {
            SpringProperty springProperty = springBean.addProperty();
            springProperty.getName().setStringValue(PropertyUtil.getPropertyNameBySetter(psiMethod));
            springProperty.getRefAttr().setStringValue(chooseReferencedBeanName(autowiredProperties.get(psiMethod)));
        }

        springBean.getAutowire().undefine();
    }

    @Nonnull
    private static String chooseReferencedBeanName(Collection<SpringBaseBeanPointer> autowiredBeans) {
        if (autowiredBeans != null) {
            for (SpringBaseBeanPointer autowiredBean : autowiredBeans) {
                String beanName = SpringUtils.getReferencedName(autowiredBean.getSpringBean());
                if (beanName != null && beanName.trim().length() > 0) {
                    return beanName;
                }
            }
        }
        return "";
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.springBeanAutowireEscapeInspectionName();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "AutowiredDependenciesInspection";
    }

    @Nonnull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }
}
