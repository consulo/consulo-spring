package com.intellij.spring.webflow.el.scopeProviders;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import consulo.util.dataholder.Key;
import com.intellij.spring.webflow.el.WebflowScope;
import com.intellij.spring.webflow.resources.messages.WebflowBundle;

import java.util.Map;

public class RequestParametersProvider extends BaseFileScopeProvider{
  private static final Key<CachedValue<Map<String, PsiElement>>> REQUEST_PARAMETERS_VARIABLES_KEY = Key.create("REQUEST_PARAMETERS_VARIABLES_KEY");

  public WebflowScope getScope() {
    return WebflowScope.REQUEST_PATARAMETERS;
  }

  protected Key<CachedValue<Map<String, PsiElement>>> getScopeVariablesKey() {
    return REQUEST_PARAMETERS_VARIABLES_KEY;
  }

  protected String getTypeName() {
    return WebflowBundle.message("request.parameters.type.name");
  }

}
