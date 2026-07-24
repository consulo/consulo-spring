/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.xml.dom.*;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SpringResourceConverter extends Converter<PsiFile> implements CustomReferenceConverter<PsiFile> {

  public PsiFile fromString(@Nullable String s, ConvertContext context) {
    if (s != null) {
      GenericAttributeValue<PsiFile> element = (GenericAttributeValue<PsiFile>)context.getInvocationElement();
      PsiReference[] references = createReferences(element, element.getXmlAttributeValue(), context);
      if (references.length > 0) {
        PsiElement result = references[references.length - 1].resolve();
        if (result instanceof PsiFile) {
          return (PsiFile)result;
        }
      }
    }
    return null;
  }

  public String toString(@Nullable PsiFile psiFile, ConvertContext context) {
      return null;
  }

  @Nonnull
  public PsiReference[] createReferences(GenericDomValue<PsiFile> genericDomValue, PsiElement element, ConvertContext context) {
    String s = genericDomValue.getStringValue();
    if (s == null || element == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    return ResourceResolverUtils.getReferences(element, s, s.startsWith("/"), false);
  }
}
