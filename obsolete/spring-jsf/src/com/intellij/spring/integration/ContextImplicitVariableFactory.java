package com.intellij.spring.integration;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import consulo.ide.impl.idea.openapi.module.ModuleServiceManager;
import com.intellij.openapi.util.Factory;
import com.intellij.psi.jsp.JspImplicitVariable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ContextImplicitVariableFactory implements Disposable {

  public static ContextImplicitVariableFactory getInstance(final Module module) {
      return ModuleServiceManager.getService(module, ContextImplicitVariableFactory.class);
  }

  @NotNull
  public abstract ContextImplicitVariable createContextVariable(@NotNull final String contextName,@NotNull  final Factory<List<JspImplicitVariable>> factory);
}

