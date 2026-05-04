package com.intellij.spring.model.actions.patterns.dataAccess;

import consulo.language.editor.impl.internal.template.TemplateImpl;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.module.Module;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.actions.patterns.PatternIcons;
import com.intellij.spring.model.actions.patterns.frameworks.AbstractFrameworkIntegrationAction;
import com.intellij.spring.model.actions.patterns.frameworks.ui.LibrariesInfo;
import com.intellij.spring.model.actions.patterns.frameworks.ui.TemplateInfo;
import com.intellij.spring.model.actions.patterns.frameworks.util.LibrariesConfigurationManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

public class JpaPatternAction extends AbstractFrameworkIntegrationAction {
  @NonNls private static final String JPA_STRING_ID = "jpa";
  @NonNls private static final String JPA_FACET_NAME = "Jpa";

  protected String[] getBeansClassNames() {
    return new String[]{"org.springframework.orm.jpa.LocalEntityManagerFactoryBean"};
  }

  protected LibrariesInfo getLibrariesInfo(final Module module) {
    final LibraryInfo[] libraryInfos = LibrariesConfigurationManager.getInstance(module.getProject()).getLibraryInfos("jpa");

    return new LibrariesInfo(libraryInfos, module, JPA_STRING_ID);
  }

  protected List<TemplateInfo> getTemplateInfos(final Module module) {
    List<TemplateInfo> infos = new LinkedList<TemplateInfo>();

    final TemplateSettings settings = TemplateSettings.getInstance();

    final TemplateImpl template = (TemplateImpl)JpaEntityManagerBeanGenerateProvider.getTemplate(module.getProject());
    template.setId("jpa-entity-manager-factory");

    final TemplateInfo emf = new TemplateInfo(module, template,
                                                     SpringBundle.message("spring.patterns.data.access.jpa.entity.manager.factory"), null);

    final TemplateInfo cemf = new TemplateInfo(module, settings.getTemplateById("jpa-container-entity-manager-factory"),
                                                     SpringBundle.message("spring.patterns.data.access.jpa.container.entity.manager.factory"), null, false);


    final TemplateInfo pum = new TemplateInfo(module, settings.getTemplateById("jpa-persistent-unit-manager"),
                                                         SpringBundle.message("spring.patterns.data.access.jpa.persistence.unit.manager"),
                                                         null, false);
    final TemplateInfo tm = new TemplateInfo(module, settings.getTemplateById("jpa-transaction-manager"), SpringBundle.message(
      "spring.patterns.data.access.jpa.transaction.manager"), null);

    final TemplateInfo anno = new TemplateInfo(module, settings.getTemplateById("jpa-anno-post-processor"), SpringBundle.message(
          "spring.patterns.data.access.jpa.persistence.anno.posr.processor"), null, false);

    final TemplateInfo ex  = new TemplateInfo(module, settings.getTemplateById("jpa-ex-translation-post-processor"), SpringBundle.message(
          "spring.patterns.data.access.jpa.persistence.ex.translation.posr.processor"), null, false);

    infos.add(emf);
    infos.add(cemf);
    infos.add(pum);
    infos.add(tm);
    infos.add(anno);
    infos.add(ex);

    return infos;
  }

  protected void addFacet(final Module module) {

  }

  @Nullable
  protected String getFacetId() {
    return JPA_STRING_ID;
  }

  protected String getDescription() {
    return SpringBundle.message("spring.patterns.jpa");
  }

  @Nullable
  protected Icon getIcon() {
    return PatternIcons.JPA_ICON;
  }
}
