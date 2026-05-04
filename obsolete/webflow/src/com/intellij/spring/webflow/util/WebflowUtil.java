package com.intellij.spring.webflow.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBeanScope;
import com.intellij.spring.webflow.config.model.xml.version2_0.FlowLocation;
import com.intellij.spring.webflow.config.model.xml.version2_0.FlowRegistry;
import com.intellij.spring.webflow.graph.WebflowNode;
import com.intellij.spring.webflow.model.xml.*;
import com.intellij.spring.webflow.resources.messages.WebflowBundle;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Set;

/**
 * User: plt
 */
public class WebflowUtil {
  public static String WEBFLOW_EL_PREFIX = "${";
  public static String WEBFLOW_EL_SUFFIX = "}";

  private WebflowUtil() {
  }

  public static List<Identified> getAllIdentified(@Nullable final Flow flow) {
    if (flow == null) return Collections.emptyList();

    return getAllIdentified(flow, true);
  }

  public static List<Identified> getAllIdentified(final Flow flow, boolean withParent) {
    List<Identified> identifiedList = new ArrayList<Identified>();

    addIdentified(flow, withParent, identifiedList);

    return identifiedList;
  }

  private static void addIdentified(final Flow flow, final boolean withParent, final List<Identified> identifiedList) {
    addIdentified(flow, identifiedList);

    if (withParent) {
      for (Flow parentFlow : getAllParentFlows(flow)) {
        addIdentified(parentFlow, identifiedList);
      }
    }
  }

  private static void addIdentified(final Flow flow, final List<Identified> identifiedList) {
    identifiedList.addAll(flow.getActionStates());
    identifiedList.addAll(flow.getDecisionStates());
    identifiedList.addAll(flow.getEndStates());
    identifiedList.addAll(flow.getSubflowStates());
    identifiedList.addAll(flow.getViewStates());
  }

  public static List<Flow> getAllParentFlows(final Flow flow) {
    final List<Flow> list = new ArrayList<Flow>();

    addParents(flow, new ArrayList<Flow>(), list);

    return list;
  }

  private static void addParents(final Flow flow, final List<Flow> visited, final List<Flow> list) {
    visited.add(flow);

    final List<Flow> parents = flow.getParentFlow().getValue();
    if (parents != null) {
      for (Flow parentFlow : parents) {
        if (!visited.contains(parentFlow)) {
          list.add(parentFlow);
          addParents(parentFlow, visited, list);
        }
      }
    }
  }

  @Nullable
  public static Flow findFlowByName(@Nullable final String name, @Nullable final Module module) {
    if (name == null || module == null) return null;

    final List<WebflowModel> models = WebflowDomModelManager.getInstance(module.getProject()).getAllModels(module);
    Set<PsiFile> configuredFiles = new HashSet<PsiFile>();

    final WebflowDomModelManager manager = WebflowDomModelManager.getInstance(module.getProject());
    for (FlowLocation flowLocation : getFlowLocations(module)) {
      final PsiFile psiFile = flowLocation.getPath().getValue();
      if (psiFile instanceof XmlFile && manager.isWebflow((XmlFile)psiFile)) {
        final String s = flowLocation.getId().getStringValue();
        if (name.equals(s)) {
          final WebflowModel model = manager.getWebflowModel((XmlFile)psiFile);
          if (model != null) {
            return model.getFlow();
          }
        }
        if (!StringUtil.isEmptyOrSpaces(flowLocation.getId().getStringValue())) {
          configuredFiles.add(psiFile);
        }
      }
    }

    for (WebflowModel model : models) {
      for (XmlFile xmlFile : model.getConfigFiles()) {
        if (configuredFiles.contains(xmlFile)) continue;

        final VirtualFile file = xmlFile.getVirtualFile();
        if (file != null && name.equals(file.getNameWithoutExtension())) {
          return model.getFlow();
        }
      }
    }

    return null;
  }

