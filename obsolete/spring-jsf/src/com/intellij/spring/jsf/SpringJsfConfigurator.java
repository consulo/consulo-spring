/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package com.intellij.spring.jsf;

import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.jsf.model.FacesConfig;
import com.intellij.jsf.model.FacesDomModel;
import com.intellij.jsf.model.FacesDomModelManager;
import com.intellij.jsf.utils.JsfCommonUtils;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.spring.facet.SpringConfigurator;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Dmitry Avdeev
 */
public class SpringJsfConfigurator implements SpringConfigurator {

  private static void configure(final FacesConfig config) {
    final GenericDomValue<PsiClass> resolver = config.getApplication().getVariableResolver();
    if (resolver.getStringValue() == null) {
      resolver.setStringValue("org.springframework.web.jsf.DelegatingVariableResolver");
    }
  }

  public boolean configure(@NotNull final Module module) {
    if (!JsfCommonUtils.isJsfFacetDefined(module)) {
      return false;
    }
    final Collection<WebFacet> webFacets = WebFacet.getInstances(module);
    if (webFacets.isEmpty()) {
      return false;
    }

    for (final WebFacet webFacet : webFacets) {
      for (FacesDomModel facesDomModel : FacesDomModelManager.getInstance(webFacet.getModule().getProject()).getAllModels(webFacet)) {
        final FacesConfig facesConfig = facesDomModel.getFacesConfig();
        new WriteCommandAction.Simple(module.getProject()) {
          protected void run() throws Throwable {
            configure(facesConfig);
          }
        }.execute();
      }
    }

    return false;
  }
}
