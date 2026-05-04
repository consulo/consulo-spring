package com.intellij.spring.security.model.converters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ElementManipulators;
import com.intellij.spring.model.converters.ResourceResolverUtils;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

/**
 * @author Serega.Vasiliev
 */
public class SpringSecurityResourceConverter implements CustomReferenceConverter<String> {

  @NotNull
  public PsiReference[] createReferences(GenericDomValue<String> value, PsiElement element, ConvertContext context) {
    final String s = value.getStringValue();
    if (s == null || element == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    return ResourceResolverUtils.getReferences(element, s, s.startsWith("/"), false, ElementManipulators.getOffsetInElement(element), true);
  }
}
