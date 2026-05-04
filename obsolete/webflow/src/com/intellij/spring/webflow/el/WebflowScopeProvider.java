package com.intellij.spring.webflow.el;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface WebflowScopeProvider {
  WebflowScope getScope();

  boolean accept(@Nullable DomElement domElement);

  @NotNull
  Set<DomElement> getScopes(@Nullable DomElement domElement);

  @Nullable
  PsiElement getOrCreateScopeVariable(final XmlFile psiFile, final String varName, final PsiElement host);
}
