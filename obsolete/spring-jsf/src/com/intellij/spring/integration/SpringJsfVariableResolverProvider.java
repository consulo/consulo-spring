/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package com.intellij.spring.integration;

import com.intellij.openapi.module.Module;
import com.intellij.psi.impl.source.jsp.el.impl.CustomJsfVariableResolverProvider;
import com.intellij.psi.jsp.JspImplicitVariable;
import com.intellij.spring.el.SpringBeansAsJsfVariableUtil;
import org.jetbrains.annotations.NonNls;

import java.util.Arrays;
import java.util.List;

public class SpringJsfVariableResolverProvider implements CustomJsfVariableResolverProvider {

  @NonNls public static List<String> VARIABLE_RESOLVER_CLASSNAMES = Arrays.asList("org.springframework.web.jsf.DelegatingVariableResolver",
                                                                                  "org.springframework.web.jsf.SpringBeanVariableResolver",
                                                                                  "org.springframework.web.jsf.el.SpringBeanFacesELResolver");

  public boolean acceptVariableResolver(final String className) {
    return VARIABLE_RESOLVER_CLASSNAMES.contains(className);
  }

  public void addVars(final List<JspImplicitVariable> resultVars, final Module module) {
    SpringBeansAsJsfVariableUtil.addVariables(resultVars, module);
  }
}
