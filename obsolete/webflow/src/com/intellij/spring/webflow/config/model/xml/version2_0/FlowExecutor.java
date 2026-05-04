package com.intellij.spring.webflow.config.model.xml.version2_0;

import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface FlowExecutor extends WebflowConfigDomElement, DomSpringBean {
  @NonNls String FLOW_EXECUTOR_FACTORY_BEAN_CLASS = "org.springframework.webflow.config.FlowExecutorFactoryBean";
  @NonNls String FLOW_EXECUTOR_CLASS = "org.springframework.webflow.executor.FlowExecutor";

  @NotNull
  @SubTagList("flow-execution-repository")   
  GenericAttributeValue<String> getFlowRegistry();

  @NotNull
  List<FlowExecutionRepository> getFlowExecutionRepositories();

  @NotNull
  @SubTagList("flow-execution-attributes")    
  List<FlowExecutionAttributes> getFlowExecutionAttributeses();

  @NotNull
  @SubTagList("flow-execution-listeners")
  List<FlowExecutionListeners> getFlowExecutionListenerses();
}
