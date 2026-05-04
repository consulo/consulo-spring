/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.security.model.xml.converters;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.security.references.SpringSecurityRolePsiReference;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
public class SpringSecurityRolesConverter extends Converter<List<String>> implements CustomReferenceConverter<List<String>> {

  public String toString(final List<String> strings, final ConvertContext context) {
    return StringUtil.join(strings, ",");
  }

  public List<String> fromString(final String s, final ConvertContext context) {
    if (s == null) return null;

    return SpringUtils.tokenize(s);
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<List<String>> genericDomValue,
                                         final PsiElement element,
                                         final ConvertContext context) {
    final List<String> roleNames = genericDomValue.getValue();
    Module module = context.getModule();
    if (roleNames != null && module != null) {
      List<PsiReference> references = new ArrayList<PsiReference>();
      for (int i = 0; i < roleNames.size(); i++) {
        references.add(createReference(element, module, roleNames.get(i), i));
      }
      return references.toArray(new PsiReference[references.size()]);
    }
    return PsiReference.EMPTY_ARRAY;
  }

  protected SpringSecurityRolePsiReference createReference(PsiElement element, @NotNull Module module, String roleName, int i) {
    return new SpringSecurityRolePsiReference(element, roleName, module);
  }

}