/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.web;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.javaee.model.xml.ParamValue;
import com.intellij.javaee.model.xml.web.Servlet;
import com.intellij.javaee.model.xml.web.ServletMapping;
import com.intellij.javaee.model.xml.web.WebApp;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.facet.SpringConfigurator;
import com.intellij.spring.facet.SpringFrameworkSupportProvider;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Dmitry Avdeev
 */
public class SpringWebConfigurator implements SpringConfigurator {

  @NonNls private static final String DEFAULT_DISPATCHER_NAME = "dispatcher";
  @NonNls private static final String DEFAULT_DISPATCHER_MAPPING = "*.form";

  private static final Logger LOG = Logger.getInstance("#com.intellij.spring.web.SpringWebConfigurator");

  public boolean configure(@NotNull final Module module) {

    if (JavaPsiFacade.getInstance(module.getProject()).findClass(SpringWebConstants.DISPATCHER_SERVLET_CLASS, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)) == null) {
      return false;
    }

    final Collection<WebFacet> webFacets = WebFacet.getInstances(module);
    if (webFacets.isEmpty()) {
      return false;
    }

    for (final WebFacet webFacet : webFacets) {
      final WebApp webApp = webFacet.getRoot();
      if (webApp != null) {
        new WriteCommandAction.Simple(module.getProject(), webApp.getContainingFile()) {
          protected void run() throws Throwable {
            configure(webApp);
          }
        }.execute();
      }
    }

    return true;
  }

  private static void configure(final WebApp webApp) {

    final PsiFile webXml = webApp.getContainingFile();
    assert webXml != null;
    final PsiDirectory webInfDir = webXml.getParent();
    assert webInfDir != null;
    final Module module = webApp.getModule();
    assert module != null;
    FileTemplate template = SpringFrameworkSupportProvider.chooseTemplate(module);

    final ParamValue paramValue = DomUtil.findByName(webApp.getContextParams(), SpringWebConstants.CONTEXT_CONFIG_LOCATION);
    if (paramValue == null) {
      final ParamValue value = webApp.addContextParam();
      value.getParamName().setValue(SpringWebConstants.CONTEXT_CONFIG_LOCATION);
      value.getParamValue().setValue(SpringWebConstants.WEB_INF + SpringWebConstants.APPLICATION_CONTEXT_XML);

      webApp.addListener().getListenerClass().setStringValue(SpringWebConstants.CONTEXT_LISTENER_CLASS);


      if (webInfDir.findFile(SpringWebConstants.APPLICATION_CONTEXT_XML) == null) {
        try {
          FileTemplateUtil.createFromTemplate(template, SpringWebConstants.APPLICATION_CONTEXT_XML, null, webInfDir);
        }
        catch (Exception e) {
          LOG.error(e);
        }
      }
    }

    Servlet dispatcher = null;
    for (Servlet servlet: webApp.getServlets()) {
      final String stringValue = servlet.getServletClass().getStringValue();
      if (stringValue != null && stringValue.equals(SpringWebConstants.DISPATCHER_SERVLET_CLASS)) {
        dispatcher = servlet;
        break;
      }
    }
    if (dispatcher == null) {
      dispatcher = webApp.addServlet();
      dispatcher.getServletName().setValue(DEFAULT_DISPATCHER_NAME);
      dispatcher.getServletClass().setStringValue(SpringWebConstants.DISPATCHER_SERVLET_CLASS);
      dispatcher.getLoadOnStartup().setValue(1);

      final ServletMapping mapping = webApp.addServletMapping();
      mapping.getServletName().setValue(dispatcher);
      mapping.addUrlPattern().setValue(DEFAULT_DISPATCHER_MAPPING);
    }

    final String dispatcherName = dispatcher.getServletName().getValue();
    final String dispatcherConfig = SpringWebModelProvider.getServletContextFileName(dispatcherName);
    if (webInfDir.findFile(dispatcherConfig) == null) {
      try {
        FileTemplateUtil.createFromTemplate(template, dispatcherConfig, null, webInfDir);
      }
      catch (Exception e) {
        LOG.error(e);
      }
    }          
  }
}
