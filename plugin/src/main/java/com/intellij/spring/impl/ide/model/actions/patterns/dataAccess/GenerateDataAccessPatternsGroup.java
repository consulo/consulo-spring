package com.intellij.spring.impl.ide.model.actions.patterns.dataAccess;

import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.patterns.PatternIcons;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithSyncUpdate;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.ui.image.Image;

public class GenerateDataAccessPatternsGroup extends DefaultActionGroup {

    public GenerateDataAccessPatternsGroup() {
        super(SpringLocalize.springPatternsDataAccessGroupName(), SpringLocalize.springPatternsDataAccessGroupName(), PatternIcons.DATA_ACCESS_GROUP_ICON);
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsDataAccessDataSource().get(), "datasource"), Image.empty()));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsDataAccessJndiDataSource().get(), "jndi-datasource"), Image.empty()));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsDataAccessTransactionManager().get(), "transaction-manager"), PatternIcons.TRANSACTION_MANAGER_ICON));
        addSeparator();
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsDataAccessHibernateSessionFactory().get(), "hibernatefactory"), PatternIcons.HIBERNATE_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsDataAccessHibernateTransactionManager().get(), "hibernate-tm"), PatternIcons.HIBERNATE_ICON));
        addSeparator();
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsDataAccessJdoPersistenceManager().get(), "jdo-persistance-manager"), PatternIcons.JDO_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsDataAccessJdoJpoxPersistenceManager().get(), "jpox-pmf"), PatternIcons.JDO_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsDataAccessJdoPersistenceManagerProxy().get(), "jdo-persistance-manager-proxy"), PatternIcons.JDO_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsDataAccessJdoTransactionManager().get(), "jdo-transaction-manager"), PatternIcons.JDO_ICON));
        addSeparator();
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsDataAccessToplinkSessionFactory().get(), "toplink-session-factory"), PatternIcons.TOPLINK_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsDataAccessToplinkTransactionAwareSessionAdapter().get(), "toplink-session-adapter"), PatternIcons.TOPLINK_ICON));
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsDataAccessToplinkTransactionManager().get(), "toplink-transaction-manager"), PatternIcons.TOPLINK_ICON));
        addSeparator();
        add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringLocalize.springPatternsDataAccessIbatisClientFactory().get(), "ibatis-client-factory"), PatternIcons.IBATIS_ICON));

        setPopup(true);
    }
}