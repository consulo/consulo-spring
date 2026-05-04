package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ActionState extends ActionsOwner, WebflowDomElement, TransitionOwner, SetOwner, Identified {

  @NotNull
  List<Attribute> getAttributes();

  Attribute addAttribute();

  @NotNull
  List<ExceptionHandler> getExceptionHandlers();

  ExceptionHandler addExceptionHandler();



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
   * Evaluates an expression against the flow request context.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  List<Evaluate> getEvaluates();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  Evaluate addEvaluate();

  /**
   * Requests that the next view render a fragment of content.
   * Multiple fragments may be specified using a comma delimiter.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  List<Render> getRenders();


  @ModelVersion(WebflowVersion.Webflow_2_0)
  Render addRender();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  EntryActions getEntryActions();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  ExitActions getExitActions();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  List<EvaluateAction> getEvaluateActions();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  EvaluateAction addEvaluateAction();
}
