/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import com.intellij.java.language.psi.PsiParameterList;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.model.ResolvedConstructorArgs;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import consulo.ide.IdeBundle;
import consulo.language.psi.EmptyResolveMessageProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.localize.LocalizeValue;
import consulo.xml.dom.ConvertContext;
import consulo.xml.dom.CustomReferenceConverter;
import consulo.xml.dom.GenericDomValue;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class ConstructorArgIndexConverter implements CustomReferenceConverter<Integer> {

  @Nonnull
  public PsiReference[] createReferences(GenericDomValue<Integer> index, PsiElement element, ConvertContext context) {

    PsiReferenceBase<PsiElement> ref = new MyReference(element, index, context);
    return new PsiReference[] { ref };
  }

  private static class MyReference extends PsiReferenceBase<PsiElement> implements EmptyResolveMessageProvider
  {
    private final GenericDomValue<Integer> myGenericDomValue;
    private final ConvertContext myContext;

    public MyReference(PsiElement element, GenericDomValue<Integer> index, ConvertContext context) {
      super(element);
      myGenericDomValue = index;
      myContext = context;
    }

    public PsiParameter resolve() {
      SpringBean bean = (SpringBean)SpringConverterUtil.getCurrentBean(myContext);
      return ConstructorArgIndexConverter.resolve(myGenericDomValue, bean);
    }

    public boolean isSoft() {
      return true;
    }

    public Object[] getVariants() {
      SpringBean bean = (SpringBean)SpringConverterUtil.getCurrentBean(myContext);
      List<PsiMethod> psiMethods = SpringBeanUtil.getInstantiationMethods(bean);
      int maxParams = 0;
      for (PsiMethod method: psiMethods) {
        PsiParameterList parameterList = method.getParameterList();
        maxParams = Math.max(maxParams, parameterList.getParametersCount());
      }
      if (maxParams > 0) {
        Object[] objects = new Object[maxParams];
        for (int i = 0; i < maxParams; i++) {
          // todo apply more descriptive completion variants
          objects[i] = Integer.toString(i);
        }
        return objects;
      }
      return EMPTY_ARRAY;
    }

    @Nonnull
    @Override
    public LocalizeValue buildUnresolvedMessage(@Nonnull String s) {
      Integer value = myGenericDomValue.getValue();
      if (value != null) {
        SpringBean bean = (SpringBean)SpringConverterUtil.getCurrentBean(myContext);
        PsiClass clazz = SpringBeanUtil.getInstantiationClass(bean);
        if (clazz != null) {
          return LocalizeValue.localizeTODO(SpringBeanUtil.isInstantiatedByFactory(bean) ?
                                              SpringBundle.message("cannot.find.factory.method.index", value, clazz.getQualifiedName()) :
                                              SpringBundle.message("cannot.find.constructor.arg.index.in.class",
                                                                   value, clazz.getQualifiedName()));
        }
        return LocalizeValue.localizeTODO(SpringBundle.message("cannot.find.constructor.arg.index", value));
      } else {
        return LocalizeValue.localizeTODO(IdeBundle.message("value.should.be.integer"));
      }
    }
  }

  @Nullable
  public static PsiParameter resolve(GenericDomValue<Integer> i, SpringBean bean) {
    Integer value = i.getValue();
    if (value != null) {
      int index = value.intValue();
      if (index >= 0) {
        ResolvedConstructorArgs resolvedArgs = bean.getResolvedConstructorArgs();
        PsiMethod resolvedMethod = resolvedArgs.getResolvedMethod();
        if (resolvedMethod != null) {
          return resolvedArgs.getResolvedArgs(resolvedMethod).get(i.getParent());
        } else {
          List<PsiMethod> checkedMethods = resolvedArgs.getCheckedMethods();
          if (checkedMethods != null) {
            for (PsiMethod method: checkedMethods) {
              PsiParameterList parameterList = method.getParameterList();
              if (parameterList.getParametersCount() > index) {
                return parameterList.getParameters()[index];
              }
            }
          }
        }
      }
    }
    return null;

  }
}
