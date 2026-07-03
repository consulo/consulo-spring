package com.intellij.spring.impl.ide.model.actions.patterns.factoryBeans;

import com.intellij.spring.impl.ide.SpringIcons;
import com.intellij.spring.impl.ide.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.patterns.PatternIcons;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DefaultActionGroup;

public class GenerateCommonBeansPatternsGroup extends DefaultActionGroup {

    public GenerateCommonBeansPatternsGroup() {
        super(SpringLocalize.springPatternsCommonBeansGroupName(), SpringLocalize.springPatternsCommonBeansGroupName(), PatternIcons.FACTORY_BEAN_ICON);
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsCommonBeansPlaceholder().get(), "placeholder-configurer"), SpringIcons.SPRING_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsCommonBeansPropertyOverrideConfigurer().get(), "property-override-configurer"), SpringIcons.SPRING_BEAN_ICON));
        addSeparator();
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsFactoryBeansResource().get(), "resource-factory"), PatternIcons.FACTORY_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsFactoryBeansCommonsLog().get(), "commons-log-factory"), PatternIcons.FACTORY_BEAN_ICON));
        addSeparator();
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsFactoryBeansSet().get(), "set-factory"), PatternIcons.FACTORY_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsFactoryBeansList().get(), "list-factory"), PatternIcons.FACTORY_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsFactoryBeansMap().get(), "map-factory"), PatternIcons.FACTORY_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsFactoryBeansProperties().get(), "properties-factory"), PatternIcons.FACTORY_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsFactoryBeansFieldRetrieving().get(), "field-factory"), PatternIcons.FACTORY_BEAN_ICON));

        setPopup(true);
    }

}