package com.intellij.spring.webflow.model.xml;

import com.intellij.psi.PsiClass;
import com.intellij.spring.webflow.constants.WebflowConstants;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.spring.webflow.model.converters.IdentifiedStateConverter;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.ExtendClass;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/webflow:transitionElemType interface.
 */
public interface Transition extends ActionsOwner, EvaluatesOwner, SetOwner, WebflowDomElement {

  /**
   * Returns the value of the on child.
   * <pre>
   * <h3>Attribute null:on documentation</h3>
   * The criteria that determines when this transition should execute.  This criteria typically evaluates
   * the last event that occurred in this flow to determine if the transition matches and should execute.
   * <br>
   * The most basic value is a static event id:
   * <pre>
   * 	&lt;transition on="submit" to="state"/&gt;
   * </pre>
   * ... which reads "on an occurrence of the 'submit' event transition to 'state'"
   * <br>
   * Sophisticated transitional expressions are also supported when enclosed within ${brackets}:
   * <pre>
   * 	&lt;transition on="${#result == 'submit' &;amp;&amp flowScope.attribute == 'foo'}" to="state"/&gt;
   * </pre>
   * Custom transition criteria implementations can be referenced by id:
   * <pre>
   * 	&lt;transition on="bean:myCustomCriteria" to="state"/&gt;
   * </pre>
   * The exact interpretation of this attribute value depends on the TextToTransitionCriteria
   * converter that is installed.
   * </pre>
   *
   * @return the value of the on child.
   */
  @NotNull
  GenericAttributeValue<String> getOn();


  /**
   * Returns the value of the on-exception child.
   * <pre>
   * <h3>Attribute null:on-exception documentation</h3>
   * The exception type that should trigger execution of this transition.
   * <br>
   * The value must be a fully-qualified Exception class name (e.g. example.booking.ItineraryExpiredException).
   * When an exception is thrown, superclasses of the configured exception class match by default.
   * <br>
   * Use of this attribute results in an exception handler being attached to the object associated
   * with this transition definition.  Use this attribute or the 'on' attribute, not both.
   * </pre>
   *
   * @return the value of the on-exception child.
   */
  @NotNull
  @ExtendClass(value = WebflowConstants.ON_EXCEPTION_EXTENDS_CLASS, instantiatable = false)
  GenericAttributeValue<PsiClass> getOnException();


  /**
   * Returns the value of the to child.
   * <pre>
   * <h3>Attribute null:to documentation</h3>
   * The target state to transition to.
   * <br>
   * The value of this attribute may be a static state identifier (e.g. to="displayForm")
   * or an expression to be evaluated at runtime against the request context
   * (e.g. to="${flowScope.previousViewState}"). Custom target state resolvers implementations
   * can be referenced by id (e.g. to="bean:myCustomTargetStateResolver"). The
   * exact interpretation of this attribute value depends on the installed TextToTargetStateResolver.
   * </pre>
   *
   * @return the value of the to child.
   */
  @NotNull
  @Convert(IdentifiedStateConverter.class)
  GenericAttributeValue<Object> getTo();

  @NotNull
  List<Attribute> getAttributes();

  Attribute addAttribute();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  Secured getSecured();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  @NotNull
  List<EvaluateAction> getEvaluateActions();

  @ModelVersion(WebflowVersion.Webflow_1_0)
  EvaluateAction addEvaluateAction();

  /**
   * Requests that the next view render a fragment of content.
   * Multiple fragments may be specified using a comma delimiter.
   */
  @ModelVersion(WebflowVersion.Webflow_2_0)
  @NotNull
  List<Render> getRenders();

  @ModelVersion(WebflowVersion.Webflow_2_0)
  Render addRender();
}
