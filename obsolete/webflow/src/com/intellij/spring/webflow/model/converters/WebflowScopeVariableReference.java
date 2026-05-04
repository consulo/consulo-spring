package com.intellij.spring.webflow.model.converters;

import com.intellij.openapi.module.Module;
import consulo.ide.impl.idea.openapi.module.ModuleUtil;
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

  public WebflowScopeVariableReference(final PsiElement element, final TextRange range, final GenericDomValue domValue,
                                       final String scopeName) {
    super(element, range, true);
    myElement = element;
    myScopeName = scopeName;
  }

  public PsiElement resolve() {
    final String value = getValue();

    final Module module = ModuleUtil.findModuleForPsiElement(myElement);
    assert module != null;

    final WebflowScopeProviderManager service = WebflowScopeProviderManager.getService(module);

    final WebflowScopeProvider provider = service.getProvider(myScopeName);

    if (provider == null) return null;

    return  provider.getOrCreateScopeVariable((XmlFile)myElement.getContainingFile(), value, getElement());
  }

  public Object[] getVariants() {
    return new Object[0];  // todo analyse type
  }
}
