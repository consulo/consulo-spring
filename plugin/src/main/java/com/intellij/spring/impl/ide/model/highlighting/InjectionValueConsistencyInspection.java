/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringModelVisitor;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.project.content.scope.ProjectScopes;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.language.psi.XmlElement;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomUtil;
import consulo.xml.dom.GenericDomValue;
import consulo.xml.util.xml.highlighting.AddDomElementQuickFix;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import consulo.xml.util.xml.highlighting.RemoveDomElementQuickFix;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;

/**
 * @author Dmitry Avdeev
 */
@ExtensionImpl
public class InjectionValueConsistencyInspection extends SpringBeanInspectionBase {
    private static final String ARG = SpringLocalize.springBeanConstructorArg().get();

    protected SpringModelVisitor createVisitor(
        final DomElementAnnotationHolder holder,
        Beans beans,
        SpringModel model,
        Object state
    ) {
        return new SpringModelVisitor() {
            protected boolean visitProperty(SpringPropertyDefinition property) {
                if (property instanceof SpringValueHolder) {
                    checkValueHolder((SpringValueHolder) property, holder, SpringLocalize.springBeanProperty());
                }
                return true;
            }

            protected boolean visitConstructorArg(ConstructorArg arg) {
                checkValueHolder(arg, holder, SpringLocalize.springBeanConstructorArg());
                return true;
            }

            protected boolean visitMapEntry(SpringEntry entry) {
                checkMapEntry(holder, entry);
                return true;
            }

            protected boolean visitRef(SpringRef ref) {
                checkRef(ref, holder);
                return true;
            }

            protected boolean visitIdref(Idref idref) {
                checkIdref(idref, holder);
                return true;
            }
        };
    }

    private static void checkValueHolder(
        SpringValueHolder valueHolder,
        DomElementAnnotationHolder holder,
        @Nonnull LocalizeValue elementName
    ) {
        boolean hasRefAttribute = valueHolder.getRefAttr().getXmlAttribute() != null;
        boolean hasValueAttribute = valueHolder.getValueAttr().getXmlAttribute() != null;

        Set<DomElement> values = getValues(valueHolder);

        if (!hasRefAttribute && !hasValueAttribute && values.size() == 0) {
            reportNoValue(valueHolder, holder, elementName);
        }
        else if ((hasRefAttribute && hasValueAttribute) || ((hasRefAttribute || hasValueAttribute)) && values.size() > 0) {
            LocalizeValue message =
                SpringLocalize.springBeanPropertyValueInconsistencyRefOrValueSubelemetMustDefined(elementName);
            if (hasValueAttribute) {
                reportAttribute(valueHolder.getValueAttr(), holder, message);
            }
            if (hasRefAttribute) {
                reportAttribute(valueHolder.getRefAttr(), holder, message);
            }
            reportSubtags(values, holder, message);
        }
        else if (values.size() > 1) {
            LocalizeValue message = SpringLocalize.springBeanPropertyValueInconsistencyMoreOneSubelement(elementName);
            reportSubtags(values, holder, message);
        }
    }

    private static void checkMapEntry(DomElementAnnotationHolder holder, SpringEntry entry) {
        boolean hasKeyAttr = DomUtil.hasXml(entry.getKeyAttr());
        boolean hasKeyElement = DomUtil.hasXml(entry.getKey());
        boolean hasKeyRef = DomUtil.hasXml(entry.getKeyRef());
        if (!hasKeyAttr && !hasKeyElement && !hasKeyRef) {
            holder.createProblem(entry, SpringLocalize.modelInspectionInjectionValueEntryKey().get()).highlightWholeElement();
        }
        else if (hasKeyAttr && hasKeyElement || hasKeyAttr && hasKeyRef || hasKeyElement && hasKeyRef) {
            LocalizeValue message = SpringLocalize.springBeanPropertyValueInconsistencyKey();
            if (hasKeyAttr) {
                reportAttribute(entry.getKeyAttr(), holder, message);
            }
            if (hasKeyElement) {
                reportAttribute(entry.getKey(), holder, message);
            }
            if (hasKeyRef) {
                reportAttribute(entry.getKeyRef(), holder, message);
            }
        }
        checkValueHolder(entry, holder, SpringLocalize.springBeanMapEntry());
    }

