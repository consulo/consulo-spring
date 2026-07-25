/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.values.converters;

import com.intellij.java.impl.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.converters.SpringConverterUtil;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringProperty;
import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.xml.dom.*;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FieldRetrievingFactoryBeanConverter extends Converter<String> implements CustomReferenceConverter<String> {
  private static final String FIELD_RETRIEVING_FACTORY_BEAN_CLASS = "org.springframework.beans.factory.config.FieldRetrievingFactoryBean";
  private static final String STATIC_FIELD_PROPERTY_NAME = "staticField";
  private final boolean mySoft;

  public FieldRetrievingFactoryBeanConverter() {
    this(true);
  }

  public FieldRetrievingFactoryBeanConverter(boolean soft) {
    mySoft = soft;
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public PsiReference[] createReferences(GenericDomValue<String> genericDomValue,
                                         PsiElement element,
                                         ConvertContext context) {
    return createReferences(genericDomValue, element);
  }

  @Nonnull
  @RequiredReadAction
  public PsiReference[] createReferences(GenericDomValue<String> genericDomValue, PsiElement element) {
    String stringValue = genericDomValue.getStringValue();
    if (stringValue == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    List<PsiReference> collectedReferences = new ArrayList<>();

    JavaClassReferenceProvider provider = new JavaClassReferenceProvider();
    provider.setSoft(mySoft);
    PsiReference[] javaClassReferences = provider.getReferencesByElement(element);

    PsiClass psiClass = null;
    for (PsiReference reference : javaClassReferences) {
      PsiElement psiElement = reference.resolve();
      if (psiElement == null) break;


      collectedReferences.add(reference);
      if (psiElement instanceof PsiClass) {
        psiClass = (PsiClass)psiElement;
      }
    }

    if (psiClass == null || stringValue.endsWith(psiClass.getName())) return javaClassReferences;

    collectedReferences.add(createFieldReference(psiClass, element, stringValue, genericDomValue));

    return collectedReferences.toArray(new PsiReference[collectedReferences.size()]);
  }

  @RequiredReadAction
  private PsiReference createFieldReference(final PsiClass psiClass,
                                            final PsiElement element,
                                            String stringValue,
                                            final GenericDomValue<String> genericDomValue) {
    String className = psiClass.getName();
    assert className != null;
    int i = stringValue.indexOf(className) + className.length();
    final String fieldName = stringValue.substring(i + 1).trim();

    final TextRange textRange = fieldName.length() == 0
      ? TextRange.from(element.getText().indexOf(className) + className.length() + 1, 0)
      : TextRange.from(element.getText().indexOf(fieldName), fieldName.length());

    return new PsiReferenceBase<>(element, textRange, mySoft) {
      @Override
      @RequiredReadAction
      public PsiElement resolve() {
        if (fieldName.length() != 0) {
          PsiField[] psiFields = psiClass.getFields();
          for (PsiField psiField : psiFields) {
            if (psiField.isPublic() && psiField.isStatic() && fieldName.equals(psiField.getName())) {
              return psiField;
            }
          }
        }
        return null;
      }

      @Override
      public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException {
        if (element instanceof PsiField) {
          PsiField field = (PsiField)element;
          genericDomValue.setStringValue(field.getContainingClass().getQualifiedName() + "." + field.getName());
        }
        return getElement();
      }

      @Override
      @RequiredReadAction
      public Object[] getVariants() {
        List<String> staticFields = new ArrayList<>();
        PsiField[] psiFields = psiClass.getFields();
        for (PsiField psiField : psiFields) {
          if (psiField.isPublic() && psiField.isStatic() && psiField.getName() != null) {
            staticFields.add(psiField.getName());
          }
        }
        return ArrayUtil.toStringArray(staticFields);
      }
    };
  }

  @Override
  public String fromString(@Nullable String s, ConvertContext context) {
    return s;
  }

  @Override
  public String toString(@Nullable String s, ConvertContext context) {
    return s;
  }

  public static class FactoryClassCondition implements Predicate<GenericDomValue> {
    @Override
    public boolean test(GenericDomValue context) {
      return checkBeanClass(context);
    }
  }

  public static class FactoryClassAndPropertyCondition implements Predicate<Pair<PsiType, GenericDomValue>> {
    @Override
    public boolean test(Pair<PsiType, GenericDomValue> pair) {
      GenericDomValue element = pair.getSecond();
      return checkBeanClass(element) && checkPropertyName(element);
    }
  }

  private static boolean checkBeanClass(DomElement element) {
    return isFieldRetrivingFactoryBean(SpringConverterUtil.getCurrentBean(element));
  }

  private static boolean checkPropertyName(DomElement element) {
    SpringProperty springProperty = element.getParentOfType(SpringProperty.class, false);
    return springProperty != null && STATIC_FIELD_PROPERTY_NAME.equals(springProperty.getName().getStringValue());
  }

  public static boolean isFieldRetrivingFactoryBean(@Nullable CommonSpringBean springBean) {
    if (springBean == null) return false;

    PsiClass beanClass = springBean.getBeanClass();

    return beanClass != null && FIELD_RETRIEVING_FACTORY_BEAN_CLASS.equals(beanClass.getQualifiedName());
  }

  public static boolean isResolved(Project project, String field) {
    PsiManager psiManager = PsiManager.getInstance(project);

    int index = field.lastIndexOf(".");
    if (index <= 0) return false;

    String className = field.substring(0, index);
    String fieldName = field.substring(index + 1);

    if (StringUtil.isEmpty(fieldName) || StringUtil.isEmpty(className)) return false;

    PsiClass psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(className, GlobalSearchScope.allScope(project));
    if (psiClass != null) {
      for (PsiField psiField : psiClass.getFields()) {
        if (psiField.isStatic() && fieldName.equals(psiField.getName())) {
          return true;
        }
      }
    }
    return false;
  }
}
