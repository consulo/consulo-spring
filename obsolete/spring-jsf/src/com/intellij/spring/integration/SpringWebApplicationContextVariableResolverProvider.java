package com.intellij.spring.integration;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Factory;
import com.intellij.psi.impl.source.jsp.el.impl.CustomJsfVariableResolverProvider;
import com.intellij.psi.jsp.JspImplicitVariable;
import com.intellij.spring.el.SpringBeansAsJsfVariableUtil;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpringWebApplicationContextVariableResolverProvider implements CustomJsfVariableResolverProvider {
  @NonNls private static final List<String> VARIABLE_RESOLVER_CLASSNAMES =
      Arrays.asList("org.springframework.web.jsf.WebApplicationContextVariableResolver", "org.springframework.web.jsf.el.WebApplicationContextFacesELResolver");

  @NonNls private static final String WEB_APPLICATION_CONTEXT_VARIABLE_NAME = "webApplicationContext";


  public boolean acceptVariableResolver(final String className) {
    return VARIABLE_RESOLVER_CLASSNAMES.contains(className);
  }

  public void addVars(final List<JspImplicitVariable> resultVars, final Module module) {
    final Factory<List<JspImplicitVariable>> factory = new Factory<List<JspImplicitVariable>>() {
      public List<JspImplicitVariable> create() {
        List<JspImplicitVariable> results = new ArrayList<JspImplicitVariable>();

        SpringBeansAsJsfVariableUtil.addVariables(results, module);

        return results;
      }
    };

    resultVars.add(ContextImplicitVariableFactory.getInstance(module).createContextVariable(WEB_APPLICATION_CONTEXT_VARIABLE_NAME, factory));
  }
}