    private static void checkRef(SpringRef ref, DomElementAnnotationHolder holder) {
        if (!DomUtil.hasXml(ref)) {
            return;
        }

        boolean hasBean = ref.getBean().getXmlAttribute() != null;
        boolean hasLocal = ref.getLocal().getXmlAttribute() != null;
        boolean hasParent = ref.getParentAttr().getXmlAttribute() != null;
        if (!hasBean && !hasLocal && !hasParent) {
            holder.createProblem(ref, HighlightSeverity.ERROR,
                SpringBundle.message("spring.bean.ref.attributes.must.specify"),
                new AddRefFix(ref.getBean()),
                new AddRefFix(ref.getLocal()),
                new AddRefFix(ref.getParentAttr())
            ).highlightWholeElement();

        }
        else if (hasBean && hasLocal || hasBean && hasParent || hasLocal && hasParent) {
            LocalizeValue message = SpringLocalize.springBeanRefAttributesInconsistency();
            reportAttribute(ref.getBean(), holder, message);
            reportAttribute(ref.getLocal(), holder, message);
            reportAttribute(ref.getParentAttr(), holder, message);
        }
    }

    private static void checkIdref(Idref ref, DomElementAnnotationHolder holder) {
        if (!DomUtil.hasXml(ref)) {
            return;
        }

        boolean hasBean = ref.getBean().getXmlAttribute() != null;
        boolean hasLocal = ref.getLocal().getXmlAttribute() != null;
        if (!hasBean && !hasLocal) {
            holder.createProblem(ref, HighlightSeverity.ERROR,
                SpringBundle.message("spring.bean.idref.attributes.must.specify"),
                new AddRefFix(ref.getBean()),
                new AddRefFix(ref.getLocal())
            ).highlightWholeElement();

        }
        else if (hasBean && hasLocal) {
            LocalizeValue message = SpringLocalize.springBeanIdrefAttributesInconsistency();
            reportAttribute(ref.getBean(), holder, message);
            reportAttribute(ref.getLocal(), holder, message);
        }
    }

    @Nonnull
    private static Set<DomElement> getValues(SpringElementsHolder elementsHolder) {
        Set<DomElement> values = new HashSet<DomElement>(DomUtil.getDefinedChildrenOfType(elementsHolder, DomSpringBean.class));
        addValue(elementsHolder.getIdref(), values);
        addValue(elementsHolder.getList(), values);
        addValue(elementsHolder.getMap(), values);
        addValue(elementsHolder.getNull(), values);
        addValue(elementsHolder.getProps(), values);
        addValue(elementsHolder.getRef(), values);
        addValue(elementsHolder.getSet(), values);
        addValue(elementsHolder.getValue(), values);
        return values;
    }

    private static void addValue(DomElement valueElement, @Nonnull Set<DomElement> values) {
        if (DomUtil.hasXml(valueElement)) {
            values.add(valueElement);
        }
    }

    private static void reportSubtags(
        @Nonnull Set<DomElement> values,
        DomElementAnnotationHolder holder,
        @Nonnull LocalizeValue message
    ) {
        for (DomElement value : values) {
            holder.createProblem(value, HighlightSeverity.ERROR, message.get(), new RemoveDomElementQuickFix(value))
                .highlightWholeElement();
        }
    }

    private static void reportAttribute(DomElement element, DomElementAnnotationHolder holder, @Nonnull LocalizeValue message) {
        if (DomUtil.hasXml(element)) {
            holder.createProblem(element, HighlightSeverity.ERROR, message.get(), new RemoveDomElementQuickFix(element))
                .highlightWholeElement();
        }
    }

