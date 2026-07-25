package com.intellij.spring.impl.ide.model.actions.patterns.webflow;

import com.intellij.spring.impl.ide.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.patterns.PatternIcons;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.ex.action.DefaultActionGroup;

public class GenerateWebflowPatternsGroup extends DefaultActionGroup {
    public GenerateWebflowPatternsGroup() {
        super(SpringLocalize.springPatternsWebflowGroupName(), SpringLocalize.springPatternsWebflowGroupName(), PatternIcons.FACTORY_BEAN_ICON);

        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowRegistry(), "flow-registry"),
            SpringImplIconGroup.springbean()
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowExecutor(), "flow-executor"),
            SpringImplIconGroup.springbean()
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowBuilderServices(), "flow-builder-serices"),
            SpringImplIconGroup.springbean()
        ));
        addSeparator();
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowExecutionListener(), "flow-execution-listener"),
            PatternIcons.FACTORY_BEAN_ICON
        ));
        addSeparator();
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowServicesConversionService(), "conversation-service"),
            PatternIcons.FACTORY_BEAN_ICON
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowServicesExpressionParser(), "expression-parser"),
            PatternIcons.FACTORY_BEAN_ICON
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowServicesViewFactoryCreator(), "factory-creator"),
            PatternIcons.FACTORY_BEAN_ICON
        ));
        add(new GenerateSpringDomElementAction(
            new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowServicesViewFormatterRegistry(), "formatter-registry"),
            PatternIcons.FACTORY_BEAN_ICON
        ));

        setPopup(true);
    }
}
