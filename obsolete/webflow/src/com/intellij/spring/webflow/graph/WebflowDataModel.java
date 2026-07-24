package com.intellij.spring.webflow.graph;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.graph.builder.GraphDataModel;
import com.intellij.openapi.graph.builder.NodesGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.webflow.graph.impl.*;
import com.intellij.spring.webflow.model.xml.*;
import com.intellij.spring.webflow.resources.messages.WebflowBundle;
import com.intellij.spring.webflow.util.WebflowUtil;
import java.util.function.Function;
import java.util.HashSet;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WebflowDataModel extends GraphDataModel<WebflowNode, WebflowEdge> {
  private final Collection<WebflowNode> myNodes = new HashSet<WebflowNode>();
  private final Collection<WebflowEdge> myEdges = new HashSet<WebflowEdge>();
  protected final Map<PsiFile, NodesGroup> myGroups = new HashMap<PsiFile, NodesGroup>();

  private final Project myProject;
  private final XmlFile myFile;
  @NonNls private static final String UNDEFINED_NAME = "Undefined";

  public WebflowDataModel(XmlFile file) {
    myFile = file;
    myProject = file.getProject();
  }

  public Project getProject() {
    return myProject;
  }

  @NotNull
  public Collection<WebflowNode> getNodes() {
    return getNodes(true);
  }

  @NotNull
  public Collection<WebflowNode> getNodes(boolean refresh) {
    if (refresh) refreshDataModel();

    return myNodes;
  }

  @NotNull
  public Collection<WebflowEdge> getEdges() {
    return myEdges;
  }

  @NotNull
  public WebflowNode getSourceNode(WebflowEdge webflowEdge) {
    return webflowEdge.getSource();
  }

  @NotNull
  public WebflowNode getTargetNode(WebflowEdge webflowBasicEdge) {
    return webflowBasicEdge.getTarget();
  }

  @NotNull
  public String getNodeName(WebflowNode webflowBasicNode) {
    return "";
  }

  @NotNull
  public String getEdgeName(WebflowEdge webflowBasicEdge) {
    return webflowBasicEdge.getName();
  }

  /**
   * Creates {@link com.intellij.spring.webflow.graph.impl.WebflowTransitionEdge}
   *
   * @param from source node
   * @param to   target node
   * @return {@link com.intellij.spring.webflow.graph.impl.WebflowTransitionEdge} instance
   */
  public WebflowEdge createEdge(@NotNull WebflowNode from, @NotNull WebflowNode to) {
    DomElement element = from.getIdentifyingElement();
    if (!element.isValid()) return null;
    if (!isEdgeCreationAllowedForFile(from, to)) return null;

    if (element instanceof TransitionOwner) {
      return createTransition(from, to, element);
    }
    else if (element instanceof DecisionState) {
      return createIfTransition(from, to, element);
    }

    return null;
  }

  private static boolean isEdgeCreationAllowedForFile(WebflowNode from, WebflowNode to) {
    DomElement fromDomElement = from.getIdentifyingElement();
    DomElement toDomElement = to.getIdentifyingElement();

    if (fromDomElement.isValid() && toDomElement.isValid()) {
      if (DomUtil.getFile(fromDomElement).equals(DomUtil.getFile(toDomElement))) return true;

      Flow fromFlow = fromDomElement.getParentOfType(Flow.class, true);
      Flow toFlow = toDomElement.getParentOfType(Flow.class, true);

      return WebflowUtil.getAllParentFlows(fromFlow).contains(toFlow);

    }

    return false;
  }

  @Nullable
  private WebflowEdge createIfTransition(WebflowNode from, WebflowNode to, DomElement element) {
    DecisionState state = (DecisionState)element;

    List<If> ifs = state.getIfs();
    if (ifs.size() != 1) {
      return createIfThenTransition(from, to, state);
    }
    else {

      If anIf = ifs.get(0);

      if (!DomUtil.hasXml(anIf.getElse())) {
        int i = Messages.showDialog(WebflowBundle.message("messages.transition.type.question"), WebflowBundle.message("messages.transition.type.title"),
                                          new String[]{WebflowBundle.message("messages.transition.type.else"), WebflowBundle.message("messages.transition.type.new.if")}, 0, Messages.getQuestionIcon());
        if (i == 0) {
          createIfElseTransition(from, to, anIf);
        }
        else if (i == 1) {
          return createIfThenTransition(from, to, state);
        }
      }
    }
    return null;
  }

  private WebflowEdge createIfThenTransition(final WebflowNode from, final WebflowNode to, final DecisionState state) {
    WriteCommandAction<WebflowEdge> action = new WriteCommandAction<WebflowEdge>(myProject) {
      protected void run(Result<WebflowEdge> result) throws Throwable {
        String toName = to.getName();
        If ifTag = state.addIf();
        ifTag.getThen().setStringValue(toName);
        ifTag.getTest().ensureXmlElementExists();
        result.setResult(new WebflowIfEdge.Then(from, to, (If)ifTag.createStableCopy(), ifTag.getThen()));
      }
    };

    return action.execute().getResultObject();
  }

  private WebflowEdge createIfElseTransition(final WebflowNode from, final WebflowNode to, final If anIf) {
    WriteCommandAction<WebflowEdge> action = new WriteCommandAction<WebflowEdge>(myProject) {
      protected void run(Result<WebflowEdge> result) throws Throwable {
        String toName = to.getName();
        anIf.getElse().setStringValue(toName);
        anIf.getTest().ensureXmlElementExists();
        result.setResult(new WebflowIfEdge.Else(from, to, (If)anIf.createStableCopy(), anIf.getElse()));
      }
    };

    return action.execute().getResultObject();
  }

  private WebflowEdge createTransition(final WebflowNode from, final WebflowNode to, final DomElement element) {
    WriteCommandAction<WebflowEdge> action = new WriteCommandAction<WebflowEdge>(myProject) {
      protected void run(Result<WebflowEdge> result) throws Throwable {
        String toName = to.getName();
        Transition transition = ((TransitionOwner)element).addTransition();
        transition.getTo().setStringValue(toName);
        transition.getOn().ensureXmlElementExists();
        result.setResult(new WebflowTransitionEdge(from, to, (Transition)transition.createStableCopy()));
      }
    };

    return action.execute().getResultObject();
  }

  public void dispose() {
  }


  private void refreshDataModel() {
    clearAll();

    updateDataModel();
  }

  private void clearAll() {
    myNodes.clear();
    myEdges.clear();
  }

  public void updateDataModel() {
    Flow flow = getFlow();

    if (flow == null) return;

    Map<String, List<WebflowBasicNode>> nodes = new HashMap<String, List<WebflowBasicNode>>();

    addNodes(flow, nodes);

    for (Flow pFlow : WebflowUtil.getAllParentFlows(flow)) {
      addNodes(pFlow, nodes);
    }

    for (List<WebflowBasicNode> basicNodeList : nodes.values()) {
      for (WebflowBasicNode node : basicNodeList) {
        addTransitions(node, nodes);
      }
    }
  }

  private void addTransitions(WebflowBasicNode node, Map<String, List<WebflowBasicNode>> nodes) {
    DomElement identifiedElement = node.getIdentifyingElement();
    if (identifiedElement instanceof TransitionOwner) {
      TransitionOwner transitionOwner = (TransitionOwner)identifiedElement;
      for (Transition transition : transitionOwner.getTransitions()) {
        String key = transition.getTo().getStringValue();
        createEdge(node, nodes, getCreatetransitionEdgeFunction(node, transition), key);

        /*
        if (!StringUtil.isEmptyOrSpaces(key)) {
          List<WebflowBasicNode> targetList = nodes.get(key);
          if (targetList != null) {   // TODO: create error node if null list
            for (WebflowBasicNode target : targetList) {
              WebflowBasicEdge edge = new WebflowBasicEdge(node, target, transition);
              myEdges.add(edge);
            }
          }
        }
        */
      }
    }
    else if (identifiedElement instanceof DecisionState) {
      DecisionState state = (DecisionState)identifiedElement;
      for (If anIf : state.getIfs()) {
        String key = anIf.getThen().getStringValue();
        if (!StringUtil.isEmptyOrSpaces(key)) {
          createEdge(node, nodes, getCreateIfThenEdgeFunction(node, anIf, anIf.getThen()), key);
        }

        String elseKey = anIf.getElse().getStringValue();
        if (!StringUtil.isEmptyOrSpaces(elseKey)) {
          createEdge(node, nodes, getCreateIfElseEdgeFunction(node, anIf, anIf.getElse()), elseKey);
        }
      }
    }
  }

  private static Function<WebflowNode, WebflowEdge> getCreatetransitionEdgeFunction(final WebflowBasicNode node,
                                                                                    final Transition transition) {
    return new Function<WebflowNode, WebflowEdge>() {
      public WebflowEdge fun(WebflowNode targetNode) {
        return new WebflowTransitionEdge(node, targetNode, transition);
      }
    };
  }

  private static Function<WebflowNode, WebflowEdge> getCreateIfThenEdgeFunction(final WebflowBasicNode node,
                                                                                final If transition,
                                                                                final GenericAttributeValue<Object> value) {
    return new Function<WebflowNode, WebflowEdge>() {
      public WebflowEdge fun(WebflowNode targetNode) {
        return new WebflowIfEdge.Then(node, targetNode, transition, value);
      }
    };
  }

  private static Function<WebflowNode, WebflowEdge> getCreateIfElseEdgeFunction(final WebflowBasicNode node,
                                                                                final If transition,
                                                                                final GenericAttributeValue<Object> value) {
    return new Function<WebflowNode, WebflowEdge>() {
      public WebflowEdge fun(WebflowNode targetNode) {
        return new WebflowIfEdge.Else(node, targetNode, transition, value);
      }
    };
  }

  private void createEdge(WebflowBasicNode node,
                          Map<String, List<WebflowBasicNode>> nodes,
                          Function<WebflowNode, WebflowEdge> function,
                          String key) {
    if (!StringUtil.isEmptyOrSpaces(key)) {
      List<WebflowBasicNode> targetList = nodes.get(key);
      if (targetList != null) {   // TODO: create error node if null list
        for (WebflowBasicNode target : targetList) {
          WebflowEdge edge = function.fun(target);
          myEdges.add(edge);
        }
      }
    }
  }

  private void addNodes(Flow flow, Map<String, List<WebflowBasicNode>> nodes) {
    for (ActionState state : flow.getActionStates()) {
      String name = getNodeName(state);
      ActionStateNode node = new ActionStateNode(name, (ActionState)state.createStableCopy());
      addNewNode(nodes, name, node);
    }
    for (ViewState state : flow.getViewStates()) {
      String name = getNodeName(state);
      ViewStateNode node = new ViewStateNode(name, (ViewState)state.createStableCopy());
      addNewNode(nodes, name, node);
    }
    for (DecisionState state : flow.getDecisionStates()) {
      String name = getNodeName(state);
      DecisionStateNode node = new DecisionStateNode(name, (DecisionState)state.createStableCopy());
      addNewNode(nodes, name, node);
    }
    for (EndState state : flow.getEndStates()) {
      String name = getNodeName(state);
      EndStateNode node = new EndStateNode(name, (EndState)state.createStableCopy());
      addNewNode(nodes, name, node);
    }
    for (SubflowState state : flow.getSubflowStates()) {
      String name = getNodeName(state);
      SubflowStateNode node = new SubflowStateNode(name, (SubflowState)state.createStableCopy());
      addNewNode(nodes, name, node);
    }

    // add global transitions  (IDEADEV-26294)
    GlobalTransitions globalTransitions = flow.getGlobalTransitions();
    if (globalTransitions.getTransitions().size() > 0) {
      addNewNode(nodes, GlobalTransitionsNode.GLOBAL_TRANSITIONS_NODE_NAME, new GlobalTransitionsNode(globalTransitions));
    }
  }

  private void addNewNode(Map<String, List<WebflowBasicNode>> nodes, String name, WebflowBasicNode node) {
    List<WebflowBasicNode> namedNodes = (!nodes.containsKey(name)) ? new ArrayList<WebflowBasicNode>() : nodes.get(name);
    namedNodes.add(node);
    nodes.put(name, namedNodes);
    myNodes.add(node);

  }

  @NotNull
  private static String getNodeName(Identified identified) {
    String name = identified.getId().getStringValue();
    if (StringUtil.isEmptyOrSpaces(name)) {
      name = UNDEFINED_NAME;
    }
    else {
    }

    return name;
  }

  @Nullable
  public Flow getFlow() {
    WebflowModel model = getModel();
    if (model == null || model.getRoots().size() != 1) return null;

    return model.getRoots().get(0).getRootElement();
  }

  public WebflowModel getModel() {
    return WebflowDomModelManager.getInstance(myProject).getWebflowModel(myFile);
  }
}
