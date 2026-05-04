package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ModelVersion(WebflowVersion.Webflow_1_0)
public interface BeanAction extends WebflowNamedAction {

  @NotNull
  List<Attribute> getAttributes();

  Attribute addAttribute();

  @NotNull
  MethodArguments getMethodArguments();

  @NotNull
  MethodResult getMethodResult();
}
