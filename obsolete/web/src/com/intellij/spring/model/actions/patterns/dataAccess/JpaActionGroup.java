package com.intellij.spring.model.actions.patterns.dataAccess;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.actions.GenerateSpringDomElementAction;
import com.intellij.spring.model.actions.generate.SpringBeanGenerateProvider;
import com.intellij.spring.model.actions.patterns.PatternIcons;

public class JpaActionGroup extends DefaultActionGroup {
  public JpaActionGroup() {
    addSeparator();

    add(new GenerateSpringDomElementAction(new JpaEntityManagerBeanGenerateProvider(), PatternIcons.JPA_ICON));
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(
      SpringBundle.message("spring.patterns.data.access.jpa.container.entity.manager.factory"), "jpa-container-entity-manager-factory"),
                                           PatternIcons.JPA_ICON));
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(
      SpringBundle.message("spring.patterns.data.access.jpa.persistence.unit.manager"), "jpa-persistent-unit-manager"),
                                           PatternIcons.JPA_ICON));
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(
      SpringBundle.message("spring.patterns.data.access.jpa.transaction.manager"), "jpa-transaction-manager"), PatternIcons.JPA_ICON));

    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(
      SpringBundle.message("spring.patterns.data.access.jpa.persistence.anno.posr.processor"), "jpa-anno-post-processor"),
                                           PatternIcons.JPA_ICON));
    add(new GenerateSpringDomElementAction(new SpringBeanGenerateProvider(
      SpringBundle.message("spring.patterns.data.access.jpa.persistence.ex.translation.posr.processor"),
      "jpa-ex-translation-post-processor"), PatternIcons.JPA_ICON));

    setPopup(false);
  }
}
