package com.intellij.spring.webflow.graph.impl;

import com.intellij.spring.webflow.WebflowIcons;
import com.intellij.spring.webflow.graph.WebflowNodeType;
import com.intellij.spring.webflow.model.xml.GlobalTransitions;
import com.intellij.spring.webflow.model.xml.WebflowNamedAction;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public class GlobalTransitionsNode extends WebflowBasicNode<GlobalTransitions>{
  @NonNls public static String GLOBAL_TRANSITIONS_NODE_NAME = "<< Global Transitions >>";

  public GlobalTransitionsNode(@NotNull final GlobalTransitions identifyingElement) {
    super(identifyingElement, GLOBAL_TRANSITIONS_NODE_NAME);
  }

  @NotNull
  public WebflowNodeType getNodeType() {
    return WebflowNodeType.GLOBAL_TRANSITIONS;
  }

  public Icon getIcon() {
    return WebflowIcons.WEBFLOW_ON_TRANSITION_STATE;
  }

  public List<WebflowNamedAction> getAllNodeActions() {
    return Collections.emptyList();
  }
}
