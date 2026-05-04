package com.intellij.spring.webflow.graph.impl;

import com.intellij.spring.webflow.graph.WebflowNodeType;
import com.intellij.spring.webflow.model.xml.Transition;
import com.intellij.spring.webflow.model.xml.WebflowNamedAction;
import com.intellij.spring.webflow.WebflowIcons;
import com.intellij.util.Icons;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public abstract class GlobalTransitionNode extends WebflowBasicNode<GenericAttributeValue<String>> {
  private final Transition myTransition;

  public GlobalTransitionNode(final Transition transition, final GenericAttributeValue<String> identifying) {
    super(identifying, identifying.getValue());
    myTransition = transition;
  }

  public Transition getTransition() {
    return myTransition;
  }

  @NotNull
  public WebflowNodeType getNodeType() {
    return WebflowNodeType.GLOBAL_TRANSITIONS;
  }

  public List<WebflowNamedAction> getAllNodeActions() {
    return Collections.emptyList();
  }

  public static class OnExceptionTransition extends GlobalTransitionNode {
    public OnExceptionTransition(final Transition transition, final GenericAttributeValue<String> identifying) {
      super(transition, identifying);
    }

    public Icon getIcon() {
      return Icons.EXCEPTION_CLASS_ICON;
    }
  }

  public static class OnTransition extends GlobalTransitionNode {
    public OnTransition(final Transition transition, final GenericAttributeValue<String> identifying) {
      super(transition, identifying);
    }

    public Icon getIcon() {
      return WebflowIcons.WEBFLOW_ON_TRANSITION_STATE;
    }
  }
}
