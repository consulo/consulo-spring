/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.security.model.xml.converters;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class InterceptMethodConverter extends Converter<String> implements CustomReferenceConverter<String> {
  private final boolean mySoft;

  public InterceptMethodConverter() {
    this(true);
  }

  public InterceptMethodConverter(boolean soft) {
    mySoft = soft;
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<String> genericDomValue,
                                         final PsiElement element,
                                         final ConvertContext context) {
    return createReferences(genericDomValue, element);
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<String> genericDomValue, final PsiElement element) {
    final String stringValue = genericDomValue.getStringValue();
    if (stringValue == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    final PsiReference[] javaClassReferences = getJavaClassReferences(element);
    Set<PsiReference> resolved = new HashSet<PsiReference>();
    PsiClass psiClass = null;
    for (PsiReference reference : javaClassReferences) {
      final PsiElement psiElement = reference.resolve();
      if (psiElement == null) break;
      resolved.add(reference);

      if (psiElement instanceof PsiClass) {
        psiClass = (PsiClass)psiElement;
      }
    }

    if (psiClass == null || stringValue.endsWith(psiClass.getName())) return javaClassReferences;

    resolved.add(createMethodReference(psiClass, element, stringValue, genericDomValue));

    return ArrayUtil.toObjectArray(resolved, PsiReference.class);
  }

  private PsiReference[] getJavaClassReferences(PsiElement element) {
    final JavaClassReferenceProvider provider = new JavaClassReferenceProvider(element.getProject());
    provider.setSoft(true);
    return provider.getReferencesByElement(element);
  }

  private PsiReference createMethodReference(final PsiClass psiClass,
                                             final PsiElement element,
                                             final String stringValue,
                                             final GenericDomValue<String> genericDomValue) {
    final String className = psiClass.getName();
    final String methodName = stringValue.substring(stringValue.indexOf(className) + className.length() + 1).trim();

    final TextRange textRange = methodName.length() == 0
                                ? TextRange.from(element.getText().indexOf(className) + className.length() + 1, 0)
                                : TextRange.from(element.getText().indexOf(methodName), methodName.length());

    return new PsiReferenceBase<PsiElement>(element, textRange, true) {
      public PsiElement resolve() {
        if (methodName.length() != 0) {
          for (PsiMethod psiMethod : psiClass.getMethods()) {
            if (psiMethod.hasModifierProperty(PsiModifier.PUBLIC) && methodName.equals(psiMethod.getName())) {
              return psiMethod;
            }
          }
        }
        return null;
      }

      public Object[] getVariants() {
        List<PsiMethod> methods = new ArrayList<PsiMethod>();
        final PsiMethod[] psiMethods = psiClass.getAllMethods();
        for (PsiMethod method : psiMethods) {
          PsiClass containingClass = method.getContainingClass();
          if (method.hasModifierProperty(PsiModifier.PUBLIC) &&
            !method.isConstructor() &&
            containingClass != null &&
            !CommonClassNames.JAVA_LANG_OBJECT.equals(containingClass.getQualifiedName())) {
            methods.add(method);
          }
        }
        return ArrayUtil.toObjectArray(methods);
      }
    };
  }

  public String fromString(@Nullable @NonNls String s, final ConvertContext context) {
    return s;
  }

  public String toString(@Nullable String s, final ConvertContext context) {
    return s;
  }

}