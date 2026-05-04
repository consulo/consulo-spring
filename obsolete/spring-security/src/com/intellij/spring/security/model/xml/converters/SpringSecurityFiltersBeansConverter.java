package com.intellij.spring.security.model.xml.converters;

import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.SpringModel;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.spring.model.converters.SpringBeanListConverter;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class SpringSecurityFiltersBeansConverter extends SpringBeanListConverter {

   @Override
  protected Collection<? extends SpringBaseBeanPointer> getVariantBeans(@NotNull SpringModel model) {
    Module module = model.getModule();
    if (module != null) {
      PsiClass filterClass = JavaPsiFacade.getInstance(module.getProject())
        .findClass(SpringSecurityClassesConstants.JAVAX_SEVLET_FILTER, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
      if (filterClass != null) {
        return model.findBeansByEffectivePsiClassWithInheritance(filterClass);
      }
    }
    return Collections.emptyList();
  }
}