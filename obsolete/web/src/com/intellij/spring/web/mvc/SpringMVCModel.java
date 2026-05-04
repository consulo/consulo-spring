package com.intellij.spring.web.mvc;

import com.intellij.javaee.model.xml.web.Servlet;
import com.intellij.javaee.model.xml.web.WebApp;
import com.intellij.javaee.model.xml.web.ServletMapping;
import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.ServletMappingInfo;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import consulo.ide.impl.idea.openapi.module.ModuleUtil;
import consulo.util.dataholder.Key;
import consulo.ide.impl.idea.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.PsiElementPointer;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.spring.web.ServletFileSet;
import com.intellij.spring.web.SpringWebModelProvider;
import com.intellij.spring.web.mvc.jam.SpringMVCJamModel;
import com.intellij.spring.web.mvc.jam.SpringMVCRequestMapping;
import consulo.ide.impl.idea.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Dmitry Avdeev
 */
public class SpringMVCModel {
  private static final Key<CachedValue<SpringMVCModel>> KEY = new Key<CachedValue<SpringMVCModel>>("spring mvc model");

  @Nullable
  public static SpringMVCModel getModel(final WebFacet facet, final SpringFacet springFacet) {

    return PsiManager.getInstance(facet.getModule().getProject()).getCachedValuesManager().getCachedValue(facet, KEY, new CachedValueProvider<SpringMVCModel>() {
      public Result<SpringMVCModel> compute() {
        return Result.createSingleDependency(new SpringMVCModel(facet, springFacet), PsiModificationTracker.MODIFICATION_COUNT);
      }
    }, false);
  }

  @Nullable
  public static SpringMVCModel getModel(final PsiElement element) {
    final Module module = ModuleUtil.findModuleForPsiElement(element);
    if (module == null) {
      return null;
    }
    final SpringFacet springFacet = SpringFacet.getInstance(module);
    if (springFacet == null) {
      return null;
    }
    final WebFacet webFacet = WebUtil.getWebFacet(element);
    return webFacet == null ? null : getModel(webFacet, springFacet);
  }

  private static class Info {
    final Map<String, PsiElementPointer> urls = new HashMap<String, PsiElementPointer>();
    final List<Pair<String, PsiElementPointer>> patterns = new ArrayList<Pair<String, PsiElementPointer>>();
    final static AntPathMatcher matcher = new AntPathMatcher();

    void addUrl(@NotNull String url, @NotNull PsiElementPointer pointer) {
      if (matcher.isPattern(url)) {
        patterns.add(new Pair<String, PsiElementPointer>(url, pointer));
      } else {
        urls.put(url, pointer);
      }
    }

    @Nullable
    PsiElement resolve(String url) {
      final PsiElementPointer pointer = urls.get(url);
      if (pointer != null) {
        return pointer.getPsiElement();
      }
      for (Pair<String, PsiElementPointer> pattern : patterns) {
        if (matcher.match(pattern.first, url)) {
          return pattern.second.getPsiElement();
        }
      }
      return null;
    }
  }

  private final WebFacet myWebFacet;
  private final SpringFacet mySpringFacet;

  private final NotNullLazyValue<Collection<SpringModel>> myModels = new NotNullLazyValue<Collection<SpringModel>>() {
    @NotNull
    protected Collection<SpringModel> compute() {
      final List<ServletFileSet> fileSets = SpringWebModelProvider.getServletSets(myWebFacet, mySpringFacet.getConfiguration());
      return fileSets == null ? Collections.<SpringModel>emptyList() : ContainerUtil.mapNotNull(fileSets, new NullableFunction<SpringFileSet, SpringModel>() {
        public SpringModel fun(final SpringFileSet springFileSet) {
          return SpringManager.getInstance(myWebFacet.getModule().getProject()).createModel(springFileSet, myWebFacet.getModule());
        }
      });
    }
  };

