package com.intellij.spring.webflow.el;

import com.intellij.openapi.module.Module;
import consulo.ide.impl.idea.openapi.module.ModuleServiceManager;
import com.intellij.spring.webflow.el.scopeProviders.*;
import java.util.function.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WebflowScopeProviderManager {
  private final List<WebflowScopeProvider> myProviders = new ArrayList<WebflowScopeProvider>();

  @NotNull
  public static WebflowScopeProviderManager getService(@NotNull Module module) {
    synchronized (module) {
      return ModuleServiceManager.getService(module, WebflowScopeProviderManager.class);
    }
  }

  public WebflowScopeProviderManager(@NotNull Module module) {
    // search scope algorithm: http://static.springframework.org/spring-webflow/docs/2.0.x/reference/html/ch03s06.html
    registerScopeProvider(new RequestScopeProvider());
    registerScopeProvider(new FlashScopeProvider());
    registerScopeProvider(new ViewScopeProvider(module));
    registerScopeProvider(new FlowScopeProvider());
    registerScopeProvider(new ConversationScopeProvider());
    registerScopeProvider(new RequestParametersProvider());
  }

  public List<WebflowScopeProvider> getAvailableProviders(@Nullable final DomElement domElement) {
    return ContainerUtil.mapNotNull(myProviders, new Function<WebflowScopeProvider, WebflowScopeProvider>() {
      public WebflowScopeProvider fun(final WebflowScopeProvider webflowScopeProvider) {
        return webflowScopeProvider.getScopes(domElement).size() > 0 ? webflowScopeProvider : null;
      }
    });
  }

  public List<WebflowScopeProvider> getProviders() {
    return myProviders;
  }

  @Nullable
  public WebflowScopeProvider getProvider(final WebflowScope scope) {
    for (WebflowScopeProvider provider : myProviders) {
      if (provider.getScope().equals(scope)) return provider;
    }
    return null;
  }

  @Nullable
  public WebflowScopeProvider getProvider(final String scopeName) {
    for (WebflowScopeProvider provider : myProviders) {
      if (provider.getScope().getName().equals(scopeName)) return provider;
    }
    return null;
  }

  public void registerScopeProvider(final WebflowScopeProvider provider) {
    myProviders.add(provider);
  }
}
