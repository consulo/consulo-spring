package com.intellij.spring.impl.ide.model.actions;

import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.ex.action.DefaultActionGroup;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.generate.SpringConstructorDependenciesGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.generate.SpringPropertiesGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.generate.SpringSetterDependenciesGenerateProvider;
import consulo.ui.ex.action.AnSeparator;

public class GenerateSpringDomElementActionGroup extends DefaultActionGroup {
    public GenerateSpringDomElementActionGroup() {
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springBean(), "spring-bean"),
            SpringImplIconGroup.springbean()
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider((SpringLocalize.springBeanInstantiationByFactory()), "spring-bean-with-factory-method"),
            SpringImplIconGroup.springbean()
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springBeanInstantiationUsingFactoryMethod(), "spring-bean-with-factory-bean"),
            SpringImplIconGroup.springbean()
        ));

        //add(new GenerateSpringDomElementAction(new SpringAliasGenerateProvider(), SpringIcons.SPRING_ALIAS_ICON));
        //add(new GenerateSpringDomElementAction(new SpringImportGenerateProvider(), SpringIcons.CONFIG_FILE));

        add(AnSeparator.getInstance());

        add(new GenerateSpringBeanBodyAction(new SpringPropertiesGenerateProvider()));
        add(new GenerateSpringBeanBodyAction(new SpringSetterDependenciesGenerateProvider(), SpringImplIconGroup.springbean()));
        add(new GenerateSpringBeanBodyAction(new SpringConstructorDependenciesGenerateProvider(), SpringImplIconGroup.springbean()));
    }
}
