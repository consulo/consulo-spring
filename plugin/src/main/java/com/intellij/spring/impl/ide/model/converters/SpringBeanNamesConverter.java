/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.util.IncorrectOperationException;
import consulo.util.lang.StringUtil;
import consulo.xml.util.xml.ConvertContext;
import consulo.xml.util.xml.Converter;
import consulo.xml.util.xml.CustomReferenceConverter;
import consulo.xml.util.xml.GenericDomValue;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
public class SpringBeanNamesConverter extends Converter<List<String>> implements CustomReferenceConverter<List<String>> {

  public String toString(final List<String> strings, final ConvertContext context) {
    return StringUtil.join(strings, ",");
  }

  public List<String> fromString(final String s, final ConvertContext context) {
    if (s == null) return null;

    return SpringUtils.tokenize(s);
  }

  @Nonnull
  public PsiReference[] createReferences(final GenericDomValue<List<String>> genericDomValue,
                                         final PsiElement element,
                                         final ConvertContext context) {
    final List<String> strings = genericDomValue.getValue();
    if (strings != null) {
      List<PsiReference> references = new ArrayList<PsiReference>();
      for (String string : strings) {
        references.add(new PsiReferenceBase<PsiElement>(element, TextRange.from(element.getText().indexOf(string), string.length())) {

          public PsiElement resolve() {
            return getElement().getParent().getParent();
          }

          public boolean isSoft() {
            return true;
          }

          public Object[] getVariants() {
            return SpringUtils.suggestBeanNames(SpringConverterUtil.getCurrentBean(context));
          }

          public PsiElement handleElementRename(final String newElementName) throws IncorrectOperationException {
            final SpringBean bean = (SpringBean)genericDomValue.getParent();
            assert bean != null;
            bean.setName(newElementName);
            return element;
          }
        });
      }
      return references.toArray(new PsiReference[references.size()]);
    }
    return PsiReference.EMPTY_ARRAY;
  }
}