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

public class AddJdkTimerAction extends AbstractFrameworkIntegrationAction {
    private static final String JDK_TIMER_STRING_ID = "jdk-timer";

    @Override
    protected LibrariesInfo getLibrariesInfo(consulo.module.Module module) {
        LibraryInfo[] libraryInfos = LibrariesConfigurationManager.getInstance(module.getProject()).getLibraryInfos("jdk-timer");

        return new LibrariesInfo(libraryInfos, module, JDK_TIMER_STRING_ID);
    }

    @Override
    protected String[] getBeansClassNames() {
        return new String[]{
            "org.springframework.scheduling.timer.ScheduledTimerTask",
            "org.springframework.scheduling.timer.TimerFactoryBean"
        };
    }

    @Override
    protected List<TemplateInfo> getTemplateInfos(Module module) {
        List<TemplateInfo> infos = new LinkedList<>();

        TemplateSettings settings = TemplateSettings.getInstance();

        TemplateInfo sf = new TemplateInfo(
            module,
            settings.getTemplateById("jdk-scheduled-timer-task"),
            SpringLocalize.springPatternsIntegrationJdkScheduledTimerTask()
        );

        TemplateInfo tfb = new TemplateInfo(
            module,
            settings.getTemplateById("jdk-timer-factory-bean"),
            SpringLocalize.springPatternsIntegrationJdkTimerFactoryBean()
        );

        infos.add(sf);
        infos.add(tfb);

        return infos;
    }

    @Override
    protected LocalizeValue getDescription() {
        return SpringLocalize.springPatternsJdkTimer();
    }

    @Nullable
    @Override
    protected Image getIcon() {
        return PatternIcons.JDK_ICON;
    }
}

