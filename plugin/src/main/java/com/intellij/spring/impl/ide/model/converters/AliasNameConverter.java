/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.Alias;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.xml.dom.ConvertContext;
import consulo.xml.dom.CustomReferenceConverter;
import consulo.xml.dom.GenericDomValue;
import consulo.language.psi.PsiReferenceBase;

import jakarta.annotation.Nonnull;

@SuppressWarnings({"UnusedDeclaration"})
public class AliasNameConverter implements CustomReferenceConverter<String> {
  @Nonnull
  @Override
  public PsiReference[] createReferences(final GenericDomValue<String> genericDomValue,
                                         final PsiElement element,
                                         ConvertContext context) {
    return new PsiReference[]{new PsiReferenceBase<>(element) {
      @Override
      public PsiElement resolve() {
        return getElement().getParent().getParent();
      }

      @Override
      public boolean isSoft() {
        return true;
      }

      @Override
      public Object[] getVariants() {
        Alias alias = genericDomValue.getParentOfType(Alias.class, false);

        if (alias != null) {
          SpringBeanPointer beanPointer = alias.getAliasedBean().getValue();
          if (beanPointer != null) {
            return SpringUtils.suggestBeanNames(beanPointer.getSpringBean());
          }
        }
        return PsiReference.EMPTY_ARRAY;
      }
    }};
  }
}
