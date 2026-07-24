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

public class AddIbatisAction extends AbstractFrameworkIntegrationAction {
    private static final String IBATIS_STRING_ID = "ibatis";

    @Override
    protected String[] getBeansClassNames() {
        return new String[]{"org.springframework.orm.ibatis.SqlMapClientFactoryBean"};
    }

    @Override
    protected LibrariesInfo getLibrariesInfo(Module module) {
        LibraryInfo[] libraryInfos = LibrariesConfigurationManager.getInstance(module.getProject()).getLibraryInfos("ibatis");

        return new LibrariesInfo(libraryInfos, module, IBATIS_STRING_ID);
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

        TemplateInfo cf = new TemplateInfo(
            module,
            settings.getTemplateById("ibatis-client-factory"),
            SpringLocalize.springPatternsDataAccessIbatisClientFactory()
        );

        infos.add(datasource);
        infos.add(cf);

        return infos;
    }

    @Override
    protected LocalizeValue getDescription() {
        return SpringLocalize.springPatternsIbatis();
    }

    @Nullable
    @Override
    protected Image getIcon() {
        return PatternIcons.IBATIS_ICON;
    }
}

