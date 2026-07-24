package com.intellij.spring.webflow.graph.impl;

import com.intellij.spring.webflow.graph.WebflowNode;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: plt
 */
public abstract class WebflowBasicNode<T extends DomElement> implements WebflowNode<T> {

  private final T myIdentifyingElement;
  private final String myName;

  protected WebflowBasicNode(@NotNull T identifyingElement, @Nullable String name) {
    myIdentifyingElement = identifyingElement;
    myName = name;
  }

  public String getName() {
    return myName;
  }

  @NotNull
  public T getIdentifyingElement() {
    return myIdentifyingElement;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    WebflowBasicNode that = (WebflowBasicNode)o;

    if (!myIdentifyingElement.equals(that.myIdentifyingElement)) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = myIdentifyingElement.hashCode();
    result = 31 * result + (myName != null ? myName.hashCode() : 0);
    return result;
  }
}
