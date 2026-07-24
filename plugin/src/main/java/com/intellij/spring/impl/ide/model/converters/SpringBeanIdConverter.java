/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.values.converters.FieldRetrievingFactoryBeanConverter;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.CustomBeanWrapper;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringValueHolderDefinition;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.util.IncorrectOperationException;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.StringUtil;
import consulo.xml.dom.ConvertContext;
import consulo.xml.dom.CustomReferenceConverter;
import consulo.xml.dom.DomUtil;
import consulo.xml.dom.GenericDomValue;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class SpringBeanIdConverter implements CustomReferenceConverter<String> {

  private static final FieldRetrievingFactoryBeanConverter.FactoryClassCondition CONDITION = new FieldRetrievingFactoryBeanConverter.FactoryClassCondition();
  private static final FieldRetrievingFactoryBeanConverter CONVERTER = new FieldRetrievingFactoryBeanConverter(true);

  @Nonnull
  public PsiReference[] createReferences(GenericDomValue<String> genericDomValue,
                                         PsiElement element,
                                         ConvertContext context) {
    if (genericDomValue.getParent() instanceof CustomBeanWrapper) return PsiReference.EMPTY_ARRAY;
    if (CONDITION.value((GenericDomValue)context.getInvocationElement())) {
      return CONVERTER.createReferences(genericDomValue, element, context);
    }
    return createDefaultReferences(genericDomValue, element);
  }

  private static PsiReference[] createDefaultReferences(final GenericDomValue<String> genericDomValue, final PsiElement element) {
    return new PsiReference[]{new PsiReferenceBase<PsiElement>(element) {

      public PsiElement resolve() {
        return getElement().getParent().getParent();
      }

      public boolean isSoft() {
        return true;
      }

      public Object[] getVariants() {
        DomSpringBean springBean = genericDomValue.getParentOfType(DomSpringBean.class, false);

        List<String> names = suggestUnusedBeanNames(springBean);

        names.addAll(Arrays.asList(SpringUtils.suggestBeanNames(springBean)));

        return ArrayUtil.toStringArray(names);
      }

      public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException {
        return element;
      }
    }};
  }

  private static List<String> suggestUnusedBeanNames(DomSpringBean springBean) {
    PsiClass beanClass = springBean.getBeanClass();
    PsiType psiType = beanClass == null ? null : JavaPsiFacade.getInstance(beanClass.getProject()).getElementFactory().createType(beanClass);

    SpringModel model = SpringUtils.getSpringModel(springBean);
    Collection<? extends SpringBeanPointer> list = model.getAllCommonBeans(true);

    List<String> unusedReferences = new ArrayList<String>();

    for (SpringBeanPointer pointer : list) {
      CommonSpringBean bean = pointer.getSpringBean();
      if (bean instanceof DomSpringBean) {
        DomSpringBean domSpringBean = (DomSpringBean)bean;
        for (SpringValueHolderDefinition definition : DomUtil.getDefinedChildrenOfType(domSpringBean, SpringValueHolderDefinition.class)) {
          GenericDomValue<SpringBeanPointer> refElement = definition.getRefElement();
          if (refElement != null && !StringUtil.isEmptyOrSpaces(refElement.getStringValue()) && refElement.getValue() == null) {
            String unusedBeanRef = refElement.getStringValue();
            if (psiType != null) {
              PsiType requiredType = SpringBeanUtil.getRequiredType(definition);
              if (requiredType != null && requiredType.isAssignableFrom(psiType)) {
                 unusedReferences.add(unusedBeanRef);
              }
            }  else {
              unusedReferences.add(unusedBeanRef);
            }
          }
        }
      }
    }

    return unusedReferences;
  }
}
