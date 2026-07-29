package com.intellij.spring.impl.model.context;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.impl.ide.model.values.PlaceholderUtils;
import com.intellij.spring.impl.ide.model.xml.context.PropertyPlaceholder;
import jakarta.annotation.Nonnull;

public abstract class PropertyPlaceholderImpl extends DomSpringBeanImpl implements PropertyPlaceholder {
    @Nonnull
    @Override
    public String getClassName() {
        return PlaceholderUtils.PLACEHOLDER_CONFIGURER_CLASS;
    }
}
