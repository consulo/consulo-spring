package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.spring.webflow.model.converters.FlowNameConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/webflow:subflow-stateElemType interface.
 */
public interface SubflowState extends WebflowDomElement, TransitionOwner, Identified {

  @NotNull
  List<ExceptionHandler> getExceptionHandlers();

  ExceptionHandler addExceptionHandler();

  @NotNull
  List<Attribute> getAttributes();

  Attribute addAttribute();

  /**
   * The subflow to start.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  @Convert(FlowNameConverter.class)
  GenericAttributeValue<Flow> getSubflow();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  List<Input> getInputs();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  Input addInput();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  List<Output> getOutputs();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  Output addOutput();

  /**
   * For exotic usage scenarios, a custom SubflowAttributeMapper bean to use to map data to the subflow.
   * Use this attribute or the input/output sub-elements, not both.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  GenericAttributeValue<String> getSubflowAttributeMapper();

  /**
   * Actions to execute when this state is entered.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  OnEntry getOnEntry();

  /**
   * Actions to execute when this state exits.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  OnExit getOnExit();

  /**
   * Flow identifier to inherit from for this state only.
   * For example <pre>&lt;state id="state" parent="flowId#stateId"&gt;</pre>
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  @com.intellij.util.xml.Attribute("parent")
  GenericAttributeValue<String> getParentAction();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  Secured getSecured();

  /**
   * The id of the flow to be spawned as a subflow when this subflow state is entered.
   */
  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  @Convert(FlowNameConverter.class)
  GenericAttributeValue<Flow> getFlow();


  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  EntryActions getEntryActions();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  AttributeMapper getAttributeMapper();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  ExitActions getExitActions();
}
