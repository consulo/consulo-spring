package com.intellij.spring.webflow;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ApplicationComponent;
import consulo.disposer.Disposer;
import com.intellij.spring.factories.SpringFactoryBeansManager;
import com.intellij.spring.factories.resolvers.SingleObjectTypeResolver;
import com.intellij.spring.webflow.config.model.xml.version2_0.FlowExecutor;
import com.intellij.spring.webflow.config.model.xml.version2_0.FlowRegistry;
import com.intellij.spring.webflow.inspections.WebflowConfigModelInspection;
import com.intellij.spring.webflow.inspections.WebflowModelInspection;
import com.intellij.spring.webflow.model.xml.Input;
import com.intellij.spring.webflow.resources.messages.WebflowBundle;
import com.intellij.util.xml.TypeNameManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * User: plt
 */
public class WebflowApplicationComponent implements ApplicationComponent, InspectionToolProvider, Disposable {

  @NonNls
  @NotNull
  public String getComponentName() {
    return getClass().getName();
  }

  public void initComponent() {
    registerMetaData();
    registerPresentations();

    registerRenameValidators();

    addResources();

    registerFactoryBeans();
  }

  private static void registerFactoryBeans() {
    SpringFactoryBeansManager.getInstance().registerFactory(FlowRegistry.FLOW_REGISTRY_FACTORY_BEAN_CLASS, new SingleObjectTypeResolver(FlowRegistry.FLOW_REGISTRY_CLASS));
    SpringFactoryBeansManager.getInstance().registerFactory(FlowExecutor.FLOW_EXECUTOR_FACTORY_BEAN_CLASS, new SingleObjectTypeResolver(FlowExecutor.FLOW_EXECUTOR_CLASS));
  }

  private static void addResources() {
  }

  private static void registerMetaData() {
  }

  private static void registerPresentations() {
    TypeNameManager.registerTypeName(Input.class, WebflowBundle.message("input.variable"));
  }

  private static void registerRenameValidators() {
  }

  public void dispose() {
  }

  public void disposeComponent() {
    Disposer.dispose(this);
  }

  public Class[] getInspectionClasses() {
    return new Class[]{WebflowModelInspection.class, WebflowConfigModelInspection.class};
  }

}