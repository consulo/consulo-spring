package com.intellij.spring.impl.ide.model.actions.patterns.factoryBeans;

import com.intellij.spring.impl.ide.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.patterns.PatternIcons;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.ex.action.DefaultActionGroup;

public class GenerateCommonBeansPatternsGroup extends DefaultActionGroup {

    public GenerateCommonBeansPatternsGroup() {
        super(SpringLocalize.springPatternsCommonBeansGroupName(), SpringLocalize.springPatternsCommonBeansGroupName(), PatternIcons.FACTORY_BEAN_ICON);
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(
            SpringLocalize.springPatternsCommonBeansPlaceholder(), "placeholder-configurer"),
            SpringImplIconGroup.springbean()
        ));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(
            SpringLocalize.springPatternsCommonBeansPropertyOverrideConfigurer(), "property-override-configurer"),
            SpringImplIconGroup.springbean()
        ));
        addSeparator();
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsFactoryBeansResource(), "resource-factory"),
            PatternIcons.FACTORY_BEAN_ICON
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsFactoryBeansCommonsLog(), "commons-log-factory"),
            PatternIcons.FACTORY_BEAN_ICON
        ));
        addSeparator();
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsFactoryBeansSet(), "set-factory"),
            PatternIcons.FACTORY_BEAN_ICON
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsFactoryBeansList(), "list-factory"),
            PatternIcons.FACTORY_BEAN_ICON
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsFactoryBeansMap(), "map-factory"),
            PatternIcons.FACTORY_BEAN_ICON
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsFactoryBeansProperties(), "properties-factory"),
            PatternIcons.FACTORY_BEAN_ICON
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsFactoryBeansFieldRetrieving(), "field-factory"),
            PatternIcons.FACTORY_BEAN_ICON
        ));

        setPopup(true);
    }
}