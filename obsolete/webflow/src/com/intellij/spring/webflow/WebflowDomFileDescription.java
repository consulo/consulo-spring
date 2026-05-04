package com.intellij.spring.webflow;


import com.intellij.spring.SpringDomFileDescription;
import com.intellij.spring.webflow.config.model.xml.impl.version1_0.ExecutorImpl;
import com.intellij.spring.webflow.config.model.xml.impl.version1_0.RegistryImpl;
import com.intellij.spring.webflow.config.model.xml.impl.version2_0.FlowBuilderServicesImpl;
import com.intellij.spring.webflow.config.model.xml.impl.version2_0.FlowExecutorImpl;
import com.intellij.spring.webflow.config.model.xml.impl.version2_0.FlowRegistryImpl;
import com.intellij.spring.webflow.config.model.xml.version1_0.Executor;
import com.intellij.spring.webflow.config.model.xml.version1_0.Registry;
import com.intellij.spring.webflow.config.model.xml.version2_0.FlowBuilderServices;
import com.intellij.spring.webflow.config.model.xml.version2_0.FlowExecutor;
import com.intellij.spring.webflow.config.model.xml.version2_0.FlowRegistry;
import com.intellij.spring.webflow.constants.WebflowConstants;
import com.intellij.spring.webflow.model.xml.Flow;
import com.intellij.util.xml.DomFileDescription;

/**
 * User: plt
 */
public class WebflowDomFileDescription extends DomFileDescription<Flow> {

  public WebflowDomFileDescription() {
    super(Flow.class, "flow");
  }

  protected void initializeFileDescription() {
    registerNamespacePolicy(WebflowConstants.WEBFLOW_NAMESPACE_KEY, WebflowConstants.WEBFLOW_NAMESPACE);

    registerSpringBeansExtensions(SpringDomFileDescription.getInstance());
  }

  private void registerSpringBeansExtensions(final SpringDomFileDescription springDomFileDescription) {
    springDomFileDescription.registerNamespacePolicy(WebflowConstants.WEBFLOW_CONFIG_NAMESPACE_KEY, WebflowConstants.WEBFLOW_CONFIG_NAMESPACE);
    // version 2
    registerImplementation(FlowRegistry.class, FlowRegistryImpl.class);
    registerImplementation(FlowExecutor.class, FlowExecutorImpl.class);
    registerImplementation(FlowBuilderServices.class, FlowBuilderServicesImpl.class);

    // version 1
    registerImplementation(Registry.class, RegistryImpl.class);
    registerImplementation(Executor.class, ExecutorImpl.class);
  }
}