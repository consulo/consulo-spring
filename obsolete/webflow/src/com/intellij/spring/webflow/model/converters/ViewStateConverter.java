package com.intellij.spring.webflow.model.converters;

import com.intellij.openapi.paths.PathReferenceManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.spring.webflow.util.WebflowUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

public class ViewStateConverter  implements CustomReferenceConverter {

  @NotNull
  public PsiReference[] createReferences(GenericDomValue genericDomValue, PsiElement element, ConvertContext context) {
    String stringValue = genericDomValue.getStringValue();

    if (stringValue != null && stringValue.contains(WebflowUtil.WEBFLOW_EL_PREFIX)) return PsiReference.EMPTY_ARRAY;

    return PathReferenceManager.getInstance().createReferences(element, true);
  }

}
