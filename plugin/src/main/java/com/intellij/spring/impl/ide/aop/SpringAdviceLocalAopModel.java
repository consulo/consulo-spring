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
import consulo.xml.ide.highlighter.XmlFileType;
import consulo.xml.psi.xml.XmlTag;
import consulo.xml.util.xml.DomElement;
import consulo.xml.util.xml.DomManager;
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

  public SpringAdviceLocalAopModel(final PsiElement host, @Nullable final BasicAdvice basicAdvice, final SpringAdvisedElementsSearcher searcher) {
    super(host, basicAdvice, searcher);
    myAdvice = basicAdvice;
    myTag = PsiTreeUtil.getParentOfType(host, XmlTag.class);
    assert myTag != null;
  }

  @Nonnull
  public List<PsiParameter> resolveParameters(@Nonnull @NonNls final String name) {
    if (!"pointcut".equals(myTag.getLocalName())) return super.resolveParameters(name);

    final List<PsiParameter> result = new SmartList<PsiParameter>();
    final GlobalSearchScope scope =
      GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(myTag.getProject()), XmlFileType.INSTANCE);
    ReferencesSearch.search(myTag, scope).forEach(new Processor<PsiReference>() {
      public boolean process(final PsiReference reference) {
        final PsiElement psiElement = reference.getElement();
        final XmlTag xmlTag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class);
        if (xmlTag != null) {
          final DomElement domElement = DomManager.getDomManager(psiElement.getProject()).getDomElement(xmlTag);
          if (domElement instanceof BasicAdvice) {
            final BasicAdvice advice = (BasicAdvice)domElement;
            final PsiMethod method = advice.getMethod().getValue();
            if (method != null) {
              final PsiParameter element = findParameter(name, method);
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
    final List<AopIntroduction> introductions = super.getIntroductions();
    if (myAdvice != null) {
      for (final SpringAspect aspect : ((AopConfig)myAdvice.getParent().getParent()).getAspects()) {
        introductions.addAll(aspect.getIntroductions());
      }
    }
    return introductions;
  }
}
