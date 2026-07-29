package com.intellij.spring.impl.model.jee;

import jakarta.annotation.Nonnull;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.impl.ide.model.xml.jee.RemoteSlsb;

public abstract class RemoteSlsbImpl extends DomSpringBeanImpl implements RemoteSlsb {
    @Nonnull
    @Override
    public String getClassName() {
        return "org.springframework.ejb.access.SimpleRemoteStatelessSessionProxyFactoryBean";
    }
}