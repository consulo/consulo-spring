package com.intellij.spring.webflow.graph.impl;

import com.intellij.spring.webflow.graph.WebflowEdge;
import com.intellij.spring.webflow.graph.WebflowNode;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

/**
 * User: plt
 */
public abstract class WebflowBasicEdge<T extends DomElement> implements WebflowEdge<T> {
  private final WebflowNode mySource;
  private final WebflowNode myTarget;
  private final T myIdentifying;

  public WebflowNode getSource() {
    return mySource;
  }

  public WebflowBasicEdge(final WebflowNode source, final WebflowNode target, final T identifying) {
    mySource = source;
    myTarget = target;
    myIdentifying = identifying;
  }

  public WebflowNode getTarget() {
    return myTarget;
  }

  @NotNull
  public T getIdentifyingElement() {
    return myIdentifying;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final WebflowBasicEdge that = (WebflowBasicEdge)o;

    if (mySource != null ? !mySource.equals(that.mySource) : that.mySource != null) return false;
    if (myTarget != null ? !myTarget.equals(that.myTarget) : that.myTarget != null) return false;
    if (myIdentifying != null ? !myIdentifying.equals(that.myIdentifying) : that.myIdentifying != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (mySource != null ? mySource.hashCode() : 0);
    result = 31 * result + (myTarget != null ? myTarget.hashCode() : 0);
    result = 31 * result + (myIdentifying != null ? myIdentifying.hashCode() : 0);
    return result;
  }
}