    private static void reportNoValue(
        SpringValueHolder injection,
        DomElementAnnotationHolder holder,
        @Nonnull LocalizeValue elementName
    ) {
        List<? extends PsiType> types = injection.getRequiredTypes();
        ArrayList<LocalQuickFix> quickFixes = new ArrayList<LocalQuickFix>();
        quickFixes.add(new AddDomElementQuickFix<DomElement>(injection.getValueAttr()));

        for (PsiType type : types) {
            if (type instanceof PsiClassType) {
                PsiClass psiClass = ((PsiClassType) type).resolve();
                quickFixes.add(0, new AddRefFix(injection.getRefAttr(), psiClass));
                if (psiClass != null) {
                    Project project = psiClass.getProject();
                    PsiManager psiManager = PsiManager.getInstance(project);
                    GlobalSearchScope scope = (GlobalSearchScope) ProjectScopes.getAllScope(project);
                    PsiClass listClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(List.class.getName(), scope);
                    if (listClass != null && InheritanceUtil.isInheritorOrSelf(psiClass, listClass, true)) {
                        quickFixes.add(0, new AddListFix(injection.getList()));
                    }
                    else {
                        PsiClass mapClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(Map.class.getName(), scope);
                        if (mapClass != null && InheritanceUtil.isInheritorOrSelf(psiClass, mapClass, true)) {
                            quickFixes.add(0, new AddMapFix(injection.getMap()));
                        }
                        else {
                            PsiClass setClass =
                                JavaPsiFacade.getInstance(psiManager.getProject()).findClass(Set.class.getName(), scope);
                            if (setClass != null && InheritanceUtil.isInheritorOrSelf(psiClass, setClass, true)) {
                                quickFixes.add(0, new AddListFix(injection.getSet()));
                            }
                        }
                    }
                }
            }
            else if (type instanceof PsiArrayType) {
                quickFixes.add(0, new AddListFix(injection.getSet()));
                quickFixes.add(0, new AddListFix(injection.getList()));
            }

        }

        holder.createProblem(
            injection,
            HighlightSeverity.ERROR,
            SpringBundle.message("model.inspection.injection.value.message", elementName),
            quickFixes.toArray(new LocalQuickFix[quickFixes.size()])
        ).highlightWholeElement();
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.modelInspectionInjectionValueConsistency();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "SpringInjectionValueConsistencyInspection";
    }

    private static class AddRefFix extends AddDomElementQuickFix<GenericDomValue> {
        @Nullable
        private final PsiClass myPsiClass;

        public AddRefFix(@Nonnull GenericDomValue ref) {
            this(ref, null);
        }

        public AddRefFix(@Nonnull GenericDomValue ref, @Nullable PsiClass psiClass) {
            super(ref);
            myPsiClass = psiClass;
        }

        @Nonnull
        @Override
        public LocalizeValue getName() {
            return SpringLocalize.modelInspectionInjectionValueAddRef();
        }

        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            super.applyFix(project, descriptor);
            if (myPsiClass != null) {
                XmlElement element = myElement.getXmlElement();
                assert element != null;
                XmlFile xmlFile = (XmlFile) element.getContainingFile();
                SpringModel model = SpringManager.getInstance(project).getSpringModelByFile(xmlFile);
                if (model != null) {
                    List<SpringBaseBeanPointer> list = model.findBeansByEffectivePsiClassWithInheritance(myPsiClass);
                    if (list.size() == 1) {
                        myElement.setStringValue(SpringUtils.getReferencedName(list.get(0), model.getAllCommonBeans(true)));
                    }
                }
            }
        }
    }

    private static class AddListFix extends AddDomElementQuickFix<ListOrSet> {
        public AddListFix(@Nonnull ListOrSet listOrSet) {
            super(listOrSet);
        }

        @Nonnull
        @Override
        public LocalizeValue getName() {
            return SpringLocalize.modelInspectionInjectionValueAddList();
        }

        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            SpringValue value = myElement.addValue();
            value.setStringValue("x");
            value.setStringValue("");
        }
    }

    private static class AddMapFix extends AddDomElementQuickFix<SpringMap> {
        public AddMapFix(SpringMap map) {
            super(map);
        }

        @Nonnull
        @Override
        public LocalizeValue getName() {
            return SpringLocalize.modelInspectionInjectionValueAddMap();
        }

        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            myElement.addEntry();
        }
    }
}
