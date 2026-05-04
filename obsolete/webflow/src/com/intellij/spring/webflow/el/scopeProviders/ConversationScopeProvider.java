package com.intellij.spring.webflow.el.scopeProviders;

import consulo.util.dataholder.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.spring.webflow.el.WebflowScope;
import com.intellij.spring.webflow.resources.messages.WebflowBundle;

import java.util.Map;

//
// http://static.springframework.org/spring-webflow/docs/2.0.x/reference/html/ch03s05.html#el-variable-conversationScope
//
public class ConversationScopeProvider extends BaseFileScopeProvider{
  private static final Key<CachedValue<Map<String, PsiElement>>> CONVERSATION_SCOPE_VARIABLES_KEY = Key.create("CONVERSATION_SCOPE_VARIABLES_KEY");

  public WebflowScope getScope() {
    return WebflowScope.CONVERSATION;
  }

  protected Key<CachedValue<Map<String, PsiElement>>> getScopeVariablesKey() {
    return CONVERSATION_SCOPE_VARIABLES_KEY;
  }

  protected String getTypeName() {
    return WebflowBundle.message("conversation.scope.type.name");
  }

}
