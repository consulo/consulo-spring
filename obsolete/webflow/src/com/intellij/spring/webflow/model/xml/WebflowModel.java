package com.intellij.spring.webflow.model.xml;

import com.intellij.util.xml.model.DomModel;

/**
 * User: plt
 */
public interface WebflowModel extends DomModel<Flow> {
  public Flow getFlow();
}