  private static Set<FlowLocation> getFlowLocations(@Nullable final Module module) {
    if (module != null) {
      final PsiClass flowRegistryClass = JavaPsiFacade.getInstance(module.getProject())
        .findClass(FlowRegistry.FLOW_REGISTRY_CLASS, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false));
      if (flowRegistryClass != null) {
        Set<FlowLocation> locations = new HashSet<FlowLocation>();
        for (SpringModel model : SpringManager.getInstance(module.getProject()).getAllModels(module)) {
          for (SpringBaseBeanPointer beanPointer : model.findBeansByPsiClassWithInheritance(flowRegistryClass)) {
            final CommonSpringBean springBean = beanPointer.getSpringBean();

            if (springBean instanceof FlowRegistry) {
              locations.addAll(((FlowRegistry)springBean).getFlowLocations());
            }
          }

        }
        return locations;
      }
    }
    return Collections.emptySet();
  }


  public static List<Flow> getAllFlows(final Module module, List<PsiFile> exceptedFiles) {
    List<Flow> flows = new ArrayList<Flow>();

    for (WebflowModel model : WebflowDomModelManager.getInstance(module.getProject()).getAllModels(module)) {
      if (!exceptedFiles.contains(DomUtil.getFile(model.getFlow()))) flows.add(model.getFlow());
    }
    return flows;
  }

  @Nullable
  public static PsiElement resolveFlow(@Nullable final Flow flow, @Nullable Module module) {
    if (flow == null) return null;
    if (module == null) module = flow.getModule();

    final XmlFile xmlFile = DomUtil.getFile(flow);

    for (FlowLocation flowLocation : getFlowLocations(module)) {
      final PsiFile psiFile = flowLocation.getPath().getValue();
      if (xmlFile.equals(psiFile)) {
        final String id = flowLocation.getId().getStringValue();
        if (!StringUtil.isEmptyOrSpaces(id)) {
          return flowLocation.getXmlElement();
        }
      }
    }

    return DomUtil.getFile(flow);
  }

  @Nullable
  public static String getFlowName(@Nullable final Flow flow) {
    if (flow == null) return null;

    return getFlowName(flow, flow.getModule());
  }

  @Nullable
  public static String getFlowName(@Nullable final Flow flow, @Nullable Module module) {
    if (flow == null) return null;

    final PsiElement element = resolveFlow(flow, module);
    if (element instanceof XmlFile) {
      final VirtualFile file = ((XmlFile)element).getVirtualFile();
      return file != null ? file.getNameWithoutExtension() : null;
    }
    else if (element instanceof XmlTag) {
      final DomElement domElement = flow.getManager().getDomElement((XmlTag)element);
      return domElement instanceof FlowLocation ? ((FlowLocation)domElement).getId().getStringValue() : null;
    }

    return null;
  }

  public static void checkBeanOfSpecificType(final GenericAttributeValue<SpringBeanPointer> bean,
                                             final String className,
                                             final DomElementAnnotationHolder holder) {
    final PsiClass beanClass = getBeanClass(bean);
    if (beanClass != null) {
      final PsiClass psiClass = getClassByQualifiedName(className, beanClass.getProject());
      if (psiClass != null && !InheritanceUtil.isInheritorOrSelf(beanClass, psiClass, true)) {
        holder.createProblem(bean, WebflowBundle.message("incorrect.action.bean.type", className));
      }
    }
  }

  @Nullable
  public static PsiClass getClassByQualifiedName(final String className, final Project project) {
    return JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
  }

  @Nullable
  public static PsiClass getBeanClass(final GenericAttributeValue<SpringBeanPointer> bean) {
    final SpringBeanPointer springBeanPointer = bean.getValue();
    if (springBeanPointer != null) {
      return springBeanPointer.getBeanClass();
    }
    return null;
  }


  public static boolean isBeanOfSpecificType(final GenericAttributeValue<SpringBeanPointer> bean, final String className) {
    final PsiClass beanClass = getBeanClass(bean);
    if (beanClass != null) {
      final PsiClass psiClass = getClassByQualifiedName(className, beanClass.getProject());

      return psiClass != null && InheritanceUtil.isInheritorOrSelf(beanClass, psiClass, true);
    }
    return false;
  }

  public static List<WebflowNamedAction> collectActons(ActionsOwner owner) {
    return collectActons(owner, new ArrayList<WebflowNamedAction>());
  }

  public static List<WebflowNamedAction> collectActons(ActionsOwner owner, @NotNull List<WebflowNamedAction> actions) {
    if (owner.isValid()) {
      actions.addAll(owner.getActions());
      actions.addAll(owner.getBeanActions());
    }

    return actions;
  }

  public static boolean isStartState(WebflowNode webflowNode) {
    final DomElement element = webflowNode.getIdentifyingElement();
    if (element instanceof Identified && element.isValid()) {
      final Flow parentOfType = element.getParentOfType(Flow.class, false);
      if (parentOfType != null) {
        final Object idref = parentOfType.getStartState().getIdref().getValue();
        if (idref instanceof Identified) {
          final String value = ((Identified)element).getId().getStringValue();
          if (value != null && value.equals(((Identified)idref).getId().getStringValue())) return true;
        }
      }
    }
    return false;
  }

  public static boolean isAction(final ConvertContext context) {
    return null != context.getInvocationElement().getParentOfType(Action.class, false);
  }

  public static boolean isNonSingletonPrototype(final CommonSpringBean bean) {
    if (bean instanceof SpringBean) {
      final SpringBean springBean = (SpringBean)bean;
      final Boolean isSingleton = springBean.getSingleton().getValue();
      if (isSingleton != null && isSingleton.booleanValue()) return false;

      final SpringBeanScope scope = springBean.getScope().getValue();
      return scope != null && scope == SpringBeanScope.PROROTYPE_SCOPE;
    }

    return false;
  }
}
