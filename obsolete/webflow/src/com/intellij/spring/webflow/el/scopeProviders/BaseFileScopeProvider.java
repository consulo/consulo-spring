package com.intellij.spring.webflow.el.scopeProviders;

import consulo.util.dataholder.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.webflow.el.ELVariablesCollectorUtils;
import com.intellij.spring.webflow.el.WebflowScopeProvider;
import com.intellij.spring.webflow.model.xml.*;
import java.util.HashMap;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class BaseFileScopeProvider implements WebflowScopeProvider {

  public boolean accept(@Nullable final DomElement domElement) {
     return getScopes(domElement).size() > 0;
  }

  @NotNull
  public Set<DomElement> getScopes(@Nullable final DomElement domElement) {
    if(domElement != null) {
      final Flow flow = domElement.getParentOfType(Flow.class, false);
      if (flow != null) {
        return Collections.<DomElement>singleton(flow);
      }
    }
    return Collections.emptySet();
  }

  public PsiElement getOrCreateScopeVariable(final XmlFile psiFile, final String varName, final PsiElement host) {
    CachedValue<Map<String, PsiElement>> cachedValue = host.getContainingFile().getUserData(getScopeVariablesKey());
    if (cachedValue == null) {
      cachedValue = PsiManager.getInstance(psiFile.getProject()).getCachedValuesManager()
          .createCachedValue(new CachedValueProvider<Map<String, PsiElement>>() {
            public Result<Map<String, PsiElement>> compute() {
              return new Result<Map<String, PsiElement>>(collectScopeVariables(psiFile),
                                                         PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
            }
          }, false);

      psiFile.putUserData(getScopeVariablesKey(), cachedValue);
    }

    final Map<String, PsiElement> map = cachedValue.getValue();

    assert map != null;

    return map.get(varName);
  }

  // collect scope variables for current file
  private Map<String, PsiElement> collectScopeVariables(final XmlFile psiFile) {
    Map<String, PsiElement> map = new HashMap<String, PsiElement>();
    final WebflowModel webflowModel = WebflowDomModelManager.getInstance(psiFile.getProject()).getWebflowModel(psiFile);

    if (webflowModel != null) {
      for (final Evaluate evaluate : ELVariablesCollectorUtils.collectEvaluates(webflowModel, false)) {
        final String varName = ELVariablesCollectorUtils.getVariableName(getScope(), evaluate.getResult().getStringValue());
        if (!StringUtil.isEmptyOrSpaces(varName)) {
          map.put(varName, new FakeScopePsiVariable(psiFile, evaluate.getResult().getXmlAttributeValue(), varName){
            public String getTypeName() {
              return BaseFileScopeProvider.this.getTypeName();
            }
          });
        }
      }
      for (final Set set : ELVariablesCollectorUtils.collectSetters(webflowModel, false)) {
        final String varName = ELVariablesCollectorUtils.getVariableName(getScope(), set.getName().getStringValue());
        if (!StringUtil.isEmptyOrSpaces(varName)) {
          map.put(varName, new FakeScopePsiVariable(psiFile, set.getName().getXmlAttributeValue(), varName){
            public String getTypeName() {
              return BaseFileScopeProvider.this.getTypeName();
            }
          });
        }
      }
    }

    return map;
  }

  protected abstract Key<CachedValue<Map<String, PsiElement>>> getScopeVariablesKey();

  protected abstract String getTypeName();
}
