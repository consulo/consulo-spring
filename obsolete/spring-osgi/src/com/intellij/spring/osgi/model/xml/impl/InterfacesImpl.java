package com.intellij.spring.osgi.model.xml.impl;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.osgi.model.xml.Interfaces;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class InterfacesImpl implements Interfaces {
  @NotNull
  public List<? extends PsiType> getRequiredTypes() {
    final Project project = getManager().getProject();

    return Collections.singletonList(PsiType.getJavaLangClass(PsiManager.getInstance(project), GlobalSearchScope.allScope(project)));
  }
}
