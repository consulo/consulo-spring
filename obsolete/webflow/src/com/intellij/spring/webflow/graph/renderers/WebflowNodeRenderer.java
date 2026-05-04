package com.intellij.spring.webflow.graph.renderers;

import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.renderer.AbstractColoredNodeCellRenderer;
import com.intellij.openapi.graph.builder.renderer.GradientFilledPanel;
import com.intellij.openapi.graph.view.NodeRealizer;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.spring.webflow.WebflowIcons;
import com.intellij.spring.webflow.graph.WebflowEdge;
import com.intellij.spring.webflow.graph.WebflowNode;
import com.intellij.spring.webflow.model.xml.WebflowNamedAction;
import com.intellij.spring.webflow.util.WebflowUtil;
import com.intellij.ui.IdeBorderFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WebflowNodeRenderer extends AbstractColoredNodeCellRenderer {

  private final GraphBuilder<WebflowNode, WebflowEdge> myBuilder;
  private final Color myBackgroundColor = new Color(252, 250, 209);
  private final Color myCaptionBackgroundColor = new Color(215, 213, 172);

  public WebflowNodeRenderer(@NotNull GraphBuilder<WebflowNode, WebflowEdge> builder, ModificationTracker modificationTracker) {
    super(modificationTracker);
    myBuilder = builder;
  }

  public void tuneNode(final NodeRealizer realizer, final JPanel wrapper) {
    wrapper.removeAll();
    final Node node = realizer.getNode();
    final WebflowNode webflowNode = myBuilder.getNodeObject(node);
    if (webflowNode != null) {
      JLabel nameLabel = new JLabel(webflowNode.getName(), getIcon(webflowNode), JLabel.HORIZONTAL);

      nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
      nameLabel.setBorder(IdeBorderFactory.createEmptyBorder(3, 3, 3, 3));
      nameLabel.setHorizontalAlignment(SwingConstants.LEFT);

      GradientFilledPanel namePanel = new GradientFilledPanel(myCaptionBackgroundColor);
      namePanel.setLayout(new BorderLayout());
      namePanel.add(nameLabel, BorderLayout.CENTER);
      namePanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
      nameLabel.setForeground(Color.BLACK);
      wrapper.add(namePanel, BorderLayout.NORTH);

      final JPanel actionsPanel = new JPanel(new GridBagLayout());
      actionsPanel.setBorder(IdeBorderFactory.createEmptyBorder(2, 5, 2, 5));
      actionsPanel.setBackground(myBackgroundColor);

      final List<WebflowNamedAction> nodeActions = webflowNode.getAllNodeActions();

      if (!nodeActions.isEmpty()) {
        for (WebflowNamedAction namedAction : nodeActions) {
          final String beanName = namedAction.getBean().getStringValue();
          final String methodName = namedAction.getMethod().getStringValue();

          String actionName =
              StringUtil.isEmptyOrSpaces(beanName) ? "" : beanName + (StringUtil.isEmptyOrSpaces(methodName) ? "" : "." + methodName);

          if (!StringUtil.isEmptyOrSpaces(actionName)) {
            final JLabel propertyNameLabel = new JLabel(actionName, WebflowIcons.WEBFLOW_ACTION, JLabel.HORIZONTAL);


            final JLabel descriptionLabel = new JLabel("");
            propertyNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
            descriptionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            actionsPanel.add(propertyNameLabel, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 0,
                                                                       GridBagConstraints.LINE_START, GridBagConstraints.BOTH,
                                                                       new Insets(2, 2, 2, 2), 0, 0));
            actionsPanel.add(descriptionLabel, new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 1, 0,
                                                                      GridBagConstraints.LINE_END, GridBagConstraints.BOTH,
                                                                      new Insets(2, 2, 2, 2), 0, 0));
          }
        }
        Dimension preferredSize = actionsPanel.getPreferredSize();
        actionsPanel.setPreferredSize(new Dimension((int)preferredSize.getWidth() + 20, (int)preferredSize.getHeight()));

      }
      else {
        Dimension preferredSize = nameLabel.getPreferredSize();
        nameLabel.setPreferredSize(new Dimension((int)preferredSize.getWidth() + 25, (int)preferredSize.getHeight()));
      }

      wrapper.add(actionsPanel, BorderLayout.CENTER);
    }
  }

  private static Icon getIcon(final WebflowNode webflowNode) {
    return WebflowUtil.isStartState(webflowNode) ? WebflowIcons.WEBFLOW_START_STATE :webflowNode.getIcon();
  }

  protected int getSelectionBorderWidth() {
    return 2;
  }
}
