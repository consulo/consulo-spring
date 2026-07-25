package com.intellij.spring.impl.ide.model.actions.patterns.frameworks;

import com.intellij.spring.impl.ide.model.actions.patterns.AbstarctSpringConfigActionGroup;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.image.Image;
import jakarta.annotation.Nullable;

public class FrameworkIntegrationActionGroup extends AbstarctSpringConfigActionGroup {
    public FrameworkIntegrationActionGroup() {
        super();
        add(new AddHibernateAction());
        add(new AddJdoAction());
        add(new AddToplinkAction());
        add(new AddIbatisAction());

        addSeparator();

        add(new AddWebflowAction());

        addSeparator();

        add(new AddOpenSymphonyTimerAction());
        add(new AddJdkTimerAction());
    }

    @Override
    protected String getDescription() {
        return SpringLocalize.springPatterns().get();
    }

    @Nullable
    @Override
    protected Image getIcon() {
        return SpringImplIconGroup.spring();
    }
}
