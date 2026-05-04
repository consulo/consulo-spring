// Generated on Thu Mar 20 18:19:54 MSK 2008
// DTD/Schema  :    http://www.springframework.org/schema/webflow-config

package com.intellij.spring.webflow.config.model.xml.version1_0;

import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.webflow.config.model.xml.WebflowConfigDomElement;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/webflow-config:registryElemType interface.
 */
public interface Registry extends DomSpringBean, WebflowConfigDomElement {

  @NonNls String FLOW_DEFINITION_REGISTRY_CLASS = "org.springframework.webflow.definition.registry.FlowDefinitionRegistry";

  /**
   * Returns the list of location children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/webflow-config:location documentation</h3>
   * Specifies a path to a externalized flow definition resource.  The flow definition built from this
   * resource will be registered in this registry.
   * <br>
   * Individual paths such as:
   * <pre>
   * 	/WEB-INF/flows/orderitem-flow.xml
   * </pre>
   * ... are supported as well as wildcard paths such as:
   * <pre>
   */
  @NotNull
  @Required
  List<Location> getLocations();

  /**
   * Adds new child to the list of location children.
   *
   * @return created child
   */
  Location addLocation();
}
