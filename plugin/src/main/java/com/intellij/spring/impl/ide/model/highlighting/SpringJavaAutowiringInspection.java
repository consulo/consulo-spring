package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.java.analysis.impl.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.java.language.psi.util.PropertyUtil;
import com.intellij.java.language.psi.util.PsiUtil;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.impl.ide.references.SpringBeanReference;
import com.intellij.spring.impl.ide.references.SpringQualifierReference;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.java.impl.model.annotations.AnnotationModelUtil;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiNameIdentifierOwner;
import consulo.language.psi.PsiReference;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.spring.localize.SpringLocalize;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Sergey.Vasiliev
 */
@ExtensionImpl
public class SpringJavaAutowiringInspection extends BaseJavaLocalInspectionTool<Object> {
    @Nullable
    @RequiredReadAction
    private static SpringModel getModelForBean(@Nullable PsiClass aClass) {
        if (aClass == null) {
            return null;
        }
        Module module = aClass.getModule();
        if (module == null) {
            return null;
        }
        return SpringManager.getInstance(module.getProject()).getCombinedModel(module);
    }

    @Nonnull
    @Override
    public PsiElementVisitor buildVisitorImpl(
        @Nonnull ProblemsHolder holder,
        boolean isOnTheFly,
        LocalInspectionToolSession session,
        Object o
    ) {
        return new JavaElementVisitor() {
            @Override
            @RequiredReadAction
            public void visitMethod(@Nonnull PsiMethod method) {
                if (SpringAutowireUtil.isAutowiredByAnnotation(method)) {
                    SpringModel springModel = getModelForBean(method.getContainingClass());
                    if (springModel != null) {
                        boolean required = SpringAutowireUtil.isRequired(method);
                        checkAutowiredMethod(method, holder, springModel, required);
                    }
                }
            }

            @Override
            @RequiredReadAction
            public void visitField(@Nonnull PsiField field) {
                if (SpringAutowireUtil.isAutowiredByAnnotation(field)) {
                    SpringModel springModel = getModelForBean(field.getContainingClass());
                    if (springModel != null) {
                        boolean required = SpringAutowireUtil.isRequired(field);
                        checkAutowiredPsiMember(field, field.getType(), holder, springModel, required);
                    }
                }
            }
        };
    }

