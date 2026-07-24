/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.impl.ide.aop;

import com.intellij.aop.AopIntroduction;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import com.intellij.spring.impl.ide.model.xml.aop.AopConfig;
import com.intellij.spring.impl.ide.model.xml.aop.BasicAdvice;
import com.intellij.spring.impl.ide.model.xml.aop.SpringAspect;
import consulo.application.util.function.Processor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.SmartList;
import consulo.xml.language.XmlFileType;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomManager;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

/**
 * @author peter
 */
public class SpringAdviceLocalAopModel extends SpringLocalAopModel {
  private final XmlTag myTag;
  @Nullable
  private final BasicAdvice myAdvice;

  public SpringAdviceLocalAopModel(PsiElement host, @Nullable BasicAdvice basicAdvice, SpringAdvisedElementsSearcher searcher) {
    super(host, basicAdvice, searcher);
    myAdvice = basicAdvice;
    myTag = PsiTreeUtil.getParentOfType(host, XmlTag.class);
    assert myTag != null;
  }

  @Nonnull
  public List<PsiParameter> resolveParameters(@Nonnull @NonNls final String name) {
    if (!"pointcut".equals(myTag.getLocalName())) return super.resolveParameters(name);

    final List<PsiParameter> result = new SmartList<PsiParameter>();
    GlobalSearchScope scope =
      GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(myTag.getProject()), XmlFileType.INSTANCE);
    ReferencesSearch.search(myTag, scope).forEach(new Processor<PsiReference>() {
      public boolean process(PsiReference reference) {
        PsiElement psiElement = reference.getElement();
        XmlTag xmlTag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class);
        if (xmlTag != null) {
          DomElement domElement = DomManager.getDomManager(psiElement.getProject()).getDomElement(xmlTag);
          if (domElement instanceof BasicAdvice) {
            BasicAdvice advice = (BasicAdvice)domElement;
            PsiMethod method = advice.getMethod().getValue();
            if (method != null) {
              PsiParameter element = findParameter(name, method);
              result.add(element);
            }
          }
        }
        return true;
      }
    });

    return result;
  }

  public List<AopIntroduction> getIntroductions() {
    List<AopIntroduction> introductions = super.getIntroductions();
    if (myAdvice != null) {
      for (SpringAspect aspect : ((AopConfig)myAdvice.getParent().getParent()).getAspects()) {
        introductions.addAll(aspect.getIntroductions());
      }
    }
    return introductions;
  }
}
