package com.intellij.spring.webflow.el.scopeProviders;

import consulo.util.dataholder.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.spring.webflow.el.WebflowScope;
import com.intellij.spring.webflow.resources.messages.WebflowBundle;

import java.util.Map;

public class FlowScopeProvider extends BaseFileScopeProvider {
  private static final Key<CachedValue<Map<String, PsiElement>>> FLOW_SCOPE_VARIABLES_KEY = Key.create("FLOW_SCOPE_VARIABLES_KEY");

  public WebflowScope getScope() {
    return WebflowScope.FLOW;
  }

  protected Key<CachedValue<Map<String, PsiElement>>> getScopeVariablesKey() {
    return FLOW_SCOPE_VARIABLES_KEY;
  }

  protected String getTypeName() {
    return WebflowBundle.message("flow.scope.type.name");
  }
}
