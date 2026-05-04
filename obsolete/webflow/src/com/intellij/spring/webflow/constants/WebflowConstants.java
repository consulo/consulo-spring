package com.intellij.spring.webflow.constants;

import org.jetbrains.annotations.NonNls;

/**
 * User: plt
 */
public interface WebflowConstants {
  @NonNls String WEBFLOW_NAMESPACE_KEY = "Spring Webflow namespace key";

  @NonNls String WEBFLOW_NAMESPACE = "http://www.springframework.org/schema/webflow";
  
  @NonNls String WEBFLOW_1_0_SCHEMA = "http://www.springframework.org/schema/webflow/spring-webflow-1.0.xsd";
  @NonNls String WEBFLOW_2_0_SCHEMA = "http://www.springframework.org/schema/webflow/spring-webflow-2.0.xsd";

  @NonNls String WEBFLOW_CONFIG_NAMESPACE_KEY = "Spring Webflow Config namespace key";
  @NonNls String WEBFLOW_CONFIG_NAMESPACE = "http://www.springframework.org/schema/webflow-config";

  @NonNls String WEBFLOW_CONFIG_1_0_SCHEMA = "http://www.springframework.org/schema/webflow-config/spring-webflow-config-1.0.xsd";
  @NonNls String WEBFLOW_CONFIG_2_0_SCHEMA = "http://www.springframework.org/schema/webflow-config/spring-webflow-config-2.0.xsd";

  @NonNls String FLOW_EXECUTION_HANDLER_CLASSNAME = "org.springframework.webflow.engine.FlowExecutionExceptionHandler";
  @NonNls String FLOW_ATTRIBUTE_MAPPER_CLASSNAME = "org.springframework.webflow.engine.FlowAttributeMapper";
  @NonNls String EXECUTION_LISTENER_CLASS_NAME = "org.springframework.webflow.execution.FlowExecutionListener";
  @NonNls String CONVERSATION_MANAGER_CLASS_NAME = "org.springframework.webflow.conversation.ConversationManager";
  @NonNls String FLOW_BUILDER_SERVICES_CLASS_NAME = "org.springframework.webflow.engine.builder.support.FlowBuilderServices";

  @NonNls String ACTION_BEAN_CLASSNAME = "org.springframework.webflow.execution.Action";
  @NonNls String MULTI_ACTION_BEAN_CLASSNAME = "org.springframework.webflow.action.MultiAction";
  @NonNls String ACTION_BEAN_METHOD_PARAMETER_CLASSNAME = "org.springframework.webflow.execution.RequestContext";
  @NonNls String ACTION_BEAN_METHOD_RETURN_TYPE_CLASSNAME = "org.springframework.webflow.execution.Event";
  @NonNls String ON_EXCEPTION_EXTENDS_CLASS ="java.lang.Throwable"; 
}
