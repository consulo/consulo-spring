package com.intellij.spring.webflow.graph.dnd;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.graph.builder.dnd.GraphDnDSupport;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.spring.webflow.WebflowIcons;
import com.intellij.spring.webflow.graph.WebflowDataModel;
import com.intellij.spring.webflow.graph.WebflowNode;
import com.intellij.spring.webflow.graph.WebflowNodeType;
import com.intellij.spring.webflow.graph.impl.*;
import com.intellij.spring.webflow.model.xml.*;
import java.util.function.Function;
import com.intellij.util.xml.DomUtil;
import com.sun.tools.jdi.LinkedHashMap;

import javax.swing.*;
import java.util.Map;

/**
 * User: Sergey.Vasiliev
 */
public class WebflowDnDSupport implements GraphDnDSupport<WebflowNode, WebflowNodeType> {
  private final WebflowDataModel myDataModel;
  private static final String UNKNOWN = "unknown";

  public WebflowDnDSupport(final WebflowDataModel dataModel) {
    myDataModel = dataModel;
  }


  public Map<WebflowNodeType, Pair<String, Icon>> getDnDActions() {
    LinkedHashMap nodes = new LinkedHashMap();

    nodes.put(WebflowNodeType.ACTION_STATE, new Pair<String, Icon>("Action State", WebflowIcons.WEBFLOW_ACTION_STATE));
    nodes.put(WebflowNodeType.VIEW_STATE, new Pair<String, Icon>("View State", WebflowIcons.WEBFLOW_VIEW_STATE));
    nodes.put(WebflowNodeType.DECISION_STATE, new Pair<String, Icon>("Desicion State", WebflowIcons.WEBFLOW_DECISION_STATE));
    nodes.put(WebflowNodeType.SUBFLOW_STATE, new Pair<String, Icon>("Subflow State", WebflowIcons.WEBFLOW_SUBFLOW_STATE));
    nodes.put(WebflowNodeType.END_STATE, new Pair<String, Icon>("End State", WebflowIcons.WEBFLOW_END_STATE));

    return nodes;
  }

  public boolean canStartDragging(final WebflowNodeType WebflowNodeType) {
    //final WebflowDefinition WebflowDefinition = getDataModel().getWebflowDefinition();
    //if (WebflowDefinition != null) {
    //  if (WebflowNodeType == WebflowNodeType.START_STATE) {
    //    return WebflowDefinition.getStartState().getXmlElement() == null;
    //  } else if (WebflowNodeType == WebflowNodeType.START_Web) {
    //    return WebflowDefinition.getStartWeb().getXmlElement() == null;
    //  }
    //}
    return true;
  }

  public WebflowNode drop(final WebflowNodeType webflowNodeType) {
    Flow flow = getDataModel().getFlow();
    switch (webflowNodeType) {
      case ACTION_STATE:
        return startInWCA(getDataModel().getProject(), flow, getDropActionStateFunction());
      case DECISION_STATE:
        return startInWCA(getDataModel().getProject(), flow, getDropDecisionStateFunction());
      case END_STATE:
        return startInWCA(getDataModel().getProject(), flow, getDropEndStateFunction());
      case SUBFLOW_STATE:
        return startInWCA(getDataModel().getProject(), flow, getDropSubflowStateFunction());
      case VIEW_STATE:
        return startInWCA(getDataModel().getProject(), flow, getDropViewStateFunction());
      default:
        break;
    }
    return null;
  }

  private static Function<Flow, WebflowNode> getDropActionStateFunction() {
    return new Function<Flow, WebflowNode>() {
      public WebflowNode fun(final Flow flow) {
        final ActionState actionState = flow.addActionState();
        //actionState.getName().setStringValue(UNKNOWN);
        return new ActionStateNode(UNKNOWN, (ActionState)actionState.createStableCopy());
      }
    };
  }

  private static Function<Flow, WebflowNode> getDropDecisionStateFunction() {
    return new Function<Flow, WebflowNode>() {
      public WebflowNode fun(final Flow flow) {
        final DecisionState state = flow.addDecisionState();
        return new DecisionStateNode(UNKNOWN, (DecisionState)state.createStableCopy());
      }
    };
  }

  private static Function<Flow, WebflowNode> getDropEndStateFunction() {
    return new Function<Flow, WebflowNode>() {
      public WebflowNode fun(final Flow flow) {
        final EndState state = flow.addEndState();
        return new EndStateNode(UNKNOWN, (EndState)state.createStableCopy());
      }
    };
  }

  private static Function<Flow, WebflowNode> getDropSubflowStateFunction() {
    return new Function<Flow, WebflowNode>() {
      public WebflowNode fun(final Flow flow) {
        final SubflowState state = flow.addSubflowState();
        return new SubflowStateNode(UNKNOWN, (SubflowState)state.createStableCopy());
      }
    };
  }

  private static Function<Flow, WebflowNode> getDropViewStateFunction() {
    return new Function<Flow, WebflowNode>() {
      public WebflowNode fun(final Flow flow) {
        final ViewState state = flow.addViewState();
        return new ViewStateNode(UNKNOWN, (ViewState)state.createStableCopy());
      }
    };
  }

  private static WebflowNode startInWCA(final Project project,
                                         final Flow flow,
                                         final Function<Flow, WebflowNode> function) {
    return new WriteCommandAction<WebflowNode>(project, DomUtil.getFile(flow)) {
      protected void run(final Result<WebflowNode> result) throws Throwable {
        result.setResult(function.fun(flow));
      }
    }.execute().getResultObject();
  }

  public WebflowDataModel getDataModel() {
    return myDataModel;
  }
  //
  //
  //public List<String> getExistedNodesNames() {
  //  List<String> names = new ArrayList<String>();
  //  for (WebflowNode node : myDataModel.getNodes(false)) {
  //    final String s = node.getName();
  //    if (!StringUtil.isEmptyOrSpaces(s)) {
  //      names.add(s);
  //    }
  //  }
  //  return names;
  //}
}
