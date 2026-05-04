package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_1_0)
public interface InputMapper extends WebflowDomElement {

  /**
   * Returns the list of input-attribute children.
   *
   * @return the list of input-attribute children.
   */
  @NotNull
  List<InputAttribute> getInputAttributes();

  /**
   * Adds new child to the list of input-attribute children.
   *
   * @return created child
   */
  InputAttribute addInputAttribute();


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
