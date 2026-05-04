package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_2_0_3)
public interface Binder extends WebflowDomElement {

  /**
   * Defines a view model binding.  A binding connects a UI element in this view to a model property.
   */
  @NotNull
  List<Binding> getBindings();

  Binding addBinding();

}
