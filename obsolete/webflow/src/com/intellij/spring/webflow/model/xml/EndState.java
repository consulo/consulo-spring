package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.spring.webflow.model.converters.ViewStateConverter;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface EndState extends WebflowDomElement, Identified {

  @NotNull
  @Referencing(value = ViewStateConverter.class, soft = true)
  GenericAttributeValue<Object> getView();

  @NotNull
  List<Attribute> getAttributes();

  Attribute addAttribute();

  @NotNull
  List<ExceptionHandler> getExceptionHandlers();

  ExceptionHandler addExceptionHandler();

  /**
   * Marks this end-state a "commit state".  If true, any changes made to managed entities attached to the flow's persistence context
   * will be flushed in a system transaction when this state is reached.  If false, no flush will occur. This attribute has no effect
   * if the flow is not a persistence-context.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  GenericAttributeValue<String> getCommit();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  List<Output> getOutputs();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  Output addOutput();

  /**
   * Actions to execute when this state is entered.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  OnEntry getOnEntry();

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


  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  EntryActions getEntryActions();


  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  OutputMapper getOutputMapper();
}
