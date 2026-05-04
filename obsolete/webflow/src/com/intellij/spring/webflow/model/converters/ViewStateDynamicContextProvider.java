package com.intellij.spring.webflow.model.converters;

import com.intellij.openapi.paths.DynamicContextProvider;
import com.intellij.psi.PsiElement;
import com.intellij.spring.webflow.model.xml.WebflowDomElement;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;

public class ViewStateDynamicContextProvider implements DynamicContextProvider {
  @NonNls public static String[] prefixes = new String[]{"forward:", "redirect:"};

  public int getOffset(final PsiElement psiElement, int offset, final String elementText) {
    if (elementText != null) {
      for (String prefix : prefixes) {
        if (elementText.contains(prefix)) {
          final DomElement element = DomUtil.getDomElement(psiElement);
          if (element != null && element.getParentOfType(WebflowDomElement.class, false) != null) {
            return offset + prefix.length();
          }
        }
      }
    }
                                                                              
    return offset;
  }
}
