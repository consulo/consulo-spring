package com.intellij.spring.webflow.model.converters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.model.converters.ResourceResolverUtils;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WebflowBeanResourceConverter extends Converter<XmlFile> implements CustomReferenceConverter {

  public XmlFile fromString(final @Nullable String s, final ConvertContext context) {
    if (s != null) {
      final GenericAttributeValue<XmlFile> element = (GenericAttributeValue<XmlFile>)context.getInvocationElement();

        final PsiReference[] references = createReferences(element, element.getXmlAttributeValue(), context);
        if (references.length > 0) {
          PsiElement result = references[references.length - 1].resolve();
          if (result instanceof XmlFile) {
            return (XmlFile)result;
          }
        }
      }
    return null;
  }

  public String toString(final @Nullable XmlFile psiFile, final ConvertContext context) {
    return psiFile == null ? null : psiFile.getName();
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue genericDomValue, final PsiElement element, final ConvertContext context) {
    final String s = genericDomValue.getStringValue();
    if (s == null || element == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    return ResourceResolverUtils.getReferences(element, s, s.startsWith("/"), true);
  }

}

