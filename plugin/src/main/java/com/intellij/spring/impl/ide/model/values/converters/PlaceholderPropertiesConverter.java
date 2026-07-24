package com.intellij.spring.impl.ide.model.values.converters;

import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.values.PlaceholderUtils;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.util.lang.Pair;
import consulo.util.lang.function.Condition;
import consulo.xml.dom.ConvertContext;
import consulo.xml.dom.Converter;
import consulo.xml.dom.CustomReferenceConverter;
import consulo.xml.dom.GenericDomValue;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class PlaceholderPropertiesConverter extends Converter<String> implements CustomReferenceConverter {

  public String fromString(@Nullable @NonNls String s, ConvertContext context) {
    return s;
  }

  public String toString(@Nullable String s, ConvertContext context) {
    return s;
  }

  @Nonnull
  public PsiReference[] createReferences(GenericDomValue genericDomValue, PsiElement element, ConvertContext context) {
    return PlaceholderUtils.createPlaceholderPropertiesReferences(genericDomValue);
  }

  public static class PlaceholderPropertiesCondition implements Condition<Pair<PsiType, GenericDomValue>> {
    public boolean value(Pair<PsiType, GenericDomValue> pair) {
      return PlaceholderUtils.isPlaceholder(pair.getSecond());
    }
  }

}
