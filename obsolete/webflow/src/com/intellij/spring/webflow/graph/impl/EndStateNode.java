package com.intellij.spring.webflow.graph.impl;

import com.intellij.spring.webflow.WebflowIcons;
import com.intellij.spring.webflow.util.WebflowUtil;
import com.intellij.spring.webflow.graph.WebflowNodeType;
import com.intellij.spring.webflow.model.xml.EndState;
import com.intellij.spring.webflow.model.xml.WebflowNamedAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: plt
 */
public class EndStateNode extends WebflowBasicNode<EndState> {

  public EndStateNode(String name, EndState identifyingElement) {
    super(identifyingElement, name);
  }

  @NotNull
  public WebflowNodeType getNodeType() {
    return WebflowNodeType.END_STATE;
  }

  public Icon getIcon() {
    return WebflowIcons.WEBFLOW_END_STATE;
  }

  public List<WebflowNamedAction> getAllNodeActions() {
    final EndState state = getIdentifyingElement();
    return state.isValid() ? WebflowUtil.collectActons(state.getEntryActions(), new ArrayList<WebflowNamedAction>()) : Collections.<WebflowNamedAction>emptyList();
  }
}