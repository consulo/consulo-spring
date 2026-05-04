package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.spring.webflow.model.converters.ViewStateConverter;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ViewState extends WebflowDomElement, TransitionOwner, Identified {

  @NotNull
  @Referencing(value = ViewStateConverter.class, soft = true)    
  GenericAttributeValue<Object> getView();

  @NotNull
  List<ExceptionHandler> getExceptionHandlers();

  ExceptionHandler addExceptionHandler();

  @NotNull
  List<Attribute> getAttributes();

  Attribute addAttribute();

  /**
   * Requests this view-state send a flow execution redirect before rendering.  Default is false.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  GenericAttributeValue<Boolean> getRedirect();

  /**
   * Displays the view in a popup dialog.  Default is false.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  GenericAttributeValue<Boolean> getPopup();

  /**
   * The model object this view is bound to.  Typically used as the source of form field values or other data input controls.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  GenericAttributeValue<String> getModel();


  /**
   * Configures the process of binding UI elements such as form fields to model properties.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0_3)
  @NotNull
  List<Binder> getBinders();

  Binder addBinder();

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
   * Actions to execute immediately before view rendering.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  OnRender getOnRender();

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
   * A view instance variable.  View variables are created when this state is entered and destroyed when this state exits.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  List<Var> getVars();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  Var addVar();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  EntryActions getEntryActions();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  RenderActions getRenderActions();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  ExitActions getExitActions();
}
