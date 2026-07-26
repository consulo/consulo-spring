package com.intellij.spring.impl.model.jee;

import jakarta.annotation.Nonnull;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.impl.ide.model.xml.jee.JndiLookup;

public abstract class JndiLookupImpl extends DomSpringBeanImpl implements JndiLookup {
    @Nonnull
    @Override
    public String getClassName() {
        return "org.springframework.jndi.JndiObjectFactoryBean";
    }
}