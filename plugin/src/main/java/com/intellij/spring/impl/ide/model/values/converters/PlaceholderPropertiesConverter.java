package com.intellij.spring.impl.ide.model.values.converters;

import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.values.PlaceholderUtils;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.util.lang.Pair;
import consulo.xml.dom.ConvertContext;
import consulo.xml.dom.Converter;
import consulo.xml.dom.CustomReferenceConverter;
import consulo.xml.dom.GenericDomValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.function.Predicate;

public class PlaceholderPropertiesConverter extends Converter<String> implements CustomReferenceConverter {
  @Override
  public String fromString(@Nullable String s, ConvertContext context) {
    return s;
  }

  @Override
  public String toString(@Nullable String s, ConvertContext context) {
    return s;
  }

  @Nonnull
  @Override
  public PsiReference[] createReferences(GenericDomValue genericDomValue, PsiElement element, ConvertContext context) {
    return PlaceholderUtils.createPlaceholderPropertiesReferences(genericDomValue);
  }

  public static class PlaceholderPropertiesCondition implements Predicate<Pair<PsiType, GenericDomValue>> {
    @Override
    public boolean test(Pair<PsiType, GenericDomValue> pair) {
      return PlaceholderUtils.isPlaceholder(pair.getSecond());
    }
  }
}
