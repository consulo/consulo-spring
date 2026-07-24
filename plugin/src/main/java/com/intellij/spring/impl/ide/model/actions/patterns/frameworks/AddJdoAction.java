package com.intellij.spring.impl.ide.model.actions.patterns.frameworks;

import com.intellij.spring.impl.ide.model.actions.patterns.PatternIcons;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui.LibrariesInfo;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui.TemplateInfo;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.util.LibrariesConfigurationManager;
import consulo.java.ex.facet.LibraryInfo;
import consulo.language.editor.template.TemplateSettings;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.image.Image;
import jakarta.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

public class AddJdoAction extends AbstractFrameworkIntegrationAction {
    private static final String JDO_STRING_ID = "jdo";

    @Override
    protected String[] getBeansClassNames() {
        return new String[]{
            "org.springframework.orm.jdo.LocalPersistenceManagerFactoryBean",
            "org.jpox.PersistenceManagerFactoryImpl"
        };
    }

    @Override
    protected LibrariesInfo getLibrariesInfo(consulo.module.Module module) {
        LibraryInfo[] libraryInfos = LibrariesConfigurationManager.getInstance(module.getProject()).getLibraryInfos("jdo");

        return new LibrariesInfo(libraryInfos, module, JDO_STRING_ID);
    }

    @Override
    protected List<TemplateInfo> getTemplateInfos(Module module) {
        List<TemplateInfo> infos = new LinkedList<>();

        TemplateSettings settings = TemplateSettings.getInstance();

        TemplateInfo datasource = new TemplateInfo(
            module,
            settings.getTemplateById("datasource"),
            SpringLocalize.springPatternsDataAccessDataSource(),
            false
        );

        TemplateInfo jpm = new TemplateInfo(
            module,
            settings.getTemplateById("jdo-persistance-manager"),
            SpringLocalize.springPatternsDataAccessJdoPersistenceManager()
        );

        TemplateInfo jpox = new TemplateInfo(
            module,
            settings.getTemplateById("jpox-pmf"),
            SpringLocalize.springPatternsDataAccessJdoJpoxPersistenceManager(),
            false
        );

        TemplateInfo pmp = new TemplateInfo(
            module,
            settings.getTemplateById("jdo-persistance-manager-proxy"),
            SpringLocalize.springPatternsDataAccessJdoPersistenceManagerProxy()
        );
        TemplateInfo transactionManager = new TemplateInfo(
            module,
            settings.getTemplateById("jdo-transaction-manager"),
            SpringLocalize.springPatternsDataAccessJdoTransactionManager()
        );

        infos.add(jpm);
        infos.add(datasource);
        infos.add(jpox);
        infos.add(pmp);
        infos.add(transactionManager);

        return infos;
    }

    @Override
    protected LocalizeValue getDescription() {
        return SpringLocalize.springPatternsJdo();
    }

    @Nullable
    @Override
    protected Image getIcon() {
        return PatternIcons.JDO_ICON;
    }
}
