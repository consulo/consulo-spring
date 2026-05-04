package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.Required;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_1_0)
public interface InlineFlow extends WebflowDomElement, Identified {

  /**
   * Returns the list of flow children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/webflow:flow documentation</h3>
   * Defines exactly one flow definition.  A flow is composed of one or more states
   * that define the steps of a task.  One of those steps is the start state, which defines
   * the starting point of the task.
   * <br>
   * A flow may also exhibit the following characteristics:
   * <ul>
   * <li>Be annotated with attributes that define descriptive properties that may affect flow execution.
   * (See the &lt;attribute/&gt; element)
   * <li>Instantiate a set of application variables when started.
   * (See the &lt;var/&gt; element)
   * <li>Map input provided by callers that start it
   * (See the &lt;input-mapper/&gt; element)
   * <li>Return output to callers that end it.
   * (See the &lt;output-mapper/&gt; element)
   * <li>Execute custom behaviors at start time and end time.
   * (See the &lt;start-actions/&gt; and &lt;end-actions/&gt; elements)
   * <li>Define transitions shared by all states.
   * (See the &lt;global-transitions/&gt; element)
   * <li>Handle exceptions thrown by its states during execution.
   * (See the &lt;exception-handler/&gt; element)
   * <li>Import one or more local bean definition files defining custom flow artifacts
   * (such as actions, exception handlers, view selectors, transition criteria, etc).
   * (See the &lt;import/&gt; element)
   * <li>Finally, a flow may nest one or more other flows within this document to
   * use as subflows, referred to as 'inline flows'.
   * (See the &lt;inline-flow/&gt; element)
   * </ul>
   * </pre>
         *
         * @return the list of flow children.
         */
	@NotNull
	@Required
	List<Flow> getFlows();
	/**
         * Adds new child to the list of flow children.
         *
         * @return created child
         */
	Flow addFlow();


}
