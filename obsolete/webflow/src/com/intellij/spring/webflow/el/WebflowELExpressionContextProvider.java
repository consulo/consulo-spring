package com.intellij.spring.webflow.el;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.impl.source.jsp.el.ELContextProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class WebflowELExpressionContextProvider implements ELContextProvider {
  private final PsiElement myHost;

  public WebflowELExpressionContextProvider(final PsiElement host) {
    myHost = host;
  }

  @Nullable
  public Iterator<? extends PsiVariable> getTopLevelElVariables(@Nullable final String nameHint) {
    return ELVariablesCollectorUtils.getImplicitVariables(myHost).iterator();
  }

  public boolean acceptsGetMethodForLastReference(final PsiMethod getter) {
    return true;
  }

  public boolean acceptsSetMethodForLastReference(final PsiMethod setter) {
    return false;
  }

  public boolean acceptsNonPropertyMethodForLastReference(final PsiMethod method) {
    if (isObjectClassMethod(method)) return false;

    return true;
  }

  private static boolean isObjectClassMethod(final PsiMethod method) {
    return CommonClassNames.JAVA_LANG_OBJECT.equals(method.getContainingClass().getQualifiedName());
  }
}