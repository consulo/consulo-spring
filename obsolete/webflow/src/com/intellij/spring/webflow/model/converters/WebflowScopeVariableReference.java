package com.intellij.spring.webflow.model.converters;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.webflow.el.WebflowScopeProvider;
import com.intellij.spring.webflow.el.WebflowScopeProviderManager;
import com.intellij.util.xml.GenericDomValue;

/**
 * User: Sergey.Vasiliev
 */
public class WebflowScopeVariableReference extends PsiReferenceBase<PsiElement> {
  private final PsiElement myElement;
  private final String myScopeName;

  public WebflowScopeVariableReference(PsiElement element, TextRange range, GenericDomValue domValue,
                                       String scopeName) {
    super(element, range, true);
    myElement = element;
    myScopeName = scopeName;
  }

  public PsiElement resolve() {
    String value = getValue();

    Module module = myElement.getModule();
    assert module != null;

    WebflowScopeProviderManager service = WebflowScopeProviderManager.getService(module);

    WebflowScopeProvider provider = service.getProvider(myScopeName);

    if (provider == null) return null;

    return  provider.getOrCreateScopeVariable((XmlFile)myElement.getContainingFile(), value, getElement());
  }

  public Object[] getVariants() {
    return new Object[0];  // todo analyse type
  }
}
