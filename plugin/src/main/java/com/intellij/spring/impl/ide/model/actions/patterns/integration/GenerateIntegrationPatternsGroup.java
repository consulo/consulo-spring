package com.intellij.spring.impl.ide.model.actions.patterns.integration;

import com.intellij.spring.impl.ide.SpringIcons;
import com.intellij.spring.impl.ide.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.patterns.PatternIcons;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.ex.action.DefaultActionGroup;

public class GenerateIntegrationPatternsGroup extends DefaultActionGroup {

    public GenerateIntegrationPatternsGroup() {
        super(SpringLocalize.springPatternsIntegrationGroupName(), SpringLocalize.springPatternsIntegrationGroupName(), PatternIcons.INTEGRATION_GROUP_ICON);

        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsIntegrationEjbLocalStatelesSessionBean().get(), "ejb-lssb"), PatternIcons.EJB_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsIntegrationEjbRemoteStatelesSessionBean().get(), "ejb-rssb"), PatternIcons.EJB_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsIntegrationFreeMarkerFactoryBean().get(), "free-marker"), SpringIcons.SPRING_BEAN_ICON));

        setPopup(true);
    }
}