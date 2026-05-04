package com.intellij.spring.webflow.fileEditor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.GraphBuilderFactory;
import com.intellij.openapi.graph.builder.dnd.GraphDnDUtils;
import com.intellij.openapi.graph.builder.dnd.SimpleDnDPanel;
import com.intellij.openapi.graph.builder.util.GraphViewUtil;
import com.intellij.openapi.graph.view.Graph2D;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.graph.view.Overview;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import consulo.disposer.Disposer;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.webflow.graph.*;
import com.intellij.spring.webflow.graph.actions.MoveSelectionModeAction;
import com.intellij.spring.webflow.graph.dnd.WebflowDnDSupport;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementNavigationProvider;
import com.intellij.util.xml.DomEventAdapter;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.events.DomEvent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: plt
 */
public class WebflowDesignerComponent extends JPanel implements DataProvider, Disposable {
  @NonNls public static final String SPRING_WEBFLOW_DESIGNER_COMPONENT = "SPRING_WEBFLOW_DESIGNER_COMPONENT";
  @NonNls private static final String SPRING_WEBFLOW_DESIGNER_NAVIGATION_PROVIDER_NAME = "SPRING_WEBFLOW_DESIGNER_NAVIGATION_PROVIDER_NAME";

  @NonNls private final SpringWebflowDesignerNavigationProvider myNavigationProvider = new SpringWebflowDesignerNavigationProvider();

  private final GraphBuilder<WebflowNode, WebflowEdge> myBuilder;
  private final XmlFile myXmlFile;
  private final WebflowDataModel myDataModel;

  public WebflowDesignerComponent(final XmlFile xmlFile) {
    myXmlFile = xmlFile;
    final Project project = xmlFile.getProject();

    final Graph2D graph = GraphManager.getGraphManager().createGraph2D();
    final Graph2DView view = GraphManager.getGraphManager().createGraph2DView();
    myDataModel = new WebflowDataModel(xmlFile);
    WebflowPresentationModel presentationModel = new WebflowPresentationModel(graph, project);

    myBuilder = GraphBuilderFactory.getInstance(project).createGraphBuilder(graph, view, myDataModel, presentationModel);

    GraphViewUtil.addDataProvider(view, new MyDataProvider(myBuilder));

    final Splitter splitter = new Splitter(false, 0.85f);

    setLayout(new BorderLayout());

    final SimpleDnDPanel simpleDnDPanel = GraphDnDUtils.createDnDActions(project, myBuilder, new WebflowDnDSupport(myDataModel));
    final JComponent graphComponent = myBuilder.getView().getJComponent();

    splitter.setSecondComponent(simpleDnDPanel.getTree());
    splitter.setFirstComponent(graphComponent);
    splitter.setDividerWidth(5);

    add(createToolbarPanel(), BorderLayout.NORTH);
    add(splitter, BorderLayout.CENTER);

    Disposer.register(this, myBuilder);

    myBuilder.initialize();
    myBuilder.getView().fitContent();
    
    DomManager.getDomManager(myBuilder.getProject()).addDomEventListener(new DomEventAdapter() {
      public void eventOccured(final DomEvent event) {
        if (isShowing()) {
          simpleDnDPanel.getBuilder().updateFromRoot();
          myBuilder.queueUpdate();
        }
      }
    }, this);
  }

  private JComponent createToolbarPanel() {
    DefaultActionGroup actions = new DefaultActionGroup();
    // todo add custom actions

    actions.add(new MoveSelectionModeAction(myBuilder));
    actions.addSeparator();
    actions.add(GraphViewUtil.getBasicToolbar(myBuilder));

    final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actions, true);

    return actionToolbar.getComponent();
  }

  public List<DomElement> getSelectedDomElements() {
    List<DomElement> selected = new ArrayList<DomElement>();
    final Graph2D graph = myBuilder.getGraph();
    for (Node n : graph.getNodeArray()) {
      if (graph.isSelected(n)) {
        final WebflowNode nodeObject = myBuilder.getNodeObject(n);
        if (nodeObject != null) {
          ContainerUtil.addIfNotNull(nodeObject.getIdentifyingElement(), selected);
        }
      }
    }
    return selected;
  }

  public void setSelectedDomElement(final DomElement domElement) {

  }

  public GraphBuilder getBuilder() {
    return myBuilder;
  }

  public Overview getOverview() {
    return GraphManager.getGraphManager().createOverview(myBuilder.getView());
  }

  public void dispose() {
  }

  private class SpringWebflowDesignerNavigationProvider extends DomElementNavigationProvider {
    public String getProviderName() {
      return SPRING_WEBFLOW_DESIGNER_NAVIGATION_PROVIDER_NAME;
    }

    public void navigate(final DomElement domElement, final boolean requestFocus) {
      setSelectedDomElement(domElement);
    }

    public boolean canNavigate(final DomElement domElement) {
      return domElement.isValid();
    }
  }

  public SpringWebflowDesignerNavigationProvider getNavigationProvider() {
    return myNavigationProvider;
  }

  public XmlFile getXmlFile() {
    return myXmlFile;
  }

  @Nullable
  public Object getData(@NonNls final String dataId) {
    if (dataId.equals(SPRING_WEBFLOW_DESIGNER_COMPONENT)) return this;

    return null;
  }

  public WebflowDataModel getDataModel() {
    return myDataModel;
  }

  private class MyDataProvider implements DataProvider {
    private final Project myProject;
    private final Graph2D myGraph;

    public MyDataProvider(final GraphBuilder<WebflowNode, WebflowEdge> builder) {
      myProject = builder.getProject();
      myGraph = builder.getGraph();
    }

    @Nullable
    public Object getData(@NonNls String dataId) {
      if (dataId.equals(DataConstants.PROJECT)) {
        return myProject;
      }
      else if (dataId.equals(DataConstants.PSI_ELEMENT)) {
        for (Node node : myGraph.getNodeArray()) {
          if (myGraph.getRealizer(node).isSelected()) {
            final WebflowNode webflowNode = myBuilder.getNodeObject(node);
            if (webflowNode != null) {
              return webflowNode.getIdentifyingElement().getXmlElement();
            }
          }
        }
      }
      return null;
    }
  }
}