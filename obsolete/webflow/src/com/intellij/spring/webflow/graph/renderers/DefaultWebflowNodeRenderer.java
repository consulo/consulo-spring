package com.intellij.spring.webflow.graph.renderers;

import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.renderer.BasicGraphNodeRenderer;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.spring.webflow.graph.WebflowEdge;
import com.intellij.spring.webflow.graph.WebflowNode;

import javax.swing.*;
import java.awt.*;

/**
 * User: plt
 */
public class DefaultWebflowNodeRenderer extends BasicGraphNodeRenderer<WebflowNode, WebflowEdge> {

  public DefaultWebflowNodeRenderer(GraphBuilder<WebflowNode, WebflowEdge> builder) {
    super(builder, ModificationTracker.EVER_CHANGED);
  }

  protected JComponent getPresenationComponent(final String text) {

    return super.getPresenationComponent(text);
  }

  protected Icon getIcon(final WebflowNode node) {
    return node.getIcon();
  }

  protected String getNodeName(final WebflowNode node) {
    return node.getName();
  }

  protected Color getBackground(final WebflowNode node) {
    return Color.LIGHT_GRAY;
  }

  protected int getSelectionBorderWidth() {
    return 1;
  }
}