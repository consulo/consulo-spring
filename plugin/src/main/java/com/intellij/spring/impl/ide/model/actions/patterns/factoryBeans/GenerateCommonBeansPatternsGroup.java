package com.intellij.spring.impl.ide.model.actions.patterns.factoryBeans;

import consulo.ui.ex.action.DefaultActionGroup;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringIcons;
import com.intellij.spring.impl.ide.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.impl.ide.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.impl.ide.model.actions.patterns.PatternIcons;
import consulo.ui.ex.action.AnActionEvent;

public class GenerateCommonBeansPatternsGroup extends DefaultActionGroup
{

  public GenerateCommonBeansPatternsGroup() {
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.common.beans.placeholder"), "placeholder-configurer"), SpringIcons.SPRING_BEAN_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.common.beans.property.override.configurer"), "property-override-configurer"), SpringIcons.SPRING_BEAN_ICON)) ;
    addSeparator();
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.factory.beans.resource"), "resource-factory"), PatternIcons.FACTORY_BEAN_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.factory.beans.commons.log"), "commons-log-factory"), PatternIcons.FACTORY_BEAN_ICON)) ;
    addSeparator();
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.factory.beans.set"), "set-factory"), PatternIcons.FACTORY_BEAN_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.factory.beans.list"), "list-factory"), PatternIcons.FACTORY_BEAN_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.factory.beans.map"), "map-factory"), PatternIcons.FACTORY_BEAN_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.factory.beans.properties"), "properties-factory"), PatternIcons.FACTORY_BEAN_ICON)) ;
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(SpringBundle.message("spring.patterns.factory.beans.field.retrieving"), "field-factory"), PatternIcons.FACTORY_BEAN_ICON)) ;

    setPopup(true);
  }

  public void update(final AnActionEvent e) {
    super.update(e);
    e.getPresentation().setText(SpringBundle.message("spring.patterns.common.beans.group.name"));
    e.getPresentation().setIcon(PatternIcons.FACTORY_BEAN_ICON);
  }

}