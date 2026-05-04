package com.intellij.spring.webflow.model.xml;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: plt
 */
public interface TransitionOwner extends WebflowDomElement {

  @NotNull
  List<Transition> getTransitions();

  /**
   * Adds new child to the list of transition children.
   *
   * @return created child
   */
  Transition addTransition();
}
