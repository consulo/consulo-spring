package com.intellij.spring.webflow.el.scopeProviders;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import consulo.util.dataholder.Key;
import com.intellij.spring.webflow.el.WebflowScope;
import com.intellij.spring.webflow.resources.messages.WebflowBundle;

import java.util.Map;

public class FlashScopeProvider extends BaseFileScopeProvider {
  private static final Key<CachedValue<Map<String, PsiElement>>> FLASH_SCOPE_VARIABLES_KEY = Key.create("FLASH_SCOPE_VARIABLES_KEY");

  public WebflowScope getScope() {
    return WebflowScope.FLASH;
  }

  protected Key<CachedValue<Map<String, PsiElement>>> getScopeVariablesKey() {
    return FLASH_SCOPE_VARIABLES_KEY;
  }

  protected String getTypeName() {
    return WebflowBundle.message("flash.scope.type.name");
  }
}
