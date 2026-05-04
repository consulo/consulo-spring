package com.intellij.spring.webflow;

import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.webflow.config.model.xml.version1_0.Executor;
import com.intellij.spring.webflow.config.model.xml.version1_0.Registry;
import com.intellij.spring.webflow.config.model.xml.version2_0.FlowBuilderServices;
import com.intellij.spring.webflow.config.model.xml.version2_0.FlowExecutor;
import com.intellij.spring.webflow.config.model.xml.version2_0.FlowRegistry;
import com.intellij.spring.webflow.constants.WebflowConstants;
import com.intellij.util.xml.XmlName;
import com.intellij.util.xml.reflect.DomExtender;
import com.intellij.util.xml.reflect.DomExtensionsRegistrar;
import org.jetbrains.annotations.NotNull;

public class WebflowDomExtender extends DomExtender<Beans> {

  public void registerExtensions(@NotNull final Beans element, @NotNull final DomExtensionsRegistrar registrar) {
    // version 2
    registrar.registerCollectionChildrenExtension(new XmlName("flow-registry", WebflowConstants.WEBFLOW_CONFIG_NAMESPACE_KEY), FlowRegistry.class);
    registrar.registerCollectionChildrenExtension(new XmlName("flow-executor", WebflowConstants.WEBFLOW_CONFIG_NAMESPACE_KEY), FlowExecutor.class);
    registrar.registerCollectionChildrenExtension(new XmlName("flow-builder-services", WebflowConstants.WEBFLOW_CONFIG_NAMESPACE_KEY), FlowBuilderServices.class);

    // version 1
    registrar.registerCollectionChildrenExtension(new XmlName("executor", WebflowConstants.WEBFLOW_CONFIG_NAMESPACE_KEY), Executor.class);
    registrar.registerCollectionChildrenExtension(new XmlName("registry", WebflowConstants.WEBFLOW_CONFIG_NAMESPACE_KEY), Registry.class);
  }
}

