package com.intellij.spring.webflow.graph.impl;

import com.intellij.spring.webflow.WebflowIcons;
import com.intellij.spring.webflow.util.WebflowUtil;
import com.intellij.spring.webflow.graph.WebflowNodeType;
import com.intellij.spring.webflow.model.xml.DecisionState;
import com.intellij.spring.webflow.model.xml.WebflowNamedAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: plt
 */
public class DecisionStateNode extends WebflowBasicNode<DecisionState> {

  public DecisionStateNode(String name, DecisionState identifyingElement) {
    super(identifyingElement, name);
  }

  @NotNull
  public WebflowNodeType getNodeType() {
    return WebflowNodeType.DECISION_STATE;
  }

  public Icon getIcon() {
    return WebflowIcons.WEBFLOW_DECISION_STATE;
  }

  public List<WebflowNamedAction> getAllNodeActions() {
    List<WebflowNamedAction> actions = new ArrayList<WebflowNamedAction>();
    final DecisionState state = getIdentifyingElement();
    if (state.isValid()) {
      WebflowUtil.collectActons(state.getEntryActions(), actions);
      WebflowUtil.collectActons(state.getExitActions(), actions);
    }
    return actions;
  }
}