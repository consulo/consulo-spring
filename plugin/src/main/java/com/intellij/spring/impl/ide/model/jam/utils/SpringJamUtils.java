package com.intellij.spring.impl.ide.model.jam.utils;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiJavaPackage;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.jam.SpringJamModel;
import com.intellij.spring.impl.ide.model.jam.javaConfig.JavaConfigConfiguration;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJavaConfiguration;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJavaExternalBean;
import com.intellij.spring.impl.ide.model.jam.stereotype.SpringStereotypeElement;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.impl.ide.model.xml.context.ComponentScan;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import consulo.xml.psi.xml.XmlTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class SpringJamUtils {
  private SpringJamUtils() {
  }

  @Nonnull
  public static List<SpringStereotypeElement> getAllStereotypeJavaBeans(final SpringModel domModel) {
    List<SpringStereotypeElement> allStereotypes = new ArrayList<SpringStereotypeElement>();
    final consulo.module.Module module = domModel.getModule();
    if (module != null) {
      List<ComponentScan> scanBeans = getComponentScans(domModel.getAllDomBeans());
      if (scanBeans.size() > 0) {
        List<PsiJavaPackage> psiPackages = getScannedPackages(scanBeans);
        if (psiPackages.isEmpty()) {
          return Collections.emptyList();
        }
        SpringJamModel javaModel = SpringJamModel.getModel(module);

        List<SpringStereotypeElement> components = getAllStereotypeComponents(javaModel);
        return filterStereotypeComponents(components, psiPackages);
      }
    }

    return allStereotypes;
  }

  @Nonnull
  public static List<SpringJavaConfiguration> getJavaConfigurations(final SpringModel domModel) {
    List<SpringJavaConfiguration> javaConfigurations = new ArrayList<SpringJavaConfiguration>();
    final consulo.module.Module module = domModel.getModule();
    if (module != null) {
      List<ComponentScan> scanBeans = getComponentScans(domModel.getAllDomBeans());
      if (scanBeans.size() > 0) {
        List<PsiJavaPackage> psiPackages = getScannedPackages(scanBeans);
        if (psiPackages.isEmpty()) {
          return Collections.emptyList();
        }

        List<SpringJavaConfiguration> components = SpringJamModel.getModel(module).getConfigurations();

        return filterJavaConfigurations(components, psiPackages);
      }
    }

    return javaConfigurations;
  }

  private static List<ComponentScan> getComponentScans(final Collection<SpringBaseBeanPointer> allDomBeans) {
    return ContainerUtil.mapNotNull(allDomBeans, new Function<SpringBaseBeanPointer, ComponentScan>() {
      public ComponentScan apply(final SpringBaseBeanPointer domSpringBeanPointer) {
        final CommonSpringBean domSpringBean = domSpringBeanPointer.getSpringBean();
        if (domSpringBean instanceof ComponentScan) {
          return (ComponentScan)domSpringBean;
        }
        return null;
      }
    });
  }

  private static List<SpringStereotypeElement> filterStereotypeComponents(final List<SpringStereotypeElement> components,
                                                                final List<PsiJavaPackage> psiPackages) {
    List<SpringStereotypeElement> filtered = new ArrayList<SpringStereotypeElement>();
    for (SpringStereotypeElement component : components) {
      final PsiClass psiClass = component.getBeanClass();
      if (isInPackage(psiPackages, psiClass)) {
        filtered.add(component);
      }
    }

    return filtered;
  }

  private static List<SpringJavaConfiguration> filterJavaConfigurations(final List<SpringJavaConfiguration> javaConfigurations,
                                                                final List<PsiJavaPackage> psiPackages) {
    List<SpringJavaConfiguration> filtered = new ArrayList<SpringJavaConfiguration>();
    for (SpringJavaConfiguration component : javaConfigurations) {
      final PsiClass psiClass = component.getPsiClass();
      if (isInPackage(psiPackages, psiClass)) {
        filtered.add(component);
      }
    }

    return filtered;
  }

  private static boolean isInPackage(List<PsiJavaPackage> psiPackages, @Nullable PsiClass psiClass) {
    if (psiClass != null) {
      final String qualifiedName = psiClass.getQualifiedName();
      if (qualifiedName != null) {
        for (PsiJavaPackage psiPackage : psiPackages) {
          if (StringUtil.startsWithConcatenation(qualifiedName, psiPackage.getQualifiedName(), ".")) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static List<SpringStereotypeElement> getAllStereotypeComponents(final SpringJamModel javaModel) {
    List<SpringStereotypeElement> elements = new ArrayList<SpringStereotypeElement>();

    elements.addAll(javaModel.getAllStereotypeComponents());

    return elements;
  }

  private static List<PsiJavaPackage> getScannedPackages(final List<ComponentScan> scanBeans) {
    final ArrayList<PsiJavaPackage> list = new ArrayList<PsiJavaPackage>(scanBeans.size());
    for (ComponentScan scanBean : scanBeans) {
      list.addAll(scanBean.getBasePackage().getValue());
    }
    return list;
  }

  @Nonnull
  public static List<SpringBaseBeanPointer> findExternalBeans(final PsiMethod psiMethod) {
    if (!isExternalBean(psiMethod)) {
      return Collections.emptyList();
    }
    final consulo.module.Module module = ModuleUtilCore.findModuleForPsiElement(psiMethod);
    PsiClass psiClass = psiMethod.getContainingClass();
    if (module == null || psiClass == null) {
      return Collections.emptyList();
    }
    List<SpringBaseBeanPointer> extBeans = new ArrayList<SpringBaseBeanPointer>();
    final SpringModel springModel = SpringManager.getInstance(psiMethod.getProject()).getCombinedModel(module);
    if(springModel != null) {
      List<SpringBaseBeanPointer> javaConfigBeans = springModel.findBeansByPsiClass(psiClass);
      if (javaConfigBeans.size() > 0) {
        for (SpringBaseBeanPointer springBean : springModel.getAllDomBeans()) {

          final String externalBeanName = psiMethod.getName();
          final String beanName = springBean.getName();
          if (externalBeanName.equals(beanName) || Arrays.asList(springBean.getAliases()).contains(externalBeanName)) {
            extBeans.add(springBean);
          }
        }
      }
    }
    return extBeans;
  }

  @Nonnull
  public static List<SpringJavaExternalBean> findExternalBeanReferences(final CommonSpringBean springBean) {
    List<SpringJavaExternalBean> extBeans = new ArrayList<SpringJavaExternalBean>();
    final Set<String> strings = SpringUtils.getAllBeanNames(springBean);

    if (strings.size() > 0) {
      final XmlTag element = springBean.getXmlTag();
      if (element != null) {
        final Module module = ModuleUtilCore.findModuleForPsiElement(element);

        if (module != null) {
          for (SpringJavaConfiguration javaConfiguration : SpringJamModel.getModel(module).getConfigurations()) {
            if (javaConfiguration instanceof JavaConfigConfiguration) {
              for (SpringJavaExternalBean externalBean : ((JavaConfigConfiguration)javaConfiguration).getExternalBeans()) {
                final PsiMethod psiMethod = externalBean.getPsiElement();
                if (psiMethod != null && strings.contains(psiMethod.getName())) {
                  extBeans.add(externalBean);
                }
              }
            }
          }
        }
      }
    }
    return extBeans;
  }

  public static boolean isExternalBean(final PsiMethod psiMethod) {
    return getExternalBean(psiMethod) != null;
  }

  @Nullable
  public static SpringJavaExternalBean getExternalBean(final PsiMethod psiMethod) {
    final consulo.module.Module module = ModuleUtilCore.findModuleForPsiElement(psiMethod);
    if (module != null) {
      for (SpringJavaConfiguration javaConfiguration : SpringJamModel.getModel(module).getConfigurations()) {
        if (javaConfiguration instanceof JavaConfigConfiguration) {
          if (psiMethod.getContainingFile().equals(javaConfiguration.getPsiClass().getContainingFile())) {
            for (SpringJavaExternalBean externalBean : ((JavaConfigConfiguration)javaConfiguration).getExternalBeans()) {
              if (psiMethod.equals(externalBean.getPsiElement())) {
                return externalBean;
              }
            }
          }
        }
      }
    }
    return null;
  }
}
