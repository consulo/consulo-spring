package com.intellij.spring.impl.ide.model.actions.patterns.integration;

import com.intellij.spring.impl.ide.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.patterns.PatternIcons;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.ex.action.DefaultActionGroup;

public class GenerateSchedulersPatternsGroup extends DefaultActionGroup {

    public GenerateSchedulersPatternsGroup() {
        super(SpringLocalize.springPatternsSchedulingGroupName(), SpringLocalize.springPatternsSchedulingGroupName(), PatternIcons.SCHEDULER_ICON);

        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsIntegrationOpensymphonyJobDetailBean().get(), "quartz-job-detail"), PatternIcons.SCHEDULER_ICON));
        add(new GenerateSpringDomElementAction(new MethodInvokingFactoryBean(SpringLocalize.springPatternsIntegrationOpensymphonyMethodInvokingFactoryBean().get()) {
            protected String getClassName() {
                return "org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean";
            }
        }, PatternIcons.SCHEDULER_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsIntegrationOpensymphonySimpleTrigger().get(), "quartz-simple-trigger"), PatternIcons.SCHEDULER_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsIntegrationOpensymphonyCronTrigger().get(), "quartz-cron-trigger"), PatternIcons.SCHEDULER_ICON));

        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsIntegrationOpensymphonyScheduler().get(), "quartz-scheduler-factory"), PatternIcons.SCHEDULER_ICON));
        addSeparator();
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsIntegrationJdkScheduledTimerTask().get(), "jdk-scheduled-timer-task"), PatternIcons.JDK_ICON));
        add(new GenerateSpringDomElementAction(new MethodInvokingFactoryBean(SpringLocalize.springPatternsIntegrationJdkMethodInvokingFactoryBean().get()) {
            protected String getClassName() {
                return "org.springframework.scheduling.timer.MethodInvokingTimerTaskFactoryBean";
            }
        }, PatternIcons.JDK_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsIntegrationJdkTimerFactoryBean().get(), "jdk-timer-factory-bean"), PatternIcons.JDK_ICON));

        setPopup(true);
    }
}