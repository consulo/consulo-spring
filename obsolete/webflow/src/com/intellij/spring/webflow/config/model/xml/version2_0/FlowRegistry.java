package com.intellij.spring.webflow.config.model.xml.version2_0;

import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.spring.webflow.config.model.xml.converters.FlowBuilderServicesConverter;
import com.intellij.spring.webflow.config.model.xml.converters.FlowRegistryParentConverter;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface FlowRegistry extends WebflowConfigDomElement, DomSpringBean {
  @NonNls String FLOW_REGISTRY_FACTORY_BEAN_CLASS = "org.springframework.webflow.config.FlowRegistryFactoryBean";
  @NonNls String FLOW_REGISTRY_CLASS = "org.springframework.webflow.definition.registry.FlowDefinitionRegistry";

  @NotNull
  @Convert(FlowBuilderServicesConverter.class)    
  GenericAttributeValue<SpringBeanPointer> getFlowBuilderServices();

  @NotNull
  @Convert(FlowRegistryParentConverter.class)
  @Attribute("parent")
  GenericAttributeValue<FlowRegistry> getParentFlowRegistry();

  @NotNull
  List<FlowLocation> getFlowLocations();

  @NotNull
  List<FlowLocationPattern> getFlowLocationPatterns();

  @NotNull
  List<FlowBuilder> getFlowBuilders();
}
