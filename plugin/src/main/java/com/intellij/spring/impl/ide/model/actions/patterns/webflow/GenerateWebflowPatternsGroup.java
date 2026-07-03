package com.intellij.spring.impl.ide.model.actions.patterns.webflow;

import com.intellij.spring.impl.ide.SpringIcons;
import com.intellij.spring.impl.ide.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.patterns.PatternIcons;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.ex.action.DefaultActionGroup;

public class GenerateWebflowPatternsGroup extends DefaultActionGroup {

    public GenerateWebflowPatternsGroup() {
        super(SpringLocalize.springPatternsWebflowGroupName(), SpringLocalize.springPatternsWebflowGroupName(), PatternIcons.FACTORY_BEAN_ICON);

        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowRegistry().get(), "flow-registry"), SpringIcons.SPRING_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowExecutor().get(), "flow-executor"), SpringIcons.SPRING_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowBuilderServices().get(), "flow-builder-serices"), SpringIcons.SPRING_BEAN_ICON));
        addSeparator();
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowExecutionListener().get(), "flow-execution-listener"), PatternIcons.FACTORY_BEAN_ICON));
        addSeparator();
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowServicesConversionService().get(), "conversation-service"), PatternIcons.FACTORY_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowServicesExpressionParser().get(), "expression-parser"), PatternIcons.FACTORY_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowServicesViewFactoryCreator().get(), "factory-creator"), PatternIcons.FACTORY_BEAN_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsWebflowServicesViewFormatterRegistry().get(), "formatter-registry"), PatternIcons.FACTORY_BEAN_ICON));

        setPopup(true);
    }
}
