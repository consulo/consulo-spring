package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_1_0)
public interface OutputMapper extends WebflowDomElement {

  /**
   * Returns the list of output-attribute children.
   *
   * @return the list of output-attribute children.
   */
  @NotNull
  List<OutputAttribute> getOutputAttributes();

  /**
   * Adds new child to the list of output-attribute children.
   *
   * @return created child
   */
  OutputAttribute addOutputAttribute();


  /**
   * Returns the list of mapping children.
   *
   * @return the list of mapping children.
   */
  @NotNull
  List<Mapping> getMappings();

  /**
   * Adds new child to the list of mapping children.
         *
         * @return created child
         */
	Mapping addMapping();


}
