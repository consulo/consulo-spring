package com.intellij.spring.impl.ide.model.actions.patterns.frameworks;

import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nullable;
import consulo.language.editor.template.TemplateSettings;
import consulo.java.ex.facet.LibraryInfo;
import consulo.module.Module;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.model.actions.patterns.PatternIcons;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui.LibrariesInfo;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui.TemplateInfo;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.util.LibrariesConfigurationManager;
import consulo.ui.image.Image;

public class AddToplinkAction extends AbstractFrameworkIntegrationAction {
  @NonNls private static final String TOPLINK_STRING_ID = "toplink";

  protected String[] getBeansClassNames() {
    return new String[]{"org.springframework.orm.toplink.LocalSessionFactoryBean"};
  }

   protected LibrariesInfo getLibrariesInfo(final consulo.module.Module module) {
    final LibraryInfo[] libraryInfos = LibrariesConfigurationManager.getInstance(module.getProject()).getLibraryInfos("toplink");

    return new LibrariesInfo(libraryInfos, module, TOPLINK_STRING_ID);
  }

  protected List<TemplateInfo> getTemplateInfos(final Module module) {
    List<TemplateInfo> infos = new LinkedList<TemplateInfo>();


    final TemplateSettings settings = TemplateSettings.getInstance();

    final TemplateInfo datasource = new TemplateInfo(module, settings.getTemplateById("datasource"),
                                                     SpringBundle.message("spring.patterns.data.access.data.source"), null, false);

    final TemplateInfo sf = new TemplateInfo(module, settings.getTemplateById("toplink-session-factory"),
                                                     SpringBundle.message("spring.patterns.data.access.toplink.session.factory"), null);

    final TemplateInfo sfa = new TemplateInfo(module, settings.getTemplateById("toplink-session-adapter"),
                                                     SpringBundle.message("spring.patterns.data.access.toplink.transaction.aware.session.adapter"), null, false);

    final TemplateInfo ttm = new TemplateInfo(module, settings.getTemplateById("toplink-transaction-manager"),
                                                         SpringBundle.message("spring.patterns.data.access.toplink.transaction.manager"),
                                                         null);

    infos.add(datasource);
    infos.add(sf);
    infos.add(sfa);
    infos.add(ttm);

    return infos;
  }

  protected String getDescription() {
    return SpringBundle.message("spring.patterns.toplink");
  }

  @Nullable
  protected Image getIcon() {
    return PatternIcons.TOPLINK_ICON;
  }
}
