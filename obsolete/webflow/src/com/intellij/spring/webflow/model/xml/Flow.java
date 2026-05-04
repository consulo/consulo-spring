package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.spring.webflow.model.converters.IdentifiedStateConverter;
import com.intellij.spring.webflow.model.converters.ParentFlowsConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Flow extends WebflowDomElement {

  @NotNull
  List<ActionState> getActionStates();

  ActionState addActionState();

  @NotNull
  List<ViewState> getViewStates();

  ViewState addViewState();

  @NotNull
  List<DecisionState> getDecisionStates();

  DecisionState addDecisionState();

  @NotNull
  List<SubflowState> getSubflowStates();

  SubflowState addSubflowState();

  @NotNull
  List<EndState> getEndStates();

  EndState addEndState();

  @NotNull
  GlobalTransitions getGlobalTransitions();

  @NotNull
  List<ExceptionHandler> getExceptionHandlers();

  ExceptionHandler addExceptionHandler();

  @NotNull
  List<Attribute> getAttributes();

  Attribute addAttribute();

  @NotNull
  List<Var> getVars();

  Var addVar();

  // *** WEBFLOW 1.0 ***
  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  InputMapper getInputMapper();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  StartActions getStartActions();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  StartState getStartState();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  EndActions getEndActions();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  OutputMapper getOutputMapper();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  List<Import> getImports();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  Import addImport();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  List<InlineFlow> getInlineFlows();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  InlineFlow addInlineFlow();


  // *** WEBFLOW 2.0 ***

  /**
   * The starting point of this flow.  If not specified, the start state is the first state defined in this document.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  @com.intellij.util.xml.Attribute("start-state")
  @Convert(IdentifiedStateConverter.class)
  GenericAttributeValue<Object> getStartStateAttr();

  /**
   * Marks a flow model as abstract, preventing it from instantiating as a flow definition.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  GenericAttributeValue<Boolean> getAbstract();

  /**
   * A comma separated list of flow identifier for this flow to inherit from.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  @com.intellij.util.xml.Attribute("parent")
  @Convert(ParentFlowsConverter.class)
  GenericAttributeValue<List<Flow>> getParentFlow();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  Secured getSecured();

  /**
   * Allocates a persistence context when this flow starts.  The persistence context is closed when the flow ends.
   * If the flow ends by reaching a "commit" end-state, changes made to managed persistent entities
   * during the course of flow execution are flushed to the database in a transaction.
   * <br>
   * The persistence context can be referenced from within this flow by the "entityManager" variable.
   * </pre>
   *
   * @return the value of the persistence-context child.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  GenericDomValue<String> getPersistenceContext();

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
   * Actions to execute when this flow starts.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  OnStart getOnStart();

  /**
   * Actions to execute when this flow ends.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  OnEnd getOnEnd();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  List<BeanImport> getBeanImports();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  BeanImport addBeanImport();

}
