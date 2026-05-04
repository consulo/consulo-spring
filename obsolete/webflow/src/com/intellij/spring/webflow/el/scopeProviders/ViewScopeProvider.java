package com.intellij.spring.webflow.el.scopeProviders;

import com.intellij.openapi.module.Module;
import consulo.util.dataholder.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.webflow.el.ELVariablesCollectorUtils;
import com.intellij.spring.webflow.el.WebflowScope;
import com.intellij.spring.webflow.el.WebflowScopeProvider;
import com.intellij.spring.webflow.model.xml.*;
import com.intellij.spring.webflow.resources.messages.WebflowBundle;
import java.util.HashMap;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ViewScopeProvider implements WebflowScopeProvider {
  private static final Key<CachedValue<Map<String, PsiElement>>> VIEW_SCOPE_VARIABLES_KEY = Key.create("VIEW_SCOPE_VARIABLES_KEY");

  private final Module myModule;

  public ViewScopeProvider(final Module module) {
    myModule = module;
  }

  public WebflowScope getScope() {
    return WebflowScope.VIEW;
  }

  public boolean accept(@Nullable final DomElement domElement) {
    return getScopes(domElement).size() > 0;
  }

  @NotNull
  public Set<DomElement> getScopes(@Nullable final DomElement domElement) {
    if(domElement != null) {
      final ViewState state = domElement.getParentOfType(ViewState.class, false);
      if (state != null) {
        return Collections.<DomElement>singleton(state);
      }
    }
    return Collections.emptySet();
  }

  @Nullable
  public PsiElement getOrCreateScopeVariable(final XmlFile psiFile, final String varName, final PsiElement host) {
    final ViewState state = getViewState(host);

    if (state != null && state.isValid()) {
      final XmlTag tag = state.getXmlTag();
      if (tag != null) {
        CachedValue<Map<String, PsiElement>> cachedValue = tag.getUserData(VIEW_SCOPE_VARIABLES_KEY);
        if (cachedValue == null) {
          cachedValue = PsiManager.getInstance(psiFile.getProject()).getCachedValuesManager()
              .createCachedValue(new CachedValueProvider<Map<String, PsiElement>>() {
                public Result<Map<String, PsiElement>> compute() {
                  return new Result<Map<String, PsiElement>>(collectViewScopeVariables(state),
                                                             PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
                }
              }, false);

          tag.putUserData(VIEW_SCOPE_VARIABLES_KEY, cachedValue);
        }

        final Map<String, PsiElement> map = cachedValue.getValue();

        assert map != null;

        return map.get(varName);
      }
    }
    return null;
  }


  @Nullable
  private static ViewState getViewState(final PsiElement host) {
    final DomElement domElement = DomUtil.getDomElement(host);
    if (domElement != null) {
      return domElement.getParentOfType(ViewState.class, false);
    }
    return null;
  }

  private static Map<String, PsiElement> collectViewScopeVariables(final ViewState viewState) {
    Map<String, PsiElement> map = new HashMap<String, PsiElement>();

    final List<Evaluate> evaluates = new ArrayList<Evaluate>();
    final List<Set> sets = new ArrayList<Set>();

    collectEvaluates(viewState, evaluates);
    collectSets(viewState, sets);

    for (final Evaluate evaluate : evaluates) {
       if (!viewState.equals(evaluate.getParentOfType(ViewState.class, false))) continue;

      final String varName = ELVariablesCollectorUtils.getVariableName(WebflowScope.VIEW, evaluate.getResult().getStringValue());
      if (!StringUtil.isEmptyOrSpaces(varName)) {
        map.put(varName, new FakeScopePsiVariable(DomUtil.getFile(viewState), evaluate.getResult().getXmlAttributeValue(), varName) {
          public String getTypeName() {
            return WebflowBundle.message("view.scope.type.name");
          }
        });
      }
    }

    for (final Set set : sets) {
      if (!viewState.equals(set.getParentOfType(ViewState.class, false))) continue;
      
      final String varName = ELVariablesCollectorUtils.getVariableName(WebflowScope.VIEW, set.getName().getStringValue());
      if (!StringUtil.isEmptyOrSpaces(varName)) {
        map.put(varName, new FakeScopePsiVariable(DomUtil.getFile(viewState), set.getName().getXmlAttributeValue(), varName) {
          public String getTypeName() {
            return WebflowBundle.message("view.scope.type.name");
          }
        });
      }
    }

    return map;
  }

  private static void collectSets(final ViewState viewState, final List<Set> sets) {
      addSets(sets, viewState.getOnEntry());
      addSets(sets, viewState.getOnRender());
      addSets(sets, viewState.getOnExit());
  }


  private static void collectEvaluates(final ViewState viewState, final List<Evaluate> evaluates) {
    addEvaluates(evaluates, viewState.getOnEntry());
    addEvaluates(evaluates, viewState.getOnRender());
    addEvaluates(evaluates, viewState.getOnExit());
    for (Transition transition : viewState.getTransitions()) {
      addEvaluates(evaluates, transition);
    }
  }

  private static void addEvaluates(final List<Evaluate> evaluates, final EvaluatesOwner evaluatesOwner) {
    evaluates.addAll(evaluatesOwner.getEvaluates());
  }

  private static void addSets(final List<Set> sets, final SetsOwner evaluatesOwner) {
    sets.addAll(evaluatesOwner.getSets());
  }

}
