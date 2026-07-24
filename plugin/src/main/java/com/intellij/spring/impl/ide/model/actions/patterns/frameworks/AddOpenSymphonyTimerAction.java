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

public class AddOpenSymphonyTimerAction extends AbstractFrameworkIntegrationAction {
    private static final String QUARTZ_ID = "quartz";

    @Override
    protected String[] getBeansClassNames() {
        return new String[]{"org.springframework.scheduling.quartz.JobDetailBean"};
    }

    @Override
    protected LibrariesInfo getLibrariesInfo(Module module) {
        LibraryInfo[] libraryInfos = LibrariesConfigurationManager.getInstance(module.getProject()).getLibraryInfos("quartz");

        return new LibrariesInfo(libraryInfos, module, QUARTZ_ID);
    }

    @Override
    protected List<TemplateInfo> getTemplateInfos(Module module) {
        List<TemplateInfo> infos = new LinkedList<>();

        TemplateSettings settings = TemplateSettings.getInstance();

        TemplateInfo job = new TemplateInfo(
            module,
            settings.getTemplateById("quartz-job-detail"),
            SpringLocalize.springPatternsIntegrationOpensymphonyJobDetailBean()
        );

        TemplateInfo simpleTrigger = new TemplateInfo(
            module,
            settings.getTemplateById("quartz-simple-trigger"),
            SpringLocalize.springPatternsIntegrationOpensymphonySimpleTrigger()
        );

        TemplateInfo cronTrigger = new TemplateInfo(
            module,
            settings.getTemplateById("quartz-cron-trigger"),
            SpringLocalize.springPatternsIntegrationOpensymphonyCronTrigger(),
            false
        );

        TemplateInfo sf = new TemplateInfo(
            module,
            settings.getTemplateById("quartz-scheduler-factory"),
            SpringLocalize.springPatternsIntegrationOpensymphonyScheduler()
        );

        infos.add(job);
        infos.add(simpleTrigger);
        infos.add(cronTrigger);
        infos.add(sf);

        return infos;
    }

    @Override
    protected LocalizeValue getDescription() {
        return SpringLocalize.springPatternsQuartzScheduler();
    }

    @Nullable
    @Override
    protected Image getIcon() {
        return PatternIcons.SCHEDULER_ICON;
    }
}
