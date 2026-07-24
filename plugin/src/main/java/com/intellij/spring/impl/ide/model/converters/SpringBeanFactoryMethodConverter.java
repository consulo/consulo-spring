/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.language.psi.*;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.highlighting.SpringAutowireUtil;
import com.intellij.spring.impl.ide.model.highlighting.SpringConstructorArgResolveUtil;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.ConstructorArg;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;
import consulo.xml.dom.ConvertContext;
import consulo.xml.dom.GenericDomValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SpringBeanFactoryMethodConverter extends SpringBeanMethodConverter {
    @Nullable
    protected PsiClass getPsiClass(ConvertContext context) {
        SpringBean springBean = (SpringBean) SpringConverterUtil.getCurrentBean(context);
        return getFactoryClass(springBean);
    }

    protected MethodAccepter getMethodAccepter(ConvertContext context, final boolean forCompletion) {
        SpringBean springBean = (SpringBean) SpringConverterUtil.getCurrentBean(context);
        assert springBean != null;
        SpringModel model = SpringUtils.getSpringModel(springBean);
        final boolean fromFactoryBean = springBean.getFactoryBean().getValue() != null;
        final boolean autowire = SpringAutowireUtil.isConstructorAutowire(springBean);
        final Set<ConstructorArg> args = springBean.getAllConstructorArgs();

        return new MethodAccepter() {
            public boolean accept(PsiMethod psiMethod) {
                if (psiMethod.isConstructor() || psiMethod.getReturnType() == null) {
                    return false;
                }

                String containingClass = psiMethod.getContainingClass().getQualifiedName();
                return (!forCompletion || containingClass == null || !containingClass.equals(CommonClassNames.JAVA_LANG_OBJECT)) &&
                    isValidFactoryMethod(psiMethod, fromFactoryBean) &&
                    (forCompletion || SpringConstructorArgResolveUtil.acceptMethodByAutowire(
                        autowire,
                        args,
                        psiMethod.getParameterList().getParameters()
                    ));
            }
        };
    }

    // gets methods matching the method name
    @Nonnull
    public static List<PsiMethod> getFactoryMethodCandidates(@Nonnull SpringBean springBean, @Nonnull String methodName) {
        PsiClass factoryClass = getFactoryClass(springBean);
        if (factoryClass != null) {
            PsiMethod[] methods;
            if (factoryClass.isEnum()) {
                MethodResolveProcessor processor = new MethodResolveProcessor(methodName);
                factoryClass.processDeclarations(processor, ResolveState.initial(), null, factoryClass);
                methods = processor.getMethods();
            }
            else {
                methods = factoryClass.findMethodsByName(methodName, true);
            }
            if (methods.length > 0) {
                ArrayList<PsiMethod> result = new ArrayList<PsiMethod>(methods.length);
                boolean fromFactoryBean = springBean.getFactoryBean().getValue() != null;
                for (PsiMethod method : methods) {
                    if (isValidFactoryMethod(method, fromFactoryBean)) {
                        result.add(method);
                    }
                }
                return result;
            }
        }
        return Collections.emptyList();
    }

    public static boolean isValidFactoryMethod(PsiMethod psiMethod, boolean fromFactoryBean) {
        if (psiMethod.isConstructor() || psiMethod.getReturnType() == null) {
            return false;
        }

        boolean isStatic = isStatic(psiMethod);
        return isPublic(psiMethod) &&
            (fromFactoryBean && !isStatic || !fromFactoryBean && isStatic) &&
            isProperReturnType(psiMethod);
    }

    @Nullable
    public static PsiClass getFactoryClass(SpringBean springBean) {
        SpringBeanPointer factoryBeanPointer = springBean.getFactoryBean().getValue();
        if (factoryBeanPointer == null) {
            return springBean.getBeanClass(false);
        }
        else {
            CommonSpringBean factoryBean = factoryBeanPointer.getSpringBean();
            return factoryBean.equals(springBean) ? null : factoryBean.getBeanClass(false);
        }
    }

    public static boolean isPublic(PsiMethod psiMethod) {
        return psiMethod.hasModifierProperty(PsiModifier.PUBLIC);
    }

    public static boolean isStatic(PsiMethod psiMethod) {
        return psiMethod.hasModifierProperty(PsiModifier.STATIC);
    }

    public static boolean isProperReturnType(PsiMethod psiMethod) {
        PsiType returnType = psiMethod.getReturnType();
        return returnType instanceof PsiClassType;
    }

    public LocalQuickFix[] getQuickFixes(ConvertContext context) {
        List<LocalQuickFix> fixes = new ArrayList<LocalQuickFix>();
        SpringBean springBean = (SpringBean) SpringConverterUtil.getCurrentBean(context);
        GenericDomValue element = (GenericDomValue) context.getInvocationElement();

        String elementName = element.getStringValue();
        if (elementName != null && elementName.length() > 0) {
            PsiClass psiClass = getFactoryMethodClass(springBean);
            if (psiClass != null) {
                fixes.add(getCreateNewMethodQuickFix(springBean, psiClass, elementName));
            }

            return fixes.toArray(new LocalQuickFix[fixes.size()]);
        }

        return LocalQuickFix.EMPTY_ARRAY;
    }

    @Nullable
    private static PsiClass getFactoryMethodClass(SpringBean springBean) {
        SpringBeanPointer factoryBeanPointer = springBean.getFactoryBean().getValue();

        return factoryBeanPointer != null ? factoryBeanPointer.getSpringBean().getBeanClass(false) : springBean.getBeanClass(false);
    }

    private static LocalQuickFix getCreateNewMethodQuickFix(
        final SpringBean springBean,
        final PsiClass beanClass,
        final String elementName
    ) {
        return new LocalQuickFix() {
            @Nonnull
            public LocalizeValue getName() {
                return SpringLocalize.modelCreateFactoryMethodQuickfixMessage(getSignature(springBean, elementName));
            }

            public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
                try {
                    assert beanClass != null;
                    PsiElementFactory elementFactory = JavaPsiFacade.getInstance(beanClass.getProject()).getElementFactory();

                    String signature = getSignature(springBean, elementName) + "{ return null; }";

                    PsiMethod method = elementFactory.createMethodFromText(signature, null);

                    beanClass.add(method);
                }
                catch (IncorrectOperationException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @NonNls
    private static String getSignature(@Nonnull SpringBean springBean, @Nonnull String elementName) {
        boolean isStatic = springBean.getFactoryBean().getValue() == null;

        String params = SpringConstructorArgResolveUtil.suggestParamsForConstructorArgsAsString(springBean);
        PsiClass psiClass = springBean.getBeanClass();
        String returnType = psiClass == null ? "java.lang.String" : psiClass.getQualifiedName();

        StringBuilder signature = new StringBuilder();
        signature.append(PsiModifier.PUBLIC);
        signature.append(" ");
        signature.append(isStatic ? PsiModifier.STATIC : "");
        signature.append(" ");
        signature.append(returnType);
        signature.append(" ");
        signature.append(elementName);
        signature.append(" (");
        signature.append(params);
        signature.append(")");

        return signature.toString();
    }

}