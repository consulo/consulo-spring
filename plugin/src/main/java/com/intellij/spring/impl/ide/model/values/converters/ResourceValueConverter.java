/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.values.converters;

import com.intellij.java.language.psi.PsiArrayType;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.converters.ResourceResolverUtils;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.ElementManipulators;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.util.lang.function.Predicates;
import consulo.xml.dom.ConvertContext;
import consulo.xml.dom.Converter;
import consulo.xml.dom.CustomReferenceConverter;
import consulo.xml.dom.GenericDomValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class ResourceValueConverter extends Converter<Object> implements CustomReferenceConverter {
  @Override
  @RequiredReadAction
  public Object fromString(@Nullable String s, ConvertContext context) {
    GenericDomValue domValue = (GenericDomValue)context.getInvocationElement();
    return StringUtil.isEmpty(s)
      ? Collections.emptySet()
      : ResourceResolverUtils.addResourceFilesFrom(domValue, s, new HashSet<>(), Predicates.alwaysTrue());
  }

  @Override
  public String toString(@Nullable Object o, ConvertContext context) {
    return null;
  }

  @Nonnull
  @Override
  public PsiReference[] createReferences(GenericDomValue genericDomValue, PsiElement element, ConvertContext context) {
    List<PsiReference> result = new ArrayList<>();
    int startInElement = ElementManipulators.getOffsetInElement(element);
    ResourceResolverUtils.processSeparatedString(genericDomValue.getStringValue(), ",", (s, offset) -> {
      result.addAll(Arrays.asList(ResourceResolverUtils.getReferences(element, s, true, false, offset + startInElement, true)));
      return true;
    });
    return result.isEmpty() ? PsiReference.EMPTY_ARRAY : result.toArray(new PsiReference[result.size()]);
  }

  public static class ResourceValueConverterCondition implements Predicate<Pair<PsiType, GenericDomValue>> {
    @Override
    public boolean test(Pair<PsiType, GenericDomValue> pair) {
      PsiType psiType = pair.getFirst();
      if (psiType instanceof PsiArrayType arrayType) {
        psiType = arrayType.getComponentType();
      }
      return psiType != null && "org.springframework.core.io.Resource".equals(psiType.getCanonicalText());
    }
  }
}