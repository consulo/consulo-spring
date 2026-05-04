package com.intellij.spring.webflow.graph.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.GraphPresentationModel;
import com.intellij.openapi.graph.builder.actions.AbstractGraphToggleAction;
import com.intellij.openapi.graph.builder.components.BasicGraphPresentationModel;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.project.Project;
import com.intellij.spring.webflow.WebflowIcons;
import com.intellij.spring.webflow.graph.WebflowEdge;
import com.intellij.spring.webflow.graph.WebflowNode;
import com.intellij.spring.webflow.graph.WebflowPresentationModel;
import com.intellij.spring.webflow.resources.messages.WebflowBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoveSelectionModeAction extends AbstractGraphToggleAction {
  private GraphBuilder<WebflowNode, WebflowEdge> myBuilder;

  public MoveSelectionModeAction() {
    super(null);
  }

  public MoveSelectionModeAction(final GraphBuilder<WebflowNode, WebflowEdge> builder) {
    super(builder.getGraph(), WebflowIcons.WEBFLOW_VIEW_STATE); // todo icon

    myBuilder = builder;
  }

  @Nullable
   protected GraphBuilder getBuilder(final AnActionEvent e) {
    return myBuilder == null ? super.getBuilder(e) : myBuilder;
  }

  public void update(final AnActionEvent e) {
    super.update(e);
    final GraphBuilder builder = getBuilder(e);

    e.getPresentation().setVisible(builder != null && builder.getGraphPresentationModel() instanceof BasicGraphPresentationModel);
  }

  protected boolean isSelected(Graph2D graph, final Project project, final AnActionEvent event) {
    final GraphBuilder builder = getBuilder(event);

    if (builder != null) {
      final GraphPresentationModel graphPresentationModel = builder.getGraphPresentationModel();
      if (graphPresentationModel instanceof WebflowPresentationModel) {
        return ((WebflowPresentationModel)graphPresentationModel).isMoveSelectionMode();
      }
    }
    return false;
  }

  protected void setSelected(Graph2D graph, boolean state, final Project project, final AnActionEvent e) {
    final GraphBuilder builder = getBuilder(e);

    if (builder != null) {
      final GraphPresentationModel graphPresentationModel = builder.getGraphPresentationModel();
      if (graphPresentationModel instanceof WebflowPresentationModel) {
        ((WebflowPresentationModel)graphPresentationModel).setMoveSelectionMode(state);
        
        builder.getEditMode().allowEdgeCreation(!state);
      }
    }
  }

  protected String getText(final @NotNull Graph2D graph) {
    return WebflowBundle.message("action.move.selection.mode");
  }
}