    private static void checkAutowiredMethod(
        PsiMethod psiMethod,
        @Nullable ProblemsHolder holder,
        SpringModel springModel,
        boolean required
    ) {
        PsiAnnotation resourceAnnotation = SpringAutowireUtil.getResourceAnnotation(psiMethod);
        if (resourceAnnotation != null) {
            PsiType type = PropertyUtil.getPropertyType(psiMethod);
            if (type != null) {
                checkAutowiredPsiMember(psiMethod, type, holder, springModel, required);
            }
        }
        else {
            for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
                checkAutowiredPsiMember(parameter, parameter.getType(), holder, springModel, required);
            }
        }
    }

    @Nullable
    public static Collection<SpringBaseBeanPointer> checkAutowiredPsiMember(
        PsiModifierListOwner modifierListOwner,
        @Nonnull PsiType psiType,
        @Nullable ProblemsHolder holder,
        @Nonnull SpringModel springModel,
        boolean required
    ) {

        PsiType beanType = psiType instanceof PsiArrayType
            ? ((PsiArrayType) psiType).getComponentType()
            : PsiUtil.extractIterableTypeParameter(psiType, true);
        boolean isIterable = beanType != null;
        if (beanType == null) {
            beanType = psiType;
        }
        PsiAnnotation resourceAnnotation = SpringAutowireUtil.getResourceAnnotation(modifierListOwner);
        if (resourceAnnotation != null && modifierListOwner instanceof PsiMember) {
            PsiAnnotationMemberValue attributeValue = resourceAnnotation.findDeclaredAttributeValue("name");
            if (attributeValue != null) {
                return checkByNameAutowiring(attributeValue, beanType, holder, springModel);
            }
            String name = null;
            if (modifierListOwner instanceof PsiMethod) {
                name = PropertyUtil.getPropertyNameBySetter((PsiMethod) modifierListOwner);
            }
            else if (modifierListOwner instanceof PsiField) {
                name = ((PsiField) modifierListOwner).getName();
            }
            if (name != null) {
                SpringBeanPointer bean = springModel.findBean(name);
                if (bean != null) {
                    return Collections.singleton(bean.getBasePointer());
                }
            }
            return checkByTypeAutowire(
                ((PsiNameIdentifierOwner) modifierListOwner).getNameIdentifier(),
                beanType,
                holder,
                springModel,
                isIterable,
                required
            );
        }
        PsiAnnotation qualifiedAnnotation = SpringAutowireUtil.getQualifiedAnnotation(modifierListOwner);

        if (qualifiedAnnotation == null) {
            return checkByTypeAutowire(
                ((PsiNameIdentifierOwner) modifierListOwner).getNameIdentifier(),
                beanType,
                holder,
                springModel,
                isIterable,
                required
            );
        }
        else {
            return checkQualifiedAutowiring(beanType, qualifiedAnnotation, holder, springModel);
        }
    }

    @Nullable
    private static Collection<SpringBaseBeanPointer> checkByNameAutowiring(
        PsiAnnotationMemberValue annotationMemberValue,
        PsiType psiType,
        @Nullable ProblemsHolder holder,
        @Nonnull SpringModel model
    ) {
        SpringBeanReference ref = null;
        for (PsiReference reference : annotationMemberValue.getReferences()) {
            if (reference instanceof SpringBeanReference) {
                ref = (SpringBeanReference) reference;
            }
        }
        if (ref != null) {
            SpringBeanPointer bean = model.findBean(ref.getValue());
            if (bean != null) {
                PsiClass beanClass = bean.getBeanClass();
                if (psiType instanceof PsiClassType) {
                    PsiClass psiClass = ((PsiClassType) psiType).resolve();
                    if (psiClass != null && InheritanceUtil.isInheritorOrSelf(beanClass, psiClass, true)) {
                        return Collections.singleton(bean.getBasePointer());
                    }
                }
                if (holder != null) {
                    holder.registerProblem(
                        ref,
                        SpringLocalize.cannotAutowireBeanOfType(psiType.getCanonicalText()).get(),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                    );
                }
            }
            else {
                if (holder != null) {
                    holder.registerProblem(ref);
                }
            }
        }
        return null;
    }

    @Nullable
    private static List<SpringBaseBeanPointer> checkQualifiedAutowiring(
        PsiType type,
        PsiAnnotation qualifiedAnnotation,
        @Nullable ProblemsHolder holder,
        @Nonnull SpringModel springModel
    ) {

        PsiAnnotationMemberValue attributeValue = qualifiedAnnotation.findDeclaredAttributeValue("value");
        PsiReference qreference = null;
        String name = attributeValue == null ? null : AnnotationModelUtil.getStringValue(qualifiedAnnotation, "value", "").getValue();
        PsiReference[] references;
        if (attributeValue != null) {
            references = attributeValue.getReferences();
            for (PsiReference reference : references) {
                if (reference instanceof SpringQualifierReference) {
                    qreference = reference;
                    if (((SpringQualifierReference) reference).multiResolve(false).length == 0) {
                        if (holder != null && reference.getElement().isPhysical()) {
                            holder.registerProblem(
                                reference,
                                SpringLocalize.beanClassUnknownQualifierBean(name).get(),
                                ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
                            );
                        }
                        return null;
                    }
                }
            }
        }
        if (type instanceof PsiClassType) {
            PsiClass aClass = ((PsiClassType) type).resolve();
            if (aClass != null) {
                List<SpringBaseBeanPointer> candidates = SpringAutowireUtil.getQualifiedBeans(qualifiedAnnotation, springModel);
                if (name != null) {
                    SpringBeanPointer pointer = springModel.findBean(name);
                    if (pointer != null) {
                        candidates = new ArrayList<SpringBaseBeanPointer>(candidates);
                        candidates.add(pointer.getBasePointer());
                    }
                }
                List<SpringBaseBeanPointer> beanPointers = SpringAutowireUtil.excludeAutowireCandidatesForCommonBeans(candidates);

                if (beanPointers.size() == 0) {
                    if (holder != null) {
                        if (attributeValue != null) {
                            reportProblem(
                                holder,
                                qreference,
                                attributeValue,
                                SpringLocalize.beanClassUnknownQualifierBean(name)
                            );
                        }
                        else {
                            String qualifiedName = qualifiedAnnotation.getQualifiedName();
                            assert qualifiedName != null;
                            reportProblem(
                                holder,
                                qreference,
                                qualifiedAnnotation,
                                SpringLocalize.cannotFindBeanQualifiedBy("@" + StringUtil.getShortName(qualifiedName))
                            );
                        }
                    }
                    return null;
                }
                else if (beanPointers.size() == 1) {
                    boolean isAssignable = false;
                    for (SpringBaseBeanPointer bean : beanPointers) {
                        PsiClass[] psiClasses = bean.getEffectiveBeanType();
                        for (PsiClass psiClass : psiClasses) {
                            if (InheritanceUtil.isInheritorOrSelf(psiClass, aClass, true)) {
                                isAssignable = true;
                                break;
                            }
                        }
                    }

                    if (!isAssignable) {
                        if (holder != null) {
                            LocalizeValue message =
                                SpringLocalize.beanClassAutowiredIncorrectQualifierType(type.getPresentableText());
                            reportProblem(holder, qreference, attributeValue == null ? qualifiedAnnotation : attributeValue, message);
                        }
                        return null;
                    }
                }
                return beanPointers;
            }
        }
        return null;
    }

    private static void reportProblem(
        @Nonnull ProblemsHolder holder,
        PsiReference qreference,
        @Nonnull PsiAnnotationMemberValue attributeValue,
        @Nonnull LocalizeValue text
    ) {
        if (qreference == null) {
            holder.newProblem(text)
                .range(attributeValue)
                .create();
        }
        else {
            holder.registerProblem(qreference, text.get(), ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }
    }

    @Nullable
    private static Collection<SpringBaseBeanPointer> checkByTypeAutowire(
        PsiElement psiElement,
        @Nonnull PsiType type,
        @Nullable ProblemsHolder holder,
        @Nonnull SpringModel springModel,
        boolean iterable, boolean required
    ) {

        Collection<SpringBaseBeanPointer> beanPointers = SpringAutowireUtil.autowireByType(springModel, type);

        if (beanPointers.isEmpty() && required) {
            if (holder != null && !SpringAutowireUtil.isAutowiredByDefault(type)) {
                holder.newProblem(SpringLocalize.beanAutowiringByTypeNone(type.getPresentableText()))
                    .range(psiElement)
                    .create();
            }
            return null;
        }
        else if (beanPointers.size() > 1 && !iterable) {
            if (holder != null) {
                List<String> beanNames = new ArrayList<String>();
                for (SpringBaseBeanPointer bean : beanPointers) {
                    String beanName = bean.getName();
                    if (StringUtil.isEmptyOrSpaces(beanName)) {
                        beanName = "Unknown";
                    }
                    beanNames.add(beanName);
                }
                Collections.sort(beanNames);
                holder.newProblem(SpringLocalize.beanClassAutowiredByType(type.getPresentableText(), StringUtil.join(beanNames, ",")))
                    .range(psiElement)
                    .create();
            }
            return beanPointers;
        }
        else {
            return beanPointers;
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
        return SpringLocalize.modelQualifiersInClassInspectionDisplayName();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "SpringJavaAutowiringInspection";
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    @Nonnull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }
}

