/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PsiFormatUtil;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.ResolvedConstructorArgs;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.actions.generate.SpringTemplateBuilder;
import com.intellij.spring.impl.ide.model.converters.SpringBeanUtil;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.ConstructorArg;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiCompiledElement;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;
import consulo.virtualFileSystem.ReadonlyStatusHandler;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomUtil;
import consulo.xml.dom.editor.DomElementAnnotationHolder;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtensionImpl
public class SpringConstructorArgInspection extends SpringBeanInspectionBase {
    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return SpringLocalize.modelInspectionBeanConstructorArg();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "SpringBeanConstructorArgInspection";
    }

    protected void checkBean(
        SpringBean springBean,
        Beans beans,
        DomElementAnnotationHolder holder,
        SpringModel springModel,
        Object state
    ) {
        PsiClass beanClass = springBean.getBeanClass();
        if (beanClass != null) {
            checkConstructorResolve(springBean, holder, beanClass);
            checkConstructorArgType(springBean, holder);
        }
        checkConstructorArgIndexes(springBean, holder);
    }

    // checks if instantiation method matches the args
    private static void checkConstructorResolve(
        @Nonnull SpringBean springBean,
        DomElementAnnotationHolder holder,
        @Nonnull PsiClass beanClass
    ) {
        if (springBean.isAbstract()) {
            return;
        }

        ResolvedConstructorArgs resolvedArgs = springBean.getResolvedConstructorArgs();

        if (!resolvedArgs.isResolved()) {
            boolean instantiatedByFactory = isInstantiatedByFactory(springBean);
            LocalizeValue message = instantiatedByFactory
                ? SpringLocalize.cannotFindFactoryMethodWithParametersCount(beanClass.getName())
                : SpringLocalize.cannotFindBeanConstructorWithParametersCount(beanClass.getName());

            DomElement element;
            if (!instantiatedByFactory && DomUtil.hasXml(springBean.getClazz())) {
                element = springBean.getClazz();
            }
            else if (instantiatedByFactory && DomUtil.hasXml(springBean.getFactoryMethod())) {
                element = springBean.getFactoryMethod();
            }
            else {
                element = springBean;
            }

            List<LocalQuickFix> fixes = new ArrayList<LocalQuickFix>();

            SpringBean stableCopy = springBean.createStableCopy();
            if (!instantiatedByFactory && !(beanClass instanceof PsiCompiledElement)) {
                fixes.add(createConstructorQuickFix(stableCopy, beanClass));
            }
            fixes.addAll(getConstructroArgsQuickFixes(stableCopy, beanClass.getConstructors()));

            holder.createProblem(element, HighlightSeverity.ERROR, message.get(), fixes.toArray(new LocalQuickFix[fixes.size()]));
        }
    }

    private static boolean isInstantiatedByFactory(SpringBean springBean) {
        return springBean.getFactoryMethod().getXmlAttribute() != null;
    }

    private static void checkConstructorArgType(SpringBean springBean, DomElementAnnotationHolder holder) {
        List<ConstructorArg> list = SpringUtils.getConstructorArgs(springBean);
        if (list.size() == 0) {
            return;
        }
        List<PsiMethod> instantiationMethods = SpringBeanUtil.getInstantiationMethods(springBean);
        args:
        for (ConstructorArg arg : list) {
            PsiType argType = arg.getType().getValue();
            if (argType == null) {
                continue;
            }
            Integer index = arg.getIndex().getValue();
            boolean parameterFound = false;
            if (index != null) {
                int i = index.intValue();
                if (i < 0) {
                    continue;
                }
                for (PsiMethod method : instantiationMethods) {
                    PsiParameter[] parameters = method.getParameterList().getParameters();
                    if (i < parameters.length) {
                        parameterFound = true;
                        if (parameters[i].getType().isAssignableFrom(argType)) {
                            continue args;
                        }
                    }
                }
            }
            else {
                for (PsiMethod method : instantiationMethods) {
                    PsiParameter[] parameters = method.getParameterList().getParameters();
                    for (PsiParameter param : parameters) {
                        if (param.getType().isAssignableFrom(argType)) {
                            continue args;
                        }
                    }
                }
            }
            if (parameterFound) {
                LocalizeValue message = SpringLocalize.constructorArgIncorrectValueType();
                holder.createProblem(arg.getType(), message.get());
            }
        }
    }

    private static void checkConstructorArgIndexes(SpringBean springBean, DomElementAnnotationHolder holder) {
        List<ConstructorArg> list = SpringUtils.getConstructorArgs(springBean);

        Map<Integer, ConstructorArg> argsMap = new HashMap<Integer, ConstructorArg>();
        ArrayList<ConstructorArg> firstSeen = new ArrayList<ConstructorArg>();
        for (ConstructorArg arg : list) {
            Integer index = arg.getIndex().getValue();
            if (index != null) {
                ConstructorArg previous = argsMap.put(index, arg);
                if (previous != null) {
                    reportNotUniqueIndex(holder, arg);
                    if (!firstSeen.contains(previous)) {
                        reportNotUniqueIndex(holder, previous);
                        firstSeen.add(previous);
                    }
                }
            }
        }
    }

    private static void reportNotUniqueIndex(DomElementAnnotationHolder holder, ConstructorArg arg) {
        LocalizeValue message = SpringLocalize.incorrectConstructorArgIndexNotUnique();
        holder.createProblem(arg.getIndex(), message.get());
    }

    private static LocalQuickFix createConstructorQuickFix(final SpringBean springBean, final PsiClass beanClass) {
        return new LocalQuickFix() {
            @Nonnull
            @Override
            public LocalizeValue getName() {
                return SpringLocalize.modelCreateConstructorQuickfixMessage(getSignature(springBean));
            }

            private String getSignature(SpringBean springBean) {
                String params = SpringConstructorArgResolveUtil.suggestParamsForConstructorArgsAsString(springBean);
                return beanClass.getName() + "(" + params + ")";
            }

            public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
                PsiClass beanClass = springBean.getBeanClass();
                try {
                    assert beanClass != null;
                    if (ReadonlyStatusHandler.getInstance(project)
                        .ensureFilesWritable(beanClass.getContainingFile().getVirtualFile())
                        .hasReadonlyFiles()) {
                        return;
                    }
                    PsiElementFactory elementFactory = JavaPsiFacade.getInstance(beanClass.getProject()).getElementFactory();

                    PsiMethod constructor = elementFactory.createConstructor();
                    List<PsiParameter> parameters = SpringConstructorArgResolveUtil.suggestParamsForConstructorArgs(springBean);
                    for (PsiParameter parameter : parameters) {
                        constructor.getParameterList().add(parameter);
                    }

                    beanClass.add(constructor);
                }
                catch (IncorrectOperationException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static List<LocalQuickFix> getConstructroArgsQuickFixes(@Nonnull SpringBean springBean, @Nonnull PsiMethod[] ctors) {
        List<LocalQuickFix> quickFixes = new ArrayList<LocalQuickFix>();

        if (SpringUtils.getConstructorArgs(springBean).size() == 0) {
            for (PsiMethod ctor : ctors) {
                if (ctor.getParameterList().getParametersCount() > 0) {
                    quickFixes.add(new AddConstructorArgQuickFix(ctor, springBean));
                }
            }
        }

        return quickFixes;
    }

    public static class AddConstructorArgQuickFix implements LocalQuickFix {
        private final SpringBean mySpringBean;
        private final String myMethodName;
        private final SmartPsiElementPointer<PsiMethod> myPointer;

        public AddConstructorArgQuickFix(PsiMethod ctor, SpringBean springBean) {
            myPointer = SmartPointerManager.getInstance(ctor.getProject()).createSmartPsiElementPointer(ctor);
            mySpringBean = springBean;
            myMethodName = PsiFormatUtil
                .formatMethod(ctor, PsiSubstitutor.EMPTY, PsiFormatUtil.SHOW_NAME | PsiFormatUtil.SHOW_PARAMETERS, PsiFormatUtil.SHOW_TYPE);
        }

        @Nonnull
        @Override
        public LocalizeValue getName() {
            return SpringLocalize.modelAddConstructorArgsForMethodQuickfixMessage(myMethodName);
        }

        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            PsiMethod myCtor = myPointer.getElement();
            if (myCtor == null) {
                return;
            }
            @SuppressWarnings({"ConstantConditions"})
            PsiMethod[] myAllCtors = myCtor.getContainingClass().getConstructors();
            PsiParameter[] parameters = myCtor.getParameterList().getParameters();
            SpringModel model = SpringUtils.getSpringModel(mySpringBean);
            SpringTemplateBuilder builder = new SpringTemplateBuilder(project);
            Editor editor = SpringTemplateBuilder.getEditor(descriptor);
            SpringTemplateBuilder.preparePlace(editor, project, mySpringBean.addConstructorArg());

            for (int i = 0; i < parameters.length; i++) {
                PsiParameter parameter = parameters[i];
                builder.addTextSegment("<constructor-arg");

                if (parameters.length > 1) {
                    builder.addTextSegment(" index=\"" + i + "\"");
                }
                for (PsiMethod ctor : myAllCtors) {
                    if (ctor == myCtor) {
                        continue;
                    }
                    PsiParameter[] params = ctor.getParameterList().getParameters();
                    if (params.length == parameters.length) {
                        builder.addTextSegment(" type=\"" + parameters[i].getType().getCanonicalText() + "\"");
                        break;
                    }
                }
                builder.createValueAndClose(parameter.getType(), model, "constructor-arg");
            }
            builder.startTemplate(editor);
        }
    }
}