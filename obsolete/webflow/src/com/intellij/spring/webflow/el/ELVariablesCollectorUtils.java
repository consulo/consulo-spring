package com.intellij.spring.webflow.el;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Factory;
import com.intellij.openapi.util.Pair;
import consulo.util.lang.ref.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.impl.source.jsp.JspImplicitVariableImpl;
import com.intellij.psi.impl.source.jsp.el.impl.ELResolveUtil;
import com.intellij.psi.jsp.JspImplicitVariable;
import com.intellij.psi.jsp.el.ELExpressionHolder;
import com.intellij.psi.jsp.el.ELSelectExpression;
import com.intellij.psi.jsp.el.ELVariable;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.el.SpringBeansAsJsfVariableUtil;
import com.intellij.spring.webflow.model.xml.*;
import com.intellij.spring.webflow.util.WebflowUtil;
import com.intellij.util.Processor;
import java.util.HashMap;
import com.intellij.util.xml.*;
import com.intellij.pom.references.PomService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ELVariablesCollectorUtils {

  private static final List<Pair<String, String>> predefinedVars = new ArrayList<Pair<String, String>>();

  static {
    predefinedVars.add(new Pair<String, String>("currentUser", "java.security.Principal"));
    predefinedVars.add(new Pair<String, String>("persistenceContext", "javax.persistence.EntityManager"));
    predefinedVars.add(new Pair<String, String>("currentEvent", "org.springframework.webflow.execution.Event"));
    predefinedVars.add(new Pair<String, String>("messageContext", "org.springframework.binding.message.MessageContext"));
    predefinedVars.add(new Pair<String, String>("resourceBundle", "java.lang.Object"));
    predefinedVars.add(new Pair<String, String>("flowRequestContext", "org.springframework.webflow.execution.RequestContext"));
    predefinedVars.add(new Pair<String, String>("flowExecutionContext", "org.springframework.webflow.execution.FlowExecutionContext"));
    predefinedVars.add(new Pair<String, String>("flowExecutionUrl", "java.lang.Object"));
    predefinedVars.add(new Pair<String, String>("externalContext", "org.springframework.webflow.context.ExternalContext"));
  }

  private ELVariablesCollectorUtils() {
  }

  public static List<JspImplicitVariable> getImplicitVariables(PsiElement host) {
    List<JspImplicitVariable> resultVars = new ArrayList<JspImplicitVariable>();
    PsiFile file = host.getContainingFile();
    if (file instanceof XmlFile) {
      WebflowModel webflowModel = WebflowDomModelManager.getInstance(host.getProject()).getWebflowModel((XmlFile)file);

      if (webflowModel != null) {
        ProcessingContext context = createContext(webflowModel, host, resultVars);
        addApplicationContextVariables(context);
        processWebflowContextVariables(context);
      }
    }

    return resultVars;
  }

  private static ProcessingContext createContext(WebflowModel webflowModel,
                                                 PsiElement host,
                                                 List<JspImplicitVariable> resultVars) {
    return new ProcessingContext(webflowModel, host, resultVars);
  }

  private static void addApplicationContextVariables(ProcessingContext context) {
    for (SpringModel model : SpringManager.getInstance(context.getProject()).getAllModels(context.getModule())) {
      SpringBeansAsJsfVariableUtil.addVariables(context.getResultVars(), model);
    }
  }

  private static void addApplicationContextVariables(ProcessingContext context, Flow flow) {
    SpringManager springManager = SpringManager.getInstance(context.getProject());

    for (BeanImport beanImport : flow.getBeanImports()) {
      XmlFile xmlFile = beanImport.getResource().getValue();
      if (xmlFile != null) {
        SpringModel springModel = springManager.getLocalSpringModel(xmlFile);
        if (springModel != null) {
          SpringBeansAsJsfVariableUtil.addVariables(context.getResultVars(), springModel);
        }
      }
    }
  }

  private static void processWebflowContextVariables(ProcessingContext context) {
    collectScopeVariables(context);
    collectSettedVariables(context);

    collectInputVariables(context);
    collectVars(context);
    collectPredefinedVariables(context);

    processScopeVariables(context);
  }

  private static void processScopeVariables(final ProcessingContext context) {
    final Map<WebflowScope, List<JspImplicitVariable>> scopeVars = context.getScopeVariablesMap();
    for (final WebflowScope webflowScope : scopeVars.keySet()) {
      context.getResultVars().add(new WebflowScopeImplicitVariable(webflowScope, new FakePsiElement() {
        public PsiElement getParent() {
          return context.getFile();
        }
      }, new Factory<List<JspImplicitVariable>>() {
        public List<JspImplicitVariable> create() {
          return scopeVars.get(webflowScope);
        }
      }));
    }
  }

  private static void collectScopeVariables(ProcessingContext context) {
    PsiType objectClassType = getObjectClassType(context.getProject());

    List<Evaluate> evaluates = collectEvaluates(context.getWebflowModel());

    for (WebflowScopeProvider scopeProvider : context.getAcceptedProviders()) {
      for (Evaluate evaluate : evaluates) {
        String varName = getVariableName(scopeProvider.getScope(), evaluate.getResult().getStringValue());
        if (!StringUtil.isEmptyOrSpaces(varName) &&  isInProviderScope(scopeProvider, evaluate, context)) {

          PsiElement element =
              scopeProvider.getOrCreateScopeVariable(context.getFile(), varName, evaluate.getResult().getXmlAttribute());
          if (element != null) {
            addScopeVariable(context, scopeProvider.getScope(),
                             createVariable(context.getFile(), objectClassType, evaluate.getExpression(), evaluate.getResultType(), varName,
                                            element));
          }
        }
      }
    }
  }

  private static void collectSettedVariables(ProcessingContext context) {
    PsiType objectClassType = getObjectClassType(context.getProject());

    List<Set> sets = collectSetters(context.getWebflowModel());
    for (WebflowScopeProvider scopeProvider : context.getAcceptedProviders()) {
      for (Set set : sets) {
        String varName = getVariableName(scopeProvider.getScope(), set.getName().getStringValue());
        if (!StringUtil.isEmptyOrSpaces(varName) && isInProviderScope(scopeProvider, set, context)) {
          PsiElement element =
              scopeProvider.getOrCreateScopeVariable(context.getFile(), varName, set.getName().getXmlAttributeValue());
          if (element != null) {
            addScopeVariable(context,  scopeProvider.getScope(),
                             createVariable(context.getFile(), objectClassType, set.getValue(), set.getType(), varName, element));
          }
        }
      }
    }
  }

  private static boolean isInProviderScope(WebflowScopeProvider scopeProvider, DomElement domElement, ProcessingContext context) {
    Set<DomElement> scopes = scopeProvider.getScopes(context.getDomElement());
    for (DomElement scope : scopes) {
      if (isParent(scope,  domElement)) return true;
    }
    return false;
  }

  private static boolean isParent(DomElement potentialParent, DomElement domElement) {
    DomElement currParent = domElement;
    while (currParent != null) {
      if (currParent.equals(potentialParent)) return true;

      currParent = currParent.getParent();
    }
    return false;
  }

  public static void collectInputVariables(final ProcessingContext context) {
    final PsiType objectClassType = getObjectClassType(context.getProject());
    Flow flow = context.getWebflowModel().getFlow();

    processFlowVariables(flow, new Processor<Flow>() {
      public boolean process(Flow flow) {
        for (Input input : flow.getInputs()) {
          String varName = input.getName().getStringValue();
          if (!StringUtil.isEmptyOrSpaces(varName)) {
            PsiType psiType = input.getType().getValue();
            if (psiType == null) psiType = objectClassType;

            context.getResultVars().add(createImplicitVariable(input, varName, psiType, context.getFile()));
          }
        }
        return true;
      }
    });

  }

  private static void processFlowVariables(Flow flow, Processor<Flow> processor) {
    processor.process(flow);

    for (Flow parentFlow : WebflowUtil.getAllParentFlows(flow)) {
      processor.process(parentFlow);
    }
  }

  public static void collectVars(ProcessingContext context) {
    PsiType objectClassType = getObjectClassType(context.getProject());

    for (Var var : getVars(context)) {
      String varName = var.getName().getStringValue();
      if (!StringUtil.isEmptyOrSpaces(varName)) {
        PsiType psiType = var.getClazz().getValue();
        if (psiType == null) psiType = objectClassType;

        addScopeVariable(context, WebflowScope.FLOW, createImplicitVariable(var, varName, psiType, context.getFile()));
      }
    }
  }

  private static List<Var> getVars(ProcessingContext context) {
    List<Var> vars = new ArrayList<Var>();
    Flow flow = context.getWebflowModel().getFlow();
    vars.addAll(flow.getVars());

    DomElement domElement = context.getDomElement();
    if (domElement != null) {
      ViewState state = domElement.getParentOfType(ViewState.class, false);
      if (state != null) {
        vars.addAll(state.getVars());
      }
    }

    for (Flow parentFlow : WebflowUtil.getAllParentFlows(flow)) {
      vars.addAll(parentFlow.getVars());
    }

    return vars;
  }

  private static void addScopeVariable(ProcessingContext context,
                                       WebflowScope webflowScope,
                                       JspImplicitVariableImpl implicitVariable) {
    context.getResultVars().add(implicitVariable);
    Map<WebflowScope, List<JspImplicitVariable>> scopeVars = context.getScopeVariablesMap();

    if (!scopeVars.containsKey(webflowScope)) scopeVars.put(webflowScope, new ArrayList<JspImplicitVariable>());

    scopeVars.get(webflowScope).add(implicitVariable);
  }


  private static JspImplicitVariableImpl createVariable(final PsiFile file,
                                                        final PsiType objectClassType,
                                                        final GenericAttributeValue<String> expressionAttr,
                                                        final GenericAttributeValue<PsiType> psiTypeAttr,

                                                        final String varName,
                                                        final PsiElement element) {
    return new JspImplicitVariableImpl(file, varName, objectClassType, element, JspImplicitVariableImpl.NESTED_RANGE) {
      private PsiType ourFlowScopeResultType;

      @NotNull
      @Override
      public PsiType getType() {
        if (ourFlowScopeResultType == null) {
          ourFlowScopeResultType = getScopeResultType(expressionAttr, psiTypeAttr, file, this);
        }
        return ourFlowScopeResultType;
      }

      @Override
      public boolean equals(Object obj) {
        if (obj instanceof JspImplicitVariableImpl) {
          if (getDeclaration() != null && getDeclaration().equals(((JspImplicitVariableImpl)obj).getDeclaration())) return true;
        }
        return super.equals(obj);
      }
    };
  }

  public static List<Evaluate> collectEvaluates(WebflowModel webflowModel) {
    return collectEvaluates(webflowModel, true);
  }

  public static List<Evaluate> collectEvaluates(WebflowModel webflowModel, boolean withParents) {
    final List<Evaluate> evaluates = new ArrayList<Evaluate>();   //todo cache evaluates

    DomElementVisitor visitor = new DomElementVisitor() {
      public void visitEvaluate(Evaluate evaluate) {
        evaluates.add(evaluate);
      }

      public void visitDomElement(DomElement element) {
        element.acceptChildren(this);
      }
    };

    visitFlow(visitor, webflowModel.getFlow(), withParents);

    return evaluates;
  }

  private static void visitFlow(DomElementVisitor visitor, Flow flow, boolean withParents) {
    flow.accept(visitor);

    if (withParents) {
      for (Flow parentFlow : WebflowUtil.getAllParentFlows(flow)) {
        parentFlow.accept(visitor);
      }
    }
  }

  public static List<Set> collectSetters(WebflowModel webflowModel) {
    return collectSetters(webflowModel, true);
  }

  public static List<Set> collectSetters(WebflowModel webflowModel, boolean withParents) {
    final List<Set> sets = new ArrayList<Set>();

    DomElementVisitor visitor = new DomElementVisitor() {
      public void visitSet(Set set) {
        sets.add(set);
      }

      public void visitDomElement(DomElement element) {
        element.acceptChildren(this);
      }
    };

    visitFlow(visitor, webflowModel.getFlow(), withParents);

    return sets;
  }

  @NotNull
  private static PsiType getScopeResultType(GenericAttributeValue<String> expressionAttr,
                                            GenericAttributeValue<PsiType> psiTypeAttr,
                                            PsiFile containingFile,
                                            final JspImplicitVariableImpl jspImplicitVariable) {
    PsiType type = psiTypeAttr.getValue();
    if (type != null) return type;

    XmlAttributeValue context = expressionAttr.getXmlAttributeValue();
    final Ref<PsiType> injectionType = new Ref<PsiType>();

    assert context != null;

    ((PsiLanguageInjectionHost)context).processInjectedPsi(new PsiLanguageInjectionHost.InjectedPsiVisitor() {
      public void visit(@NotNull PsiFile injectedPsi, @NotNull List<PsiLanguageInjectionHost.Shred> places) {
        PsiElement at = injectedPsi.findElementAt(injectedPsi.getTextLength() - 1);
        ELExpressionHolder holder = PsiTreeUtil.getParentOfType(at, ELExpressionHolder.class);
        if (holder != null) {
          PsiElement firstChild = holder.getFirstChild();

          if (!isSelfReference(firstChild, jspImplicitVariable)) {
            injectionType.set(ELResolveUtil.resolveContextAsType(firstChild));
          }
        }
      }
    });

    PsiType contextType = injectionType.get();
    if (contextType != null) return contextType;

    return getObjectClassType(containingFile.getProject());
  }

  private static boolean isSelfReference(PsiElement firstChild, JspImplicitVariableImpl jspImplicitVariable) {
    ELVariable var = null;
    if (firstChild instanceof ELVariable) var = (ELVariable)firstChild;
    if (firstChild instanceof ELSelectExpression) var = ((ELSelectExpression)firstChild).getField();


    if (var != null) {
      PsiReference[] references = var.getReferences();
      if (references.length > 0 && jspImplicitVariable.equals(references[0].resolve())) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  public static String getVariableName(WebflowScope scope, String value) {
    if (StringUtil.isEmptyOrSpaces(value)) return null;

    String s = StringUtil.trimStart(value, scope.getName() + ".");

    return s.equals(value) || s.contains(".") ? null : s;
  }

  public static void collectPredefinedVariables(final ProcessingContext context) {
    for (Pair<String, String> pair : predefinedVars) {
      String varName = pair.first;
      String className = pair.second;

      PsiType psiType = getPsiClassTypeByName(context.getProject(), className);
      if (psiType == null) psiType = getObjectClassType(context.getProject());

      addImplicitVariable(new FakePsiElement() {
        public PsiElement getParent() {
          return context.getFile();
        }
      }, varName, context.getResultVars(), psiType, context.getFile());
    }
  }

  private static PsiType getObjectClassType(Project project) {
    PsiType psiType = getPsiClassTypeByName(project, CommonClassNames.JAVA_LANG_OBJECT);

    return psiType == null ? PsiType.VOID : psiType;
  }

  @Nullable
  private static PsiType getPsiClassTypeByName(Project project, String className) {
    JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);

    PsiClass psiClass = psiFacade.findClass(className, GlobalSearchScope.allScope(project));

    return psiClass == null ? null : psiFacade.getElementFactory().createType(psiClass);
  }

  private static void addImplicitVariable(PsiElement psiElement,
                                          @Nullable String name,
                                          List<JspImplicitVariable> result,
                                          @Nullable PsiType type,
                                          PsiFile file) {

    if (name == null || name.length() == 0 || type == null) return;

    result.add(new JspImplicitVariableImpl(file, name, type, psiElement, JspImplicitVariableImpl.NESTED_RANGE));
  }

  @Nullable
  private static JspImplicitVariableImpl createImplicitVariable(WebflowDomElement element,
                                                                @Nullable String name,
                                                                @Nullable PsiType type,
                                                                PsiFile file) {

    if (name == null || name.length() == 0 || type == null) return null;

    DomTarget target = DomTarget.getTarget(element);
    assert target != null;
    return new JspImplicitVariableImpl(file, name, type, PomService.convertToPsi(target), JspImplicitVariableImpl.NESTED_RANGE);
  }

  @Nullable
  public static DomElement getDomElement(PsiElement host) {
    return DomManager.getDomManager(host.getProject()).getDomElement(PsiTreeUtil.getParentOfType(host, XmlTag.class));
  }

  private static class ProcessingContext {
    private final WebflowModel myWebflowModel;
    private final PsiElement myHost;
    private final List<JspImplicitVariable> myResultVars;
    private final DomElement myDomElement;
    private Map<WebflowScope, List<JspImplicitVariable>> myScopeVarsMap;

    public ProcessingContext(WebflowModel webflowModel, PsiElement host, List<JspImplicitVariable> resultVars) {
      myWebflowModel = webflowModel;
      myHost = host;
      myResultVars = resultVars;
      myDomElement = DomUtil.getDomElement(host);
    }

    public List<JspImplicitVariable> getResultVars() {
      return myResultVars;
    }

    public Map<WebflowScope, List<JspImplicitVariable>> getScopeVariablesMap() {
      if (myScopeVarsMap == null) {
        myScopeVarsMap = new HashMap<WebflowScope, List<JspImplicitVariable>>();
        for (WebflowScopeProvider provider : WebflowScopeProviderManager.getService(getModule()).getAvailableProviders(getDomElement())) {
          myScopeVarsMap.put(provider.getScope(), new ArrayList<JspImplicitVariable>());
        }
      }

      return myScopeVarsMap;
    }

    public XmlFile getFile() {
      return (XmlFile)myHost.getContainingFile().getOriginalFile();
    }

    public WebflowModel getWebflowModel() {
      return myWebflowModel;
    }

    public PsiElement getHost() {
      return myHost;
    }

    @Nullable
    public DomElement getDomElement() {
      return myDomElement;
    }

    public Module getModule() {
      return getWebflowModel().getFlow().getModule();
    }

    public Project getProject() {
      return myHost.getProject();
    }

    public List<WebflowScopeProvider> getAcceptedProviders() {
      List<WebflowScopeProvider> acceptedProviders = new ArrayList<WebflowScopeProvider>();

      if (getDomElement() != null) {
        for (WebflowScopeProvider scopeProvider : WebflowScopeProviderManager.getService(getModule()).getProviders()) {
          if (scopeProvider.accept(getDomElement())) {
            acceptedProviders.add(scopeProvider);
          }
        }
      }
      return acceptedProviders;
    }
  }
}
