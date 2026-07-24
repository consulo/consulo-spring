/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.web;

import com.intellij.javaee.model.xml.ParamValue;
import com.intellij.javaee.model.xml.web.Servlet;
import com.intellij.javaee.model.xml.web.WebApp;
import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.jsp.WebDirectoryElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlElement;
import com.intellij.spring.SpringModelProvider;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.facet.SpringFacetConfiguration;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class SpringWebModelProvider implements SpringModelProvider {

  private final static Icon WEB_FILESET_ICON = IconLoader.getIcon("/resources/icons/webFileSet.png");
  
  @NonNls private static final String WEB = "web: ";
  @NonNls private static final String APPLICATION_CONTEXT_FILESET = WEB + "application context";
  @NonNls private static final String SERVLET_CONTEXT_POSTFIX = " servlet context";

  @NotNull
  public List<SpringFileSet> getFilesets(@NotNull SpringFacet springFacet) {
    Collection<WebFacet> facets = WebFacet.getInstances(springFacet.getModule());
    List<SpringFileSet> result = new ArrayList<SpringFileSet>(facets.size());
    for (WebFacet facet : facets) {
      List<ServletFileSet> servletSets = getServletSets(facet, springFacet.getConfiguration());
      if (servletSets == null) {
        continue;
      }
      WebApp app = facet.getRoot();
      VirtualFile webinfFile = getWebInf(facet);
      assert webinfFile != null;
      assert app != null;
      ParamValue param = DomUtil.findByName(app.getContextParams(), SpringWebConstants.CONTEXT_CONFIG_LOCATION);
      SpringFileSet appContext = null;
      String fsname = SpringWebBundle.message("mvc.application.context.autodetected");
      if (param != null) {
        appContext = createFileSet(webinfFile.getParent(), param, fsname, APPLICATION_CONTEXT_FILESET, springFacet.getConfiguration(), null);
      } else {
        VirtualFile file = webinfFile.findChild(SpringWebConstants.APPLICATION_CONTEXT_XML);
        if (file != null) {
          appContext = createFileSet(APPLICATION_CONTEXT_FILESET, fsname, springFacet.getConfiguration(), null);
          appContext.addFile(file);
        }
      }
      if (appContext != null) {
        result.add(appContext);
        for (SpringFileSet springFileSet : servletSets) {
          springFileSet.addDependency(appContext.getId());
        }
      }
      result.addAll(servletSets);
    }
    return result;
  }

  @Nullable
  private static VirtualFile getWebInf(WebFacet facet) {
    WebDirectoryElement webinf = WebUtil.getWebUtil().findWebDirectoryElement(SpringWebConstants.WEB_INF, facet);
    return webinf == null ? null : webinf.getVirtualFile();
  }

  @Nullable
  public static List<ServletFileSet> getServletSets(WebFacet facet, @NotNull SpringFacetConfiguration configuration) {

    WebApp app = facet.getRoot();
    if (app == null) {
      return null;
    }
    Module module = facet.getModule();
    GlobalSearchScope scope = module.getModuleWithDependenciesAndLibrariesScope(false);
    PsiClass dispatchClass = JavaPsiFacade.getInstance(module.getProject()).findClass(SpringWebConstants.DISPATCHER_SERVLET_CLASS, scope);
    if (dispatchClass == null) {
      return null;
    }
    List<ServletFileSet> result = new ArrayList<ServletFileSet>();
    VirtualFile webInf = getWebInf(facet);
    if (webInf == null) {
      return null;
    }
    VirtualFile root = webInf.getParent();
    for (Servlet servlet: app.getServlets()) {
      PsiClass servletClass = servlet.getServletClass().getValue();
      String servletName = servlet.getServletName().getValue();
      if (servletClass != null && servletName != null && dispatchClass.equals(servletClass)) {

        ParamValue servlParam = DomUtil.findByName(servlet.getInitParams(), SpringWebConstants.CONTEXT_CONFIG_LOCATION);
        ServletFileSet springFileSet;
        String id = WEB + servletName + SERVLET_CONTEXT_POSTFIX;
        String name = SpringWebBundle.message("mvc.servlet.context.autodetected", servletName);
        if (servlParam != null) {
          springFileSet = createFileSet(root, servlParam, id, name, configuration, servlet);
        } else {
          springFileSet = createFileSet(id, name, configuration, servlet);
          springFileSet.addFile(webInf.getUrl() + "/" + getServletContextFileName(servletName));
        }

        result.add(springFileSet);
      }
    }
    return result;
  }

  @NotNull
  private static ServletFileSet createFileSet(VirtualFile root,
                                              ParamValue param,
                                              String name, String id,
                                              @NotNull SpringFacetConfiguration configuration,
                                              @Nullable Servlet servlet) {
    ServletFileSet fileSet = createFileSet(id, name, configuration, servlet);
    XmlElement tag = param.getParamValue().getXmlElement();
    if (tag != null) {
      PsiReference[] references = tag.getReferences();
      for (PsiReference reference : references) {
        if (reference instanceof FileReference) {
          if (((FileReference)reference).isLast()) {
            ResolveResult[] results = ((FileReference)reference).multiResolve(false);
            boolean resolved = false;
            for (ResolveResult resolveResult : results) {
              PsiElement element = resolveResult.getElement();
              if (element instanceof PsiFileSystemItem && !((PsiFileSystemItem)element).isDirectory()) {
                VirtualFile virtualFile = ((PsiFileSystemItem)element).getVirtualFile();
                if (virtualFile != null) {
                  fileSet.addFile(virtualFile);
                  resolved = true;
                }
              }
            }
            if (!resolved) {
              fileSet.addFile(root.getUrl() + "/" + ((FileReference)reference).getText());
            }
          }
        }
      }
    }
    return fileSet;
  }

  @NonNls
  static String getServletContextFileName(String servletName) {
    return servletName + "-servlet.xml";
  }

  private static ServletFileSet createFileSet(@NonNls String id, String name, @NotNull final SpringFacetConfiguration configuration,
                                             @Nullable final Servlet servlet) {
    return new ServletFileSet(id, name, configuration) {
      public Icon getIcon() {
        return WEB_FILESET_ICON;
      }

      public boolean isAutodetected() {
        return true;
      }

      public Servlet getServlet() {
        return servlet;
      }
    };
  }
}
