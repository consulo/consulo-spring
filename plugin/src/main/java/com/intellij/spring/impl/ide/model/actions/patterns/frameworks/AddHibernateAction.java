package com.intellij.spring.impl.ide.model.actions.patterns.frameworks;

import java.util.LinkedList;
import java.util.List;

import consulo.localize.LocalizeValue;
import consulo.spring.localize.SpringLocalize;
import jakarta.annotation.Nullable;

import consulo.language.editor.template.TemplateSettings;
import consulo.java.ex.facet.LibraryInfo;
import consulo.module.Module;
import com.intellij.spring.impl.ide.model.actions.patterns.PatternIcons;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui.LibrariesInfo;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui.TemplateInfo;
import com.intellij.spring.impl.ide.model.actions.patterns.frameworks.util.LibrariesConfigurationManager;
import consulo.ui.image.Image;

public class AddHibernateAction extends AbstractFrameworkIntegrationAction {
    private static final String HIBERNATE_STRING_ID = "hibernate";
    private static final String HIBERNATE_FACET_NAME = "Hibernate";

    @Override
    protected String[] getBeansClassNames() {
        return new String[]{"org.springframework.orm.hibernate3.LocalSessionFactoryBean",
            "org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean"};
    }

    @Override
    protected LibrariesInfo getLibrariesInfo(Module module) {
        LibraryInfo[] libraryInfos = LibrariesConfigurationManager.getInstance(module.getProject()).getLibraryInfos("hibernate");

        return new LibrariesInfo(libraryInfos, module, HIBERNATE_STRING_ID);
    }

    @Override
    protected List<TemplateInfo> getTemplateInfos(Module module) {
        List<TemplateInfo> infos = new LinkedList<>();

        TemplateSettings settings = TemplateSettings.getInstance();

        TemplateInfo datasource = new TemplateInfo(
            module,
            settings.getTemplateById("datasource"),
            SpringLocalize.springPatternsDataAccessDataSource()
        );
        TemplateInfo jndiDatasource = new TemplateInfo(
            module,
            settings.getTemplateById("jndi-datasource"),
            SpringLocalize.springPatternsDataAccessJndiDataSource()
        );
        jndiDatasource.setAccepted(false);

        TemplateInfo sessionFactory = new TemplateInfo(
            module,
            settings.getTemplateById("hibernatefactory"),
            SpringLocalize.springPatternsDataAccessHibernateSessionFactory()
        );
        TemplateInfo transactionManager = new TemplateInfo(
            module,
            settings.getTemplateById("hibernate-tm"),
            SpringLocalize.springPatternsDataAccessHibernateTransactionManager()
        );

        infos.add(datasource);
        infos.add(jndiDatasource);
        infos.add(sessionFactory);
        infos.add(transactionManager);

        return infos;
    }

    @Nullable
    @Override
    protected String getFacetId() {
        return HIBERNATE_STRING_ID;
    }

    @Override
    protected LocalizeValue getDescription() {
        return SpringLocalize.springPatternsHibernate();
    }

    @Nullable
    @Override
    protected Image getIcon() {
        return PatternIcons.HIBERNATE_ICON;
    }
}
