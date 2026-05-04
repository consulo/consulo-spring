package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_1_0)
public interface EvaluateAction extends WebflowDomElement {

  /**
   * Returns the value of the expression child.
   * <pre>
   * <h3>Attribute null:expression documentation</h3>
   * The expression to evaluate when this action is invoked.
   * </pre>
   *
   * @return the value of the expression child.
   */
  @NotNull
  @Required
  GenericAttributeValue<String> getExpression();


  /**
   * Returns the value of the name child.
   * <pre>
   * <h3>Attribute null:name documentation</h3>
   * An optional name qualifier for this evaluate action. When specified this action will
   * qualify execution result event identifiers by this name.  For example, if this
   * action is named "firstInterviewQuestion" and signals a "success" result event after execution,
   * the fully qualified result event the flow can respond to would be "firstInterviewQuestion.success".
   * <br>
   * This can be used to execute actions in an ordered chain, where the flow responds
   * to the the last action result in the chain:
   * <pre>
   *     &lt;action-state id="setupForm"&gt;
   *         &lt;evaluate-action name="firstInterviewQuestion" bean="flowScope.interview.firstQuestion()"/&gt;
   *         &lt;action name="setupForm" bean="formAction" method="setupForm"/&gt;
   *         &lt;transition on="setupForm.success" to="displayForm"&gt;
   *     &lt;/action-state&gt;
   * </pre>
   * ... will execute 'firstInterviewQuestion' followed by 'setupForm', then transition the flow to
   * the 'displayForm' state on a successful 'setupForm' invocation.
   * <br>
   * An action with a name is often referred to as a "named action".
   * </pre>
   *
   * @return the value of the name child.
   */
  @NotNull
  GenericAttributeValue<String> getName();


  @NotNull
  List<Attribute> getAttributes();

  Attribute addAttribute();

  @NotNull
  EvaluationResult getEvaluationResult();


}
