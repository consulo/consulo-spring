package com.intellij.spring.webflow.model.xml;

import com.intellij.psi.PsiMethod;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.webflow.model.converters.ActionBeanMethodConverter;
import com.intellij.spring.webflow.model.converters.ActionBeanConverter;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

@ModelVersion(WebflowVersion.Webflow_1_0)
public interface WebflowNamedAction extends WebflowDomElement {
  /**
   * Returns the value of the bean child.
   * <pre>
   * <h3>Attribute null:bean documentation</h3>
   * The identifier of the action implementation to execute, typically the id of a bean
   * registered in a Spring BeanFactory.
   * <br>
   * If the referenced bean implements the org.springframework.webflow.execution.Action interface it is
   * retrieved from the factory and used as is.  If the bean is not an Action an exception is thrown.
   * <br>
   * This is similar to the &lt;ref bean="myBean"/&gt; notation of the Spring beans DTD.
   * </pre>
   *
   * @return the value of the bean child.
   */
  @NotNull
  @Required
  @Convert(value = ActionBeanConverter.class)
  GenericAttributeValue<SpringBeanPointer> getBean();


  /**
   * Returns the value of the name child.
   * <pre>
   * <h3>Attribute null:name documentation</h3>
   * An optional name qualifier for this action. When specified this action will
   * qualify execution result event identifiers by this name.  For example, if this
   * action is named "placeOrder" and signals a "success" result event after execution,
   * the fully qualified result event the flow can respond to would be "placeOrder.success".
   * <br>
   * This can be used to execute actions in an ordered chain, where the flow responds
   * to the the last action result in the chain:
   * <pre>
   *     &lt;action-state id="setupForm"&gt;
   *         &lt;action name="setupForm" bean="formAction" method="setupForm"/&gt;
   *         &lt;action name="loadReferenceData" bean="formAction" method="loadReferenceData"/&gt;
   *         &lt;transition on="loadReferenceData.success" to="displayForm"&gt;
   *     &lt;/action-state&gt;
   * </pre>
   * ... will execute 'setupForm' followed by 'loadRefenceData', then transition the flow to
   * the 'displayForm' state on a successful 'loadReferenceData' invocation.
   * <br>
   * An action with a name is often referred to as a "named action".
   * </pre>
   *
   * @return the value of the name child.
   */
  @NotNull
  GenericAttributeValue<String> getName();


  /**
   * Returns the value of the method child.
   * <pre>
   * <h3>Attribute null:method documentation</h3>
   * The name of the method to invoke on this action.
   * <br>
   * Use this attribute when the action is a "multi action" extending
   * org.springframework.webflow.action.MultiAction.  The value should be
   * name of the method to invoke on the multi-action instance.
   * The method's implementation must have the following signature:
   * <pre>
   *     public Event &lt;methodName&gt;(RequestContext context);
   * </pre>
   * As an example:
   * <pre>
   * 	&lt;action bean="formAction" method="setupForm"/&gt;
   * </pre>
   * 	... might invoke:
   * <pre>
   * 	public class FormAction extends MultiAction {
   * 		public Event setupForm(RequestContext context) {
   * 			return success();
   * 		}
   * 	}
   * </pre>
   * </pre>
   *
   * @return the value of the method child.
   */
  @NotNull
  @Convert(value = ActionBeanMethodConverter.class)
  GenericAttributeValue<PsiMethod> getMethod();
}
