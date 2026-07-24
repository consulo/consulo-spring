/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PsiUtil;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.*;
import com.intellij.spring.impl.ide.model.xml.custom.ParseCustomBeanIntention;
import consulo.codeEditor.Editor;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.intention.IntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.util.lang.StringUtil;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.*;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Sergey.Vasiliev
 * @since 2006-11-10
 */
public class SpringBeanResolveConverter extends ResolvingConverter<SpringBeanPointer> {
    public SpringBeanPointer fromString(@Nullable String s, ConvertContext context) {
        if (s == null) {
            return null;
        }
        SpringModel model = getSpringModel(context);
        return model == null ? null : SpringUtils.getBeanPointer(model, s);
    }

    public String toString(@Nullable SpringBeanPointer springBeanPointer, ConvertContext context) {
        return springBeanPointer == null ? null : springBeanPointer.getName();
    }

    @Nonnull
    public Collection<SpringBeanPointer> getVariants(ConvertContext context) {
        return getVariants(context, false);
    }

    @Override
    public LookupElement createLookupElement(SpringBeanPointer springBeanPointer) {
        return createCompletionVariant(springBeanPointer);
    }

    protected Collection<SpringBeanPointer> getVariants(ConvertContext context, boolean parentBeans) {
        SpringModel model = getSpringModel(context);
        if (model == null) {
            return Collections.emptyList();
        }

        List<SpringBeanPointer> variants = new ArrayList<SpringBeanPointer>();
        CommonSpringBean currentBean = SpringConverterUtil.getCurrentBeanCustomAware(context);
        processBeans(model, variants, parentBeans ? model.getAllParentBeans() : model.getAllCommonBeans(), parentBeans, currentBean);
        return variants;
    }

    public Collection<SpringBeanPointer> getSmartVariants(ConvertContext context) {
        SpringModel model = getSpringModel(context);
        if (model == null) {
            return Collections.emptyList();
        }

        List<SpringBeanPointer> variants = new ArrayList<SpringBeanPointer>();

        CommonSpringBean currentBean = SpringConverterUtil.getCurrentBeanCustomAware(context);
        List<PsiClassType> requiredClasses = getRequiredClasses(context);
        if (requiredClasses != null) {
            for (PsiClassType requiredClass : requiredClasses) {
                PsiClass psiClass = PsiUtil.resolveClassInType(requiredClass);
                if (psiClass != null) {
                    processBeans(model, variants, model.findBeansByEffectivePsiClassWithInheritance(psiClass), false, currentBean);
                }
                PsiClass componentClass = PsiUtil.resolveClassInType(PsiUtil.extractIterableTypeParameter(requiredClass, false));
                if (componentClass != null) {
                    processBeans(model, variants, model.findBeansByEffectivePsiClassWithInheritance(componentClass), false, currentBean);
                }
            }
        }
        return variants;
    }

    private static void processBeans(
        SpringModel model,
        List<SpringBeanPointer> variants,
        Collection<? extends SpringBaseBeanPointer> pointers,
        boolean acceptAbstract,
        CommonSpringBean currentBean
    ) {
        for (SpringBaseBeanPointer bean : pointers) {
            if (!acceptAbstract && bean.isAbstract()) {
                continue;
            }
            if (bean.isReferenceTo(currentBean)) {
                continue;
            }

            String beanName = bean.getName();
            if (beanName != null) {
                for (String string : model.getAllBeanNames(beanName)) {
                    if (StringUtil.isNotEmpty(string)) {
                        variants.add(bean.derive(string));
                    }
                }
            }
        }
    }

    private final CreateElementQuickFixProvider<SpringBeanPointer> myQuickFixProvider =
        new CreateElementQuickFixProvider<SpringBeanPointer>() {
            @Override
            public LocalQuickFix[] getQuickFixes(GenericDomValue<SpringBeanPointer> value) {
                List<LocalQuickFix> result = new SmartList<LocalQuickFix>();
                ContainerUtil.addIfNotNull(result, getQuickFix(value));

                String id = value.getStringValue();
                if (StringUtil.isNotEmpty(id)) {
                    List<SpringModel> models = SpringUtils.getNonEmptySpringModelsByFile(DomUtil.getFile(value));
                    Collection<XmlTag> tags = ContainerUtil.concat(models, model -> model.getCustomBeanCandidates(id));
                    if (!tags.isEmpty()) {
                        result.add(new TryParsingCustomBeansFix(tags));
                    }
                }

                return result.toArray(new LocalQuickFix[result.size()]);
            }

            protected void apply(String elementName, GenericDomValue<SpringBeanPointer> genericDomValue) {
                Beans beans = getBeans(genericDomValue);
                if (beans != null) {
                    SpringBean springBean = beans.addBean();
                    springBean.setName(elementName);
                    // setting correct class...
                    DomElement parent = genericDomValue.getParent();
                    if (parent instanceof SpringInjection) {
                        PsiClassType classType = SpringBeanUtil.getRequiredClass((SpringInjection) parent);
                        if (classType != null) {
                            springBean.getClazz().setStringValue(classType.getCanonicalText());
                        }
                    }
                }
            }

            @Nonnull
            @Override
            protected LocalizeValue getFixName(String elementName) {
                return SpringLocalize.modelBeanQuickfixMessage(elementName);
            }
        };

    @Nullable
    protected Beans getBeans(GenericDomValue<SpringBeanPointer> genericDomValue) {
        return genericDomValue.getParentOfType(Beans.class, false);
    }

    public String getErrorMessage(String s, ConvertContext context) {
        return SpringLocalize.modelBeanErrorMessage(s).get();
    }

