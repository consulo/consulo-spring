package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_2_0)
public interface Render extends WebflowDomElement {

  /**
   * The fragments of the next view to render.  Multiple fragments may be specified by using the comma delimiter.
   * Each fragment is a template expression.
   */
  @NotNull
  @Required
  GenericAttributeValue<String> getFragments();


  @NotNull
  List<Attribute> getAttributes();

  Attribute addAttribute();
}
