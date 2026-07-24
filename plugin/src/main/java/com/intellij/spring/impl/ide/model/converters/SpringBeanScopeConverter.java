/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.*;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.xml.dom.ConvertContext;
import consulo.xml.dom.ResolvingConverter;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SpringBeanScopeConverter extends ResolvingConverter<SpringBeanScope> {
  @NonNls private static final String CUSTOM_SCOPE_CONFIGURER_CLASSNAME = "org.springframework.beans.factory.config.CustomScopeConfigurer";
  @NonNls private static final String CUSTOM_SCOPES_PROPERTY_NAME = "scopes";

  @Nonnull
  public Collection<SpringBeanScope> getVariants(ConvertContext context) {
    return getAllBeanScopes(context);
  }

  private static List<SpringBeanScope> getAllBeanScopes(ConvertContext context) {
    List<SpringBeanScope> scopes = new ArrayList<SpringBeanScope>(Arrays.asList(SpringBeanScope.getDefaultScopes()));

    scopes.addAll(getCustomBeanScopes(context));

    return scopes;
  }

  public SpringBeanScope fromString(String s, ConvertContext context) {
    if(s == null) return null;
    for (SpringBeanScope beanScope : getAllBeanScopes(context)) {
      if (s.equals(beanScope.getValue())) return beanScope;
    }

    return null;
  }

  public String toString(SpringBeanScope springBeanScope, ConvertContext context) {
    return springBeanScope.getValue();
  }

  public static List<SpringBeanScope> getCustomBeanScopes(ConvertContext context) {
    List<SpringBeanScope> customScopes = new ArrayList<SpringBeanScope>();

    SpringModel model = SpringConverterUtil.getSpringModel(context);

    if (model == null) return customScopes;

    Project project = context.getPsiManager().getProject();
    PsiClass psiClass =
      JavaPsiFacade.getInstance(project).findClass(CUSTOM_SCOPE_CONFIGURER_CLASSNAME, GlobalSearchScope.allScope(project));
    if (psiClass == null) {
      return customScopes;
    }
    List<SpringBaseBeanPointer> springBeans = model.findBeansByPsiClass(psiClass);
    for (DomSpringBeanPointer springBean : ContainerUtil.findAll(springBeans, DomSpringBeanPointer.class)) {
      SpringPropertyDefinition property = SpringUtils.findPropertyByName(springBean.getSpringBean(), CUSTOM_SCOPES_PROPERTY_NAME);
      if (property instanceof SpringProperty) {
        for (SpringEntry springEntry : ((SpringProperty)property).getMap().getEntries()) {
          String keyValue = springEntry.getKeyAttr().getStringValue();
          if (keyValue != null && keyValue.length() > 0) {
            customScopes.add(new SpringBeanScope(keyValue));
          }  else {
            String keyValue2 = springEntry.getKey().getValue().getStringValue();
            if (keyValue2!= null && keyValue2.length() > 0) {
              customScopes.add(new SpringBeanScope(keyValue2));
            }
          }
        }
      }
    }


    return customScopes;
  }
}