    @Nullable
    protected SpringModel getSpringModel(ConvertContext context) {
        return SpringManager.getInstance(context.getFile().getProject()).getSpringModelByFile(context.getFile());
    }

    /**
     * Used to filter variants by bean class.
     *
     * @param context conversion context.
     * @return null if no requirement applied; empty list to suppress completion.
     */
    @Nullable
    public List<PsiClassType> getRequiredClasses(ConvertContext context) {
        return null;
    }

    public PsiElement getPsiElement(@Nullable SpringBeanPointer resolvedValue) {
        if (resolvedValue == null) {
            return null;
        }

        return resolvedValue.getPsiElement();
    }

    public LocalQuickFix[] getQuickFixes(ConvertContext context) {
        return myQuickFixProvider.getQuickFixes((GenericDomValue) context.getInvocationElement());
    }

    @Nullable
    private static List<PsiClassType> getValueClasses(ConvertContext context) {
        TypeHolder valueHolder = context.getInvocationElement().getParentOfType(TypeHolder.class, false);
        assert valueHolder != null;
        if (valueHolder instanceof ConstructorArg) {
            return getClassesForArg((ConstructorArg) valueHolder);
        }
        else {
            PsiClassType classType = SpringBeanUtil.getRequiredClass(valueHolder);
            return classType == null ? null : Collections.singletonList(classType);
        }
    }

    @Nullable
    private static List<PsiClassType> getClassesForArg(ConstructorArg arg) {
        PsiType type = arg.getType().getValue();
        if (type == null) {
            Integer integer = arg.getIndex().getValue();
            int index = -1;
            if (integer != null) {
                index = integer.intValue();
            }
            ArrayList<PsiClassType> classes = new ArrayList<PsiClassType>();
            SpringBean springBean = (SpringBean) arg.getParent();
            List<PsiMethod> psiMethods = SpringBeanUtil.getInstantiationMethods(springBean);
            for (PsiMethod method : psiMethods) {
                PsiParameterList parameterList = method.getParameterList();
                if (parameterList.getParametersCount() > 0) {
                    if (index >= 0) {
                        if (index < parameterList.getParametersCount()) {
                            type = parameterList.getParameters()[index].getType();
                            if (type instanceof PsiClassType) {
                                classes.add((PsiClassType) type);
                            }
                        }
                    }
                    else {
                        for (PsiParameter parameter : parameterList.getParameters()) {
                            type = parameter.getType();
                            if (type instanceof PsiClassType) {
                                classes.add((PsiClassType) type);
                            }
                        }
                    }
                }
            }
            return classes;
        }
        else {
            if (type instanceof PsiClassType) {
                return Collections.singletonList((PsiClassType) type);
            }
            else {
                return Collections.emptyList();
            }
        }
    }

    public static LookupElementBuilder createCompletionVariant(SpringBeanPointer variant) {
        String name = variant.getName();
        PsiClass beanClass = variant.getBeanClass();
        if (name != null) {
            LookupElementBuilder element = LookupElementBuilder.create(variant, name);
            element.withIcon(variant.getBeanIcon());
            if (beanClass != null) {
                element.withTypeText(beanClass.getName());
            }
            return element;
        }
        return null;
    }

    public static class Local extends SpringBeanResolveConverter {
        @Nullable
        protected SpringModel getSpringModel(ConvertContext context) {
            return SpringManager.getInstance(context.getFile().getProject()).getLocalSpringModel(context.getFile());
        }
    }

    public static class PropertyBean extends SpringBeanResolveConverter {
        @Nullable
        public List<PsiClassType> getRequiredClasses(ConvertContext context) {
            return getValueClasses(context);
        }
    }

    public static class PropertyLocal extends Local {
        @Nullable
        public List<PsiClassType> getRequiredClasses(ConvertContext context) {
            return getValueClasses(context);
        }

        public void bindReference(
            GenericDomValue<SpringBeanPointer> genericValue,
            ConvertContext context,
            PsiElement newTarget
        ) {
            if (newTarget.getContainingFile() != context.getFile()) {
                RefBase ref = (RefBase) genericValue.getParent();
                assert ref != null;
                ref.getBean().setStringValue(genericValue.getStringValue());
                genericValue.undefine();
            }
        }
    }

    public static class Parent extends SpringBeanResolveConverter {
    }

    public static class Key extends SpringBeanResolveConverter {
        @Nullable
        public List<PsiClassType> getRequiredClasses(ConvertContext context) {
            SpringEntry entry = (SpringEntry) context.getInvocationElement().getParent();
            assert entry != null;
            PsiClass keyClass = entry.getRequiredKeyClass();
            return keyClass == null
                ? null
                : Collections.singletonList(JavaPsiFacade.getInstance(keyClass.getProject()).getElementFactory().createType(keyClass));
        }
    }

    private static class TryParsingCustomBeansFix implements LocalQuickFix, IntentionAction {
        private final Collection<XmlTag> myTags;

        public TryParsingCustomBeansFix(Collection<XmlTag> tags) {
            myTags = tags;
        }

        @Nonnull
        public LocalizeValue getText() {
            return getName();
        }

        public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
            return true;
        }

        public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            ParseCustomBeanIntention.invokeCustomBeanParsers(project, myTags);
        }

        public boolean startInWriteAction() {
            return false;
        }

        @Nonnull
        @Override
        public LocalizeValue getName() {
            return SpringLocalize.tryParsingCustomBeans();
        }

        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            throw new UnsupportedOperationException("Method applyFix is not yet implemented in " + getClass().getName());
        }
    }
}
