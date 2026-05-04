/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.security.model.xml.converters;

import consulo.util.lang.function.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiClass;
import com.intellij.spring.model.xml.beans.ConstructorArg;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.security.references.SpringSecurityRolePsiReferenceProvider;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SecurityRoleConstructorArgConverter extends Converter<Object> implements CustomReferenceConverter {

  public Object fromString(@Nullable @NonNls String s, final ConvertContext context) {
    return s;
  }

  public String toString(@Nullable Object o, final ConvertContext context) {
    return null;
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue genericDomValue, final PsiElement element, final ConvertContext context) {
    String stringValue = genericDomValue.getStringValue();
    return SpringSecurityRolePsiReferenceProvider.getSecurityRolesReferences(element, stringValue);
  }

  public static class SecurityRoleConstructorArgCondition implements Condition<Pair<PsiType, GenericDomValue>> {
    public boolean value(Pair<PsiType, GenericDomValue> pair) {
      GenericDomValue genericDomValue = pair.getSecond();
      ConstructorArg arg = DomUtil.getParentOfType(genericDomValue, ConstructorArg.class, false);
      if (arg != null) {
        DomSpringBean springBean = DomUtil.getParentOfType(arg, DomSpringBean.class, false);
        if (springBean != null) {
          PsiClass psiClass = springBean.getBeanClass();
          return psiClass != null && SpringSecurityClassesConstants.GRANTED_AUTHORITY.equals(psiClass.getQualifiedName());
        }
      }
      return false;
    }
  }
}