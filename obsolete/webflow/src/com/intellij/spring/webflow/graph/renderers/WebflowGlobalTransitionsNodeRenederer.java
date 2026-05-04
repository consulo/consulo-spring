package com.intellij.spring.webflow.graph.renderers;

import com.intellij.openapi.graph.builder.renderer.AbstractColoredNodeCellRenderer;
import com.intellij.openapi.graph.view.NodeRealizer;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.spring.webflow.graph.impl.GlobalTransitionsNode;

import javax.swing.*;
import java.awt.*;

public class WebflowGlobalTransitionsNodeRenederer extends AbstractColoredNodeCellRenderer {
  public WebflowGlobalTransitionsNodeRenederer() {
    super(ModificationTracker.EVER_CHANGED);
  }

  public void tuneNode(final NodeRealizer realizer, final JPanel wrapper) {
    wrapper.removeAll();

    wrapper.setLayout(new BorderLayout());

    final JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

    final JLabel jLabel = new JLabel(GlobalTransitionsNode.GLOBAL_TRANSITIONS_NODE_NAME);
    jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD));

    panel.add(jLabel);

    wrapper.add(panel, BorderLayout.CENTER);
  }
}