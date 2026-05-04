package com.intellij.spring.security.util;

import com.intellij.javaee.model.xml.web.Filter;
import com.intellij.javaee.model.xml.web.WebApp;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.spring.security.constants.SpringSecurityConstants;
import com.intellij.util.xml.DomFileElement;
import com.intellij.xml.util.XmlUtil;

import java.util.Collection;

/**
 * @author Serega.Vasiliev
 */
public class SpringSecurityUtil {

  public static boolean isSpringSecurityUsed(DomFileElement<Beans> domFileElement) {
    XmlFile xmlFile = domFileElement.getFile();

    return XmlUtil.findNamespace(xmlFile, SpringSecurityConstants.SECURITY_NAMESPACE) != null || isFilterChainProxyDefined(domFileElement);
  }

  private static boolean isFilterChainProxyDefined(DomFileElement<Beans> domFileElement) {
    Module module = domFileElement.getModule();
    if (module != null) {
      SpringModel springModel = SpringManager.getInstance(module.getProject()).getSpringModelByFile(domFileElement.getFile());
      if (springModel != null) {
        PsiClass psiClass = JavaPsiFacade.getInstance(module.getProject())
          .findClass(SpringSecurityClassesConstants.FILTER_CHAIN_PROXY, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
        if (psiClass != null) return springModel.findBeansByPsiClassWithInheritance(psiClass).size() > 0;
      }
    }

    return false;
  }

  public static boolean isFilterConfigured(Collection<WebFacet> webFacets) {
    for (WebFacet webFacet : webFacets) {
      WebApp webApp = webFacet.getRoot();
      if (webApp != null) {
        for (Filter filter : webApp.getFilters()) {
          PsiClass psiClass = filter.getFilterClass().getValue();
          if (InheritanceUtil.isInheritor(psiClass, SpringSecurityClassesConstants.DELEGATING_FILTER_PROXY)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
