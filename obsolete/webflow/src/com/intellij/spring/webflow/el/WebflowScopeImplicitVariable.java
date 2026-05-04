package com.intellij.spring.webflow.el;

import com.intellij.psi.impl.source.jsp.JspImplicitVariableImpl;
import com.intellij.psi.impl.source.jsp.el.impl.JspImplicitVariableWithCustomResolve;
import com.intellij.psi.impl.source.jsp.el.impl.ELElementProcessor;
import com.intellij.psi.jsp.JspImplicitVariable;
import com.intellij.psi.jsp.el.ELExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import com.intellij.openapi.util.Factory;

import java.util.List;


public class WebflowScopeImplicitVariable extends JspImplicitVariableImpl implements JspImplicitVariableWithCustomResolve {
  private final Factory<List<JspImplicitVariable>> myFactory;

  public WebflowScopeImplicitVariable(final WebflowScope scope, final PsiElement element, final Factory<List<JspImplicitVariable>> factory) {

    super(element, scope.getName(), PsiType.VOID, element, NESTED_RANGE);
    myFactory = factory;
  }

  public boolean process(final ELExpression element, final ELElementProcessor processor) {
    for (PsiVariable variable : myFactory.create()) {
      if (!processor.processVariable(variable)) {
        break;
      }
    }
    return true;
  }
}
