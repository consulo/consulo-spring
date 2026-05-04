package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DecisionState extends WebflowDomElement, Identified {

  @NotNull
  List<Attribute> getAttributes();

  Attribute addAttribute();

  @NotNull
  List<ExceptionHandler> getExceptionHandlers();

  ExceptionHandler addExceptionHandler();


  @NotNull
  List<If> getIfs();

  If addIf();

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


  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  EntryActions getEntryActions();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  ExitActions getExitActions();
}
