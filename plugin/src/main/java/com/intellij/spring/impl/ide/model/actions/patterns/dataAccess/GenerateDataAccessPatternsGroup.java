package com.intellij.spring.impl.ide.model.actions.patterns.dataAccess;

import consulo.application.AllIcons;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DefaultActionGroup;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.patterns.PatternIcons;

public class GenerateDataAccessPatternsGroup extends DefaultActionGroup {

  public GenerateDataAccessPatternsGroup() {
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.data.access.data.source"), "datasource"), AllIcons.Nodes.DataSource)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.data.access.jndi.data.source"), "jndi-datasource"), AllIcons.Nodes.DataSource)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.data.access.transaction.manager"), "transaction-manager"), PatternIcons.TRANSACTION_MANAGER_ICON)) ;
    addSeparator();
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.data.access.hibernate.session.factory"), "hibernatefactory"), PatternIcons.HIBERNATE_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.data.access.hibernate.transaction.manager"), "hibernate-tm"), PatternIcons.HIBERNATE_ICON)) ;
    addSeparator();
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.data.access.jdo.persistence.manager"), "jdo-persistance-manager"), PatternIcons.JDO_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.data.access.jdo.jpox.persistence.manager"), "jpox-pmf"), PatternIcons.JDO_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.data.access.jdo.persistence.manager.proxy"), "jdo-persistance-manager-proxy"), PatternIcons.JDO_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.data.access.jdo.transaction.manager"), "jdo-transaction-manager"), PatternIcons.JDO_ICON)) ;
    addSeparator();
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.data.access.toplink.session.factory"), "toplink-session-factory"), PatternIcons.TOPLINK_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.data.access.toplink.transaction.aware.session.adapter"), "toplink-session-adapter"), PatternIcons.TOPLINK_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.data.access.toplink.transaction.manager"), "toplink-transaction-manager"), PatternIcons.TOPLINK_ICON)) ;
    addSeparator();
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.data.access.ibatis.client.factory"), "ibatis-client-factory"), PatternIcons.IBATIS_ICON)) ;

    setPopup(true);
  }

  public void update(final AnActionEvent e) {
    super.update(e);
    e.getPresentation().setText(SpringBundle.message("spring.patterns.data.access.group.name"));
    e.getPresentation().setIcon(PatternIcons.DATA_ACCESS_GROUP_ICON);
  }

}