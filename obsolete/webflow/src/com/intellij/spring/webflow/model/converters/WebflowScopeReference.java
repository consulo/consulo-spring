package com.intellij.spring.webflow.model.converters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.containers.ContainerUtil;
import java.util.function.Function;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.module.Module;
import com.intellij.spring.webflow.el.WebflowScopeProvider;
import com.intellij.spring.webflow.el.WebflowScopeProviderManager;

import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class WebflowScopeReference extends PsiReferenceBase<PsiElement> {
  private final PsiElement myElement;
  private final GenericDomValue myDomValue;

  public WebflowScopeReference(final PsiElement element, final TextRange range, final GenericDomValue domValue) {
    super(element, range, true);
    myElement = element;
    myDomValue = domValue;
  }

  public PsiElement resolve() {
    return new FakePsiElement() {
      public PsiElement getParent() {
        return myElement;
      }
    };
  }

  public Object[] getVariants() {
    final Module module = myDomValue.getModule();

    assert module != null;

    final List<WebflowScopeProvider> list =
        WebflowScopeProviderManager.getService(module).getAvailableProviders(myDomValue);

    return ContainerUtil.map2Array(list, new Function<WebflowScopeProvider, Object>() {
      public Object fun(final WebflowScopeProvider provider) {
        return provider.getScope().getName();
      }
    });
  }
}
