/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.web;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.psi.impl.source.resolve.reference.impl.manipulators.XmlTagManipulator;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.converters.ResourceResolverUtils;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import consulo.util.lang.text.StringTokenizer;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Avdeev
 */
public class SpringWebReferenceContributor extends PsiReferenceContributor {

  public static final NamespaceFilter NAMESPACE_WEBAPP = new NamespaceFilter("http://java.sun.com/dtd/web-app_2_2.dtd",
                                                                             "http://java.sun.com/dtd/web-app_2_3.dtd",
                                                                             "http://java.sun.com/dtd/web-app_2_4.dtd",
                                                                             "http://java.sun.com/xml/ns/j2ee",
                                                                             "http://java.sun.com/xml/ns/javaee");

  @NonNls private static final String CONTEXT_PARAM = "context-param";
  @NonNls private static final String INIT_PARAM = "init-param";

  private static final PsiReferenceProviderBase PROVIDER = new PsiReferenceProviderBase() {
    @NotNull
    public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {

      PsiReference[] result = PsiReference.EMPTY_ARRAY;
      final XmlTag tag = (XmlTag)element;
      final XmlTag parent = tag.getParentTag();
      if (parent == null || !(parent.getName().equals(CONTEXT_PARAM) || parent.getName().equals(INIT_PARAM))) {
        return result;
      }
      final XmlTag nameTag = parent.findFirstSubTag("param-name");
      if (nameTag == null) {
        return result;
      }
      final String name = ElementManipulators.getValueText(nameTag);
      if (!name.equals(SpringWebConstants.CONTEXT_CONFIG_LOCATION)) {
        return result;
      }

      final TextRange[] ranges = XmlTagManipulator.getValueRanges(tag);
      for (TextRange range : ranges) {
        final String text = range.substring(element.getText());
        final StringTokenizer tokenizer = new StringTokenizer(text, SpringUtils.SPRING_DELIMITERS + "\n\t");
        while (tokenizer.hasMoreTokens()) {
          final String s = tokenizer.nextToken();
          final int end = tokenizer.getCurrentPosition();
          final int offset = end - s.length() + range.getStartOffset();
          final PsiReference[] references = ResourceResolverUtils.getReferences(tag, s, true, false, offset, false);
          result = ArrayUtil.mergeArrays(result, references, PsiReference.class);
        }       
      }
      return result;
    }
  };

  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {
    XmlUtil.registerXmlTagReferenceProvider(registrar, new String[] {"param-value"}, NAMESPACE_WEBAPP, true, PROVIDER);
  }
}