  private final NotNullLazyValue<Info> myMap = new NotNullLazyValue<Info>() {
    @NotNull
    protected Info compute() {
      final Info info = new Info();
      final JavaPsiFacade facade = JavaPsiFacade.getInstance(myWebFacet.getModule().getProject());
      final GlobalSearchScope scope = GlobalSearchScope.allScope(myWebFacet.getModule().getProject());
      final PsiClass controllerClass = facade.findClass("org.springframework.web.servlet.mvc.Controller", scope);
      if (controllerClass == null) {
        return info;
      }
      final PsiClass simpleMappingClass = facade.findClass("org.springframework.web.servlet.handler.SimpleUrlHandlerMapping", scope);
      final Collection<SpringModel> models = getServletModels();
      for (SpringModel model : models) {
        final List<SpringBaseBeanPointer> list = model.findBeansByPsiClassWithInheritance(controllerClass);
        for (SpringBaseBeanPointer beanPointer : list) {
          final String name = beanPointer.getName();
          if (name != null) {
            info.addUrl(name, beanPointer);
          }
        }
        if (simpleMappingClass != null) {
          final List<SpringBaseBeanPointer> simpleMappings = model.findBeansByPsiClassWithInheritance(simpleMappingClass);
          for (SpringBaseBeanPointer mapping : simpleMappings) {
            final CommonSpringBean springBean = mapping.getSpringBean();
            if (springBean instanceof SpringBean) {
              final SpringProperty urlMap = DomUtil.findByName(((SpringBean)springBean).getProperties(), "urlMap");
              if (urlMap != null) {
                for (SpringEntry entry: urlMap.getMap().getEntries()) {
                  final String key = entry.getKeyAttr().getValue();
                  if (key != null) {
                    final SpringBeanPointer springBeanPointer = entry.getRefElement().getValue();
                    if (springBeanPointer != null) {
                      info.addUrl(key, springBeanPointer.getBasePointer());
                    }
                  }
                }
              }
              final SpringProperty mappings = DomUtil.findByName(((SpringBean)springBean).getProperties(), "mappings");
              if (mappings != null) {
                final String s = mappings.getValue().getStringValue();
                if (s != null) {
                  try {
                    final Properties properties = new Properties();
                    properties.load(new ByteArrayInputStream(s.getBytes()));
                    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                      final Object beanName = entry.getValue();
                      final SpringBeanPointer bean = model.findBean((String)beanName);
                      if (bean != null) {
                        info.addUrl((String)entry.getKey(), bean);
                      }
                    }
                  }
                  catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                }
              }
            }
          }
        }
      }

      final List<SpringMVCRequestMapping> mappings = SpringMVCJamModel.getModel(myWebFacet.getModule()).getRequestMappings();
      for (final SpringMVCRequestMapping<?> mapping : mappings) {
        for (String url: mapping.getUrls()) {
          info.addUrl(url, new PsiElementPointer() {
            public PsiElement getPsiElement() {
              return mapping.getAnnotation();
            }
          });
        }
      }

      MVCViewProvider[] extensions = Extensions.getExtensions(MVCViewProvider.EP_NAME);
      for (MVCViewProvider extension : extensions) {
        for (SpringModel model : models) {
          Servlet servlet = ((ServletFileSet)model.getFileSet()).getServlet();
          if (servlet != null) {
            List<Pair<String, PsiElementPointer>> views = extension.getViews(model, myWebFacet);
            WebApp root = (WebApp)servlet.getParent();
            assert root != null;
            for (ServletMapping mapping : root.getServletMappings()) {
              if (servlet.equals(mapping.getServletName().getValue())) {
                List<ServletMappingInfo> infos = ServletMappingInfo.createMappingInfos(mapping);
                for (ServletMappingInfo mappingInfo : infos) {
                  for (Pair<String, PsiElementPointer> view : views) {
                    String url = mappingInfo.addMapping(view.getFirst());
                    info.addUrl(url, view.getSecond());
                  }
                }
              }
            }
          }
        }
      }

      return info;
    }
  };

  private SpringMVCModel(final WebFacet facet, final SpringFacet springFacet) {
    myWebFacet = facet;
    mySpringFacet = springFacet;
  }

  public WebFacet getWebFacet() {
    return myWebFacet;
  }

  public SpringFacet getSpringFacet() {
    return mySpringFacet;
  }

  public Set<String> getAllUrls() {
    return myMap.getValue().urls.keySet();
  }

  public Collection<SpringModel> getAllModels() {
    return myModels.getValue();
  }

  public Collection<SpringModel> getServletModels() {
    Collection<SpringModel> models = myModels.getValue();
    return ContainerUtil.mapNotNull(models, new NullableFunction<SpringModel, SpringModel>() {
      public SpringModel fun(SpringModel springModel) {
        return ((ServletFileSet)springModel.getFileSet()).getServlet() == null ? null : springModel;
      }
    });
  }

  @Nullable
  public PsiElement resolveUrl(final String url) {
    return myMap.getValue().resolve(url);
  }
}
