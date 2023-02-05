package com.intellij.spring.impl.ide.model.actions;

import consulo.ui.ex.action.DefaultActionGroup;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringIcons;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.generate.SpringConstructorDependenciesGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.generate.SpringPropertiesGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.generate.SpringSetterDependenciesGenerateProvider;
import consulo.ui.ex.action.AnSeparator;

public class GenerateSpringDomElementActionGroup extends DefaultActionGroup
{

  public GenerateSpringDomElementActionGroup() {
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.bean"), "spring-bean"), SpringIcons.SPRING_BEAN_ICON)) ;
    add(new GenerateSpringDomElementAction(
      new SpringBeanGenerateProvider((SpringBundle.message("spring.bean.instantiation.by.factory")), "spring-bean-with-factory-method"), SpringIcons.SPRING_BEAN_ICON));
    add(new GenerateSpringDomElementAction(
      new SpringBeanGenerateProvider((SpringBundle.message("spring.bean.instantiation.using.factory.method")), "spring-bean-with-factory-bean"), SpringIcons.SPRING_BEAN_ICON));

    //add(new GenerateSpringDomElementAction(new SpringAliasGenerateProvider(), SpringIcons.SPRING_ALIAS_ICON ));
    //add(new GenerateSpringDomElementAction(new SpringImportGenerateProvider(), SpringIcons.CONFIG_FILE));

    add(AnSeparator.getInstance());

    add(new GenerateSpringBeanBodyAction(new SpringPropertiesGenerateProvider()));
    add(new GenerateSpringBeanBodyAction(new SpringSetterDependenciesGenerateProvider(), SpringIcons.SPRING_BEAN_ICON));
    add(new GenerateSpringBeanBodyAction(new SpringConstructorDependenciesGenerateProvider(), SpringIcons.SPRING_BEAN_ICON));
  }

}

