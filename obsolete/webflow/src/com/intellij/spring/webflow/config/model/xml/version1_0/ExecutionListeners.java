package com.intellij.spring.webflow.config.model.xml.version1_0;

import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/webflow-config:execution-listenersType interface.
 */
public interface ExecutionListeners extends WebflowConfigDomElement {

  /**
   * Returns the list of listener children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/webflow-config:listener documentation</h3>
   * Deploys a single flow execution listener that will observe the execution lifecycle of one or more
   * flow definitions.  The flow definitions this listener applies to may be restricted by specifying criteria.
   * </pre>
   *
   * @return the list of listener children.
   */
  @NotNull
  @Required
  List<Listener> getListeners();

  /**
   * Adds new child to the list of listener children.
   *
   * @return created child
   */
  Listener addListener();
}
