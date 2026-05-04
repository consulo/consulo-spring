package com.intellij.spring.webflow.graph;

import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

public interface WebflowEdge<T extends DomElement> {
  WebflowNode getSource();

  WebflowNode getTarget();

  @NotNull
  T getIdentifyingElement();

  @NotNull
  String getName();
}