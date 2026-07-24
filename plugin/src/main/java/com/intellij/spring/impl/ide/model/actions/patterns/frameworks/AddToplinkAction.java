package com.intellij.spring.impl.ide.model.actions.patterns.frameworks;

import java.util.LinkedList;
import java.util.List;

import consulo.localize.LocalizeValue;
import consulo.spring.localize.SpringLocalize;
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
    private static final String TOPLINK_STRING_ID = "toplink";

    @Override
    protected String[] getBeansClassNames() {
        return new String[]{"org.springframework.orm.toplink.LocalSessionFactoryBean"};
    }

    @Override
    protected LibrariesInfo getLibrariesInfo(consulo.module.Module module) {
        LibraryInfo[] libraryInfos = LibrariesConfigurationManager.getInstance(module.getProject()).getLibraryInfos("toplink");

        return new LibrariesInfo(libraryInfos, module, TOPLINK_STRING_ID);
    }

    @Override
    protected List<TemplateInfo> getTemplateInfos(Module module) {
        List<TemplateInfo> infos = new LinkedList<>();

        TemplateSettings settings = TemplateSettings.getInstance();

        TemplateInfo datasource = new TemplateInfo(
            module,
            settings.getTemplateById("datasource"),
            SpringLocalize.springPatternsDataAccessDataSource(),
            LocalizeValue.empty(),
            false
        );

        TemplateInfo sf = new TemplateInfo(
            module,
            settings.getTemplateById("toplink-session-factory"),
            SpringLocalize.springPatternsDataAccessToplinkSessionFactory()
        );

        TemplateInfo sfa = new TemplateInfo(
            module,
            settings.getTemplateById("toplink-session-adapter"),
            SpringLocalize.springPatternsDataAccessToplinkTransactionAwareSessionAdapter(),
            LocalizeValue.empty(),
            false
        );

        TemplateInfo ttm = new TemplateInfo(
            module,
            settings.getTemplateById("toplink-transaction-manager"),
            SpringLocalize.springPatternsDataAccessToplinkTransactionManager()
        );

        infos.add(datasource);
        infos.add(sf);
        infos.add(sfa);
        infos.add(ttm);

        return infos;
    }

    @Override
    protected LocalizeValue getDescription() {
        return SpringLocalize.springPatternsToplink();
    }

    @Nullable
    @Override
    protected Image getIcon() {
        return PatternIcons.TOPLINK_ICON;
    }
}
