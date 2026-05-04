package com.intellij.spring.webflow.config.model.xml.version1_0;

import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.spring.webflow.config.model.xml.converters.RegistryRefConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.springframework.org/schema/webflow-config:executorElemType interface.
 */
public interface Executor extends WebflowConfigDomElement, DomSpringBean {
  @NonNls String FLOW_EXECUTOR_CLASS = "org.springframework.webflow.executor.FlowExecutor";
  /**
   * Returns the value of the repository-type child.
   * <pre>
   * <h3>Attribute null:repository-type documentation</h3>
   * The type of execution repository to use.  The repository is responsible for managing flow execution
   * persistence between requests.
   * </pre>
   *
   * @return the value of the repository-type child.
   */
  @NotNull
  GenericAttributeValue<RepositoryTypeAttribute> getRepositoryType();


  /**
   * Returns the value of the registry-ref child.
   * <pre>
   * <h3>Attribute null:registry-ref documentation</h3>
   * The idref to the registry this executor will use to locate flow definitions for execution.
   * </pre>
   *
   * @return the value of the registry-ref child.
   */
  @NotNull
  @Required
  @Convert(RegistryRefConverter.class)
  GenericAttributeValue<SpringBeanPointer> getRegistryRef();


  /**
   * Returns the value of the repository child.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/webflow-config:repository documentation</h3>
   * Explicit repository configuration for this executor.  This element is used if configuration needs
   * to be more fine grained than the repositoryType attribute on executor.
   * </pre>
   *
   * @return the value of the repository child.
   */
  @NotNull
  Repository getRepository();


  /**
   * Returns the value of the execution-attributes child.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/webflow-config:execution-attributes documentation</h3>
   * Execution attributes to associate with new flow executions launched by this executor.
   * These attributes may influence execution behavior.
   * </pre>
   *
   * @return the value of the execution-attributes child.
   */
  @NotNull
  ExecutionAttributes getExecutionAttributes();


  /**
   * Returns the value of the execution-listeners child.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/webflow-config:execution-listeners documentation</h3>
   * The listeners eligible for observing the lifecycle of executions launched by this executor.
   * </pre>
   *
   * @return the value of the execution-listeners child.
   */
  @NotNull
  ExecutionListeners getExecutionListeners();
}
