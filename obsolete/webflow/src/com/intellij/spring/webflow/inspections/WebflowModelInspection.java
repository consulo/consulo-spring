package com.intellij.spring.webflow.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.webflow.constants.WebflowConstants;
import com.intellij.spring.webflow.model.xml.*;
import com.intellij.spring.webflow.resources.messages.WebflowBundle;
import com.intellij.spring.webflow.util.WebflowUtil;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WebflowModelInspection extends BasicDomElementsInspection<Flow> {

  public WebflowModelInspection() {
    super(Flow.class);
  }

  public void checkFileElement(final DomFileElement<Flow> flowDomFileElement, final DomElementAnnotationHolder holder) {
    super.checkFileElement(flowDomFileElement, holder);

    final Flow flow = flowDomFileElement.getRootElement();

    if (!flow.isValid()) return;

    for (WebflowNamedAction action : getAllActions(flow)) {
      checkAction(action, holder);
    }

    for (SubflowState state : flow.getSubflowStates()) {
      WebflowUtil.checkBeanOfSpecificType(state.getAttributeMapper().getBean(), WebflowConstants.FLOW_ATTRIBUTE_MAPPER_CLASSNAME, holder);
    }

    for (Var var : flow.getVars()) {
      checkFlowVar(var, holder);
    }

    for (TransitionOwner owner : getAllTransitionOwner(flow)) {
      checkTransitions(owner, holder);
    }
    checkExceptionHandlerBeanClass(flow, holder);
  }

  private static void checkTransitions(final TransitionOwner owner, final DomElementAnnotationHolder holder) {
    for (Transition transition : owner.getTransitions()) {
      if (DomUtil.hasXml(transition.getOn()) && DomUtil.hasXml(transition.getOnException())) {
        holder.createProblem(transition, WebflowBundle.message("transition.on.and.onexception.inconsistency"));
      }
    }
  }

  private static void checkFlowVar(final Var var, final DomElementAnnotationHolder holder) {
    final SpringBeanPointer pointer = var.getBean().getValue();
    if (pointer != null) {
      final CommonSpringBean bean = pointer.getSpringBean();
      if (bean.isValid() && !WebflowUtil.isNonSingletonPrototype(bean)) {
        holder.createProblem(var.getBean(), WebflowBundle.message("var.bean.must.be.non.singleton.prototype"));
      }
    }

    if (DomUtil.hasXml(var.getBean()) && DomUtil.hasXml(var.getClazz())) {
      holder.createProblem(var, WebflowBundle.message("var.class.and.bean.attributes.inconsistency"));   //IDEADEV-26335
    }

  }

  private static void checkAction(final WebflowNamedAction action, final DomElementAnnotationHolder holder) {
    if (action instanceof Action) {
      checkActionBeanClass(action.getBean(), holder);
      checkActionMethodSignature(action.getMethod(), holder);
      checkMultiActionInconsistency((Action)action, holder);   // IDEADEV-26890
    }
    checkActionMethodIsPublic(action.getMethod(), holder);
  }

  private static void checkMultiActionInconsistency(final Action action, final DomElementAnnotationHolder holder) {
    if (isMultiActionBean(action.getBean())) {
      final PsiMethod method = action.getMethod().getValue();
      if (method == null) {
        final ActionState actionState = action.getParentOfType(ActionState.class, false);
        if (actionState != null) {
          final String defaultMethodName = actionState.getId().getValue();
          if (defaultMethodName != null) {
            final PsiClass beanClass = WebflowUtil.getBeanClass(action.getBean());

            final PsiMethod actionMethod = findMultiActionMethod(beanClass, defaultMethodName);
            if (actionMethod == null) {
              holder.createProblem(action, HighlightSeverity.ERROR, WebflowBundle.message("multi.action.bean.method.not.specified"),
                                   getMultiActionBeanMethodQuickFixes(action, beanClass));
            }
          }
        }
      }
    }
  }

  private static LocalQuickFix[] getMultiActionBeanMethodQuickFixes(final Action action, final PsiClass beanClass) {
    LocalQuickFix addMethodAttrQuickFix = new LocalQuickFix() {
      @NotNull
      public String getName() {
        return WebflowBundle.message("multi.action.bean.specify.method.quick.fix");
      }

      @NotNull
      public String getFamilyName() {
        return WebflowBundle.message("webflow.model.bean.quickfix.family");
      }

      public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
        action.getMethod().ensureXmlElementExists();

        final XmlElement xmlElement = action.getXmlElement();
        final XmlAttributeValue attributeValue = action.getMethod().getXmlAttributeValue();
        if (xmlElement != null && attributeValue != null) {
          final VirtualFile file = xmlElement.getContainingFile().getVirtualFile();
          if (file != null) {
            new OpenFileDescriptor(project, file, attributeValue.getTextRange().getStartOffset() + 1).navigate(true);
          }
        }
      }
    };

    return new LocalQuickFix[]{addMethodAttrQuickFix};
  }

  @Nullable
  private static PsiMethod findMultiActionMethod(final PsiClass beanClass, final String methodName) {
    if (beanClass != null) {
      for (PsiMethod method : beanClass.getAllMethods()) {
        // public Event ${methodName}(RequestContext context) throws Exception;
        if (method.getName().equals(methodName) && method.hasModifierProperty(PsiModifier.PUBLIC) &&
            method.getParameterList().getParametersCount() == 1 &&
            isAssignable(WebflowConstants.ACTION_BEAN_METHOD_PARAMETER_CLASSNAME, method.getParameterList().getParameters()[0].getType(),
                         method.getProject()) &&
                                              isAssignable(WebflowConstants.ACTION_BEAN_METHOD_RETURN_TYPE_CLASSNAME,
                                                           method.getReturnType(), method.getProject())) {
          return method;
        }
      }
    }
    return null;
  }

  private static boolean isMultiActionBean(@Nullable final GenericAttributeValue<SpringBeanPointer> value) {
    return WebflowUtil.isBeanOfSpecificType(value, WebflowConstants.MULTI_ACTION_BEAN_CLASSNAME);
  }

  private static void checkActionBeanClass(final GenericAttributeValue<SpringBeanPointer> bean, final DomElementAnnotationHolder holder) {
    WebflowUtil.checkBeanOfSpecificType(bean, WebflowConstants.ACTION_BEAN_CLASSNAME, holder);
  }

  private static void checkActionMethodSignature(final GenericAttributeValue<PsiMethod> methodValue,
                                                 final DomElementAnnotationHolder holder) {
    final PsiMethod method = methodValue.getValue();
    if (method != null) {
      if (method.getParameterList().getParameters().length != 1 ||
          !isAssignable(WebflowConstants.ACTION_BEAN_METHOD_PARAMETER_CLASSNAME, method.getParameterList().getParameters()[0].getType(),
                        method.getProject())) {
        holder.createProblem(methodValue, WebflowBundle.message("action.bean.method.parameter.type"));
      }
      final PsiType type = method.getReturnType();
      if (type == null || !isAssignable(WebflowConstants.ACTION_BEAN_METHOD_RETURN_TYPE_CLASSNAME, type, method.getProject())) {
        holder.createProblem(methodValue, WebflowBundle.message("action.bean.method.return.type"));

      }
    }
  }

  private static boolean isAssignable(final String className, final PsiType type, final Project project) {
    if (type == null) return false;
    if (type.getCanonicalText().equals(className)) return true;

    final PsiClass psiClass = WebflowUtil.getClassByQualifiedName(className, project);

    return psiClass != null && type.isAssignableFrom(JavaPsiFacade.getInstance(project).getElementFactory().createType(psiClass));
  }

  private static void checkActionMethodIsPublic(final GenericAttributeValue<PsiMethod> methodValue,
                                                final DomElementAnnotationHolder holder) {
    final PsiMethod method = methodValue.getValue();
    if (method != null) {
      if (!method.hasModifierProperty(PsiModifier.PUBLIC)) {
        holder.createProblem(methodValue, WebflowBundle.message("action.bean.method.must.be.public"));
      }
      if (method.isConstructor()) {
        holder.createProblem(methodValue, WebflowBundle.message("action.bean.method.cannot.be.constructor"));
      }
    }
  }


  @NotNull
  private static List<WebflowNamedAction> getAllActions(final Flow flow) {
    List<WebflowNamedAction> actions = new ArrayList<WebflowNamedAction>();
    if (flow.isValid()) {
      for (ActionState state : flow.getActionStates()) {
        WebflowUtil.collectActons(state, actions);
        WebflowUtil.collectActons(state.getEntryActions(), actions);
        WebflowUtil.collectActons(state.getExitActions(), actions);
      }

      for (ViewState state : flow.getViewStates()) {
        WebflowUtil.collectActons(state.getEntryActions(), actions);
        WebflowUtil.collectActons(state.getExitActions(), actions);
        WebflowUtil.collectActons(state.getRenderActions(), actions);
      }

      for (SubflowState state : flow.getSubflowStates()) {
        WebflowUtil.collectActons(state.getEntryActions(), actions);
        WebflowUtil.collectActons(state.getExitActions(), actions);
      }

      for (DecisionState state : flow.getDecisionStates()) {
        WebflowUtil.collectActons(state.getEntryActions(), actions);
        WebflowUtil.collectActons(state.getExitActions(), actions);
      }

      for (EndState state : flow.getEndStates()) {
        WebflowUtil.collectActons(state.getEntryActions(), actions);
      }

      WebflowUtil.collectActons(flow.getEndActions());
      WebflowUtil.collectActons(flow.getStartActions());
    }
    return actions;
  }

  @NotNull
  private static List<TransitionOwner> getAllTransitionOwner(final Flow flow) {
    List<TransitionOwner> owners = new ArrayList<TransitionOwner>();
    if (flow.isValid()) {
      owners.addAll(flow.getActionStates());
      owners.addAll(flow.getViewStates());
      owners.addAll(flow.getSubflowStates());
      owners.add(flow.getGlobalTransitions());
    }
    return owners;
  }

  protected static List<WebflowNamedAction> collectActons(ActionsOwner owner, @NotNull List<WebflowNamedAction> actions) {
    if (owner.isValid()) {
      actions.addAll(owner.getActions());
      actions.addAll(owner.getBeanActions());
    }

    return actions;
  }

  private static void checkExceptionHandlerBeanClass(final Flow flow, final DomElementAnnotationHolder holder) {

    for (ExceptionHandler exceptionHandler : flow.getExceptionHandlers()) {
      WebflowUtil.checkBeanOfSpecificType(exceptionHandler.getBean(), WebflowConstants.FLOW_EXECUTION_HANDLER_CLASSNAME, holder);
    }
  }


  @NotNull
  public String getGroupDisplayName() {
    return WebflowBundle.message("model.inspection.group.name");
  }

  @NotNull
  public String getDisplayName() {
    return WebflowBundle.message("model.inspection.display.name");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "WebflowModelInspection";
  }
}