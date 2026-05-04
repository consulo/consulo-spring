package com.intellij.spring.webflow.model.xml;

import com.intellij.spring.webflow.model.ModelVersion;
import com.intellij.spring.webflow.model.WebflowVersion;

@ModelVersion(WebflowVersion.Webflow_2_0)
public interface OnRender extends EvaluatesOwner, RendersOwner, SetsOwner, WebflowDomElement {
}
