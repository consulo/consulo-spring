package com.intellij.spring.webflow.graph.impl;

import com.intellij.spring.webflow.WebflowIcons;
import com.intellij.spring.webflow.util.WebflowUtil;
import com.intellij.spring.webflow.graph.WebflowNodeType;
import com.intellij.spring.webflow.model.xml.ViewState;
import com.intellij.spring.webflow.model.xml.WebflowNamedAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: plt
 */
public class ViewStateNode extends WebflowBasicNode<ViewState> {

  public ViewStateNode(String name, ViewState identifyingElement) {
    super(identifyingElement, name);
  }

  @NotNull
  public WebflowNodeType getNodeType() {
    return WebflowNodeType.VIEW_STATE;
  }

  public Icon getIcon() {
    return WebflowIcons.WEBFLOW_VIEW_STATE;
  }

  public List<WebflowNamedAction> getAllNodeActions() {
    List<WebflowNamedAction> actions = new ArrayList<WebflowNamedAction>();
    final ViewState state = getIdentifyingElement();
    if (state.isValid()) {
      WebflowUtil.collectActons(state.getEntryActions(), actions);
      WebflowUtil.collectActons(state.getExitActions(), actions);
      WebflowUtil.collectActons(state.getRenderActions(), actions);
    }

    return actions;
  }
}