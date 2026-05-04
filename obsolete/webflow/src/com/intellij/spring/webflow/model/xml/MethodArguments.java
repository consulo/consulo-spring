package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.Required;
import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_1_0)
public interface MethodArguments extends WebflowDomElement {

  @NotNull
  @Required
  List<Argument> getArguments();

  Argument addArgument();
}
