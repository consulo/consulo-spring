package com.intellij.spring.webflow.graph;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.builder.DeleteProvider;
import com.intellij.openapi.graph.builder.SimpleNodeCellEditor;
import com.intellij.openapi.graph.builder.EdgeCreationPolicy;
import com.intellij.openapi.graph.builder.components.SelectionDependenciesPresentationModel;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.layout.OrientationLayouter;
import com.intellij.openapi.graph.layout.hierarchic.HierarchicGroupLayouter;
import com.intellij.openapi.graph.layout.hierarchic.HierarchicLayouter;
import com.intellij.openapi.graph.settings.GraphSettings;
import com.intellij.openapi.graph.settings.GraphSettingsProvider;
import com.intellij.openapi.graph.view.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.xml.XmlElement;
import com.intellij.spring.webflow.graph.impl.WebflowIfEdge;
import com.intellij.spring.webflow.graph.impl.WebflowTransitionEdge;
import com.intellij.spring.webflow.graph.renderers.WebflowGlobalTransitionsNodeRenederer;
import com.intellij.spring.webflow.graph.renderers.WebflowNodeRenderer;
import com.intellij.spring.webflow.model.xml.Identified;
import com.intellij.spring.webflow.model.xml.TransitionOwner;
import com.intellij.spring.webflow.model.xml.DecisionState;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class WebflowPresentationModel extends SelectionDependenciesPresentationModel<WebflowNode, WebflowEdge> {
  private final Project myProject;
  private WebflowNodeRenderer myRenderer;
  private WebflowGlobalTransitionsNodeRenederer myGlobalTransitionsRenderer;

  private boolean myMoveSelectionMode;

  public WebflowPresentationModel(final Graph2D graph, Project project) {
    super(graph, false);
    myProject = project;
    setShowEdgeLabels(true);

    customizeDefaultSettings(GraphSettingsProvider.getInstance(project).getSettings(graph));
  }

  private static void customizeDefaultSettings(final GraphSettings settings) {
    final HierarchicGroupLayouter groupLayouter = settings.getGroupLayouter();

    groupLayouter.setOrientationLayouter(GraphManager.getGraphManager().createOrientationLayouter(OrientationLayouter.TOP_TO_BOTTOM));
    groupLayouter.setMinimalNodeDistance(20);
    groupLayouter.setMinimalLayerDistance(50);
    groupLayouter.setRoutingStyle(HierarchicLayouter.ROUTE_POLYLINE);
  }

  @NotNull
  public NodeRealizer getNodeRealizer(final WebflowNode node) {
    return node.getNodeType()== WebflowNodeType.GLOBAL_TRANSITIONS
           ? GraphViewUtil.createNodeRealizer("WebflowGlobalTransitionsNodeRenderer", getGlobalTransitionsRenderer()) : GraphViewUtil.createNodeRealizer("WebflowNodeRenderer", getRenderer());
  }

  private NodeCellRenderer getGlobalTransitionsRenderer() {
    if (myGlobalTransitionsRenderer == null) {
      myGlobalTransitionsRenderer = new WebflowGlobalTransitionsNodeRenederer();
    }
    return myGlobalTransitionsRenderer;
  }

  public WebflowNodeRenderer getRenderer() {
    if (myRenderer == null) {
      myRenderer = new WebflowNodeRenderer(getGraphBuilder(), ModificationTracker.EVER_CHANGED);
    }
    return myRenderer;
  }

  @NotNull
  public EdgeRealizer getEdgeRealizer(final WebflowEdge edge) {
    final PolyLineEdgeRealizer edgeRealizer = GraphManager.getGraphManager().createPolyLineEdgeRealizer();

    final boolean elseIfEdge = edge instanceof WebflowIfEdge.Else;

    edgeRealizer.setLineType(elseIfEdge ? LineType.DASHED_1 : LineType.LINE_1);

    edgeRealizer.setLineColor(Color.GRAY);
    edgeRealizer.setArrow(Arrow.STANDARD);

    if (edge instanceof WebflowTransitionEdge) {
      final boolean onEventTransition = ((WebflowTransitionEdge)edge).isOnEventTransition();
      if (!onEventTransition) {
         edgeRealizer.setLineType(LineType.DASHED_1);
         edgeRealizer.setLineColor(new Color(128, 0, 0));
      }
    }

    return edgeRealizer;
  }

  public boolean editNode(final WebflowNode node) {
    return super.editNode(node);
  }

  public boolean editEdge(final WebflowEdge webflowEdge) {
    final XmlElement xmlElement = webflowEdge.getIdentifyingElement().getXmlElement();
    if (xmlElement instanceof Navigatable) {
      OpenSourceUtil.navigate(new Navigatable[]{(Navigatable)xmlElement}, true);
      return true;
    }
    return super.editEdge(webflowEdge);
  }

  public Project getProject() {
    return myProject;
  }

  public String getNodeTooltip(final WebflowNode node) {
    return node.getName();
  }

  public String getEdgeTooltip(final WebflowEdge edge) {
    return "";
  }

  public void customizeSettings(final Graph2DView view, final EditMode editMode) {
    editMode.allowEdgeCreation(!myMoveSelectionMode);

    editMode.allowMovePorts(true);
    editMode.allowMoving(true);
    editMode.allowBendCreation(true);

    view.setFitContentOnResize(false);
    view.fitContent();
  }

  public DeleteProvider getDeleteProvider() {
    return new DeleteProvider<WebflowNode, WebflowEdge>() {
      public boolean canDeleteNode(@NotNull final WebflowNode node) {
        return !((CellEditorMode)getGraphBuilder().getEditMode().getEditNodeMode()).isCellEditing();
      }

      public boolean canDeleteEdge(@NotNull final WebflowEdge edge) {
        return true;
      }

      public boolean deleteNode(@NotNull final WebflowNode node) {
        new WriteCommandAction(getProject()) {
          protected void run(final Result result) throws Throwable {
             node.getIdentifyingElement().undefine();
          }
        }.execute();

        return true;
      }

      public boolean deleteEdge(@NotNull final WebflowEdge edge) {
        new WriteCommandAction(getProject()) {
          protected void run(final Result result) throws Throwable {
             edge.getIdentifyingElement().undefine();
          }
        }.execute();

        return true;
      }
    };
  }


  public NodeCellEditor getCustomNodeCellEditor(final WebflowNode webflowNode) {
    if (webflowNode.getNodeType() == WebflowNodeType.GLOBAL_TRANSITIONS) return null;

    return new SimpleNodeCellEditor<WebflowNode>(webflowNode, getProject()) {
      protected String getEditorValue(final WebflowNode value) {
        final String s = value.getName();
        return s == null ? "" : s;
      }

      protected void setEditorValue(final WebflowNode value, final String newValue) {
        final DomElement element = value.getIdentifyingElement();
        if (element instanceof Identified) {
          new WriteCommandAction(myProject) {
            protected void run(final Result result) throws Throwable {
              ((Identified)element).getId().setStringValue(newValue);
            }
          }.execute();
        }

        IdeFocusManager.getInstance(getProject()).requestFocus(getGraphBuilder().getView().getJComponent(), true);
      }
    };
  }


  public DefaultActionGroup getNodeActionGroup(final WebflowNode webflowNode) {
    final DefaultActionGroup group = super.getNodeActionGroup(webflowNode);

    group.add(ActionManager.getInstance().getAction("Webflow.Designer"), Constraints.FIRST);

    return group;
  }

  public boolean isMoveSelectionMode() {
    return myMoveSelectionMode;
  }

  public void setMoveSelectionMode(final boolean moveSelectionMode) {
    myMoveSelectionMode = moveSelectionMode;
  }

  @Override
  public EdgeCreationPolicy<WebflowNode> getEdgeCreationPolicy() {
    return new EdgeCreationPolicy<WebflowNode>() {
      public boolean acceptSource(@NotNull final WebflowNode source) {
         final DomElement element = source.getIdentifyingElement();

        return element.isValid() && (element instanceof TransitionOwner || element instanceof DecisionState);
      }

      public boolean acceptTarget(@NotNull final WebflowNode target) {
        return true;
      }
    };
  }
}
