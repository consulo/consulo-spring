/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.aop;

import com.intellij.aop.AopAdvice;
import com.intellij.aop.AopIntroduction;
import com.intellij.aop.jam.AopJavaAnnotator;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.aop.DeclareParents;
import com.intellij.spring.impl.ide.model.xml.aop.SpringAopAdvice;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.editor.gutter.LineMarkerProvider;
import consulo.language.editor.ui.navigation.NavigationGutterIconBuilder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.xml.language.XMLLanguage;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.language.psi.XmlToken;
import consulo.xml.language.psi.util.XmlTagUtil;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomFileElement;
import consulo.xml.dom.DomManager;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author peter
 */
@ExtensionImpl
public class SpringAopAnnotator implements LineMarkerProvider {

  public LineMarkerInfo getLineMarkerInfo(PsiElement element) {
    return null;
  }

  public void collectSlowLineMarkers(List<PsiElement> elements, Collection<LineMarkerInfo> result) {
    if (elements.isEmpty()) return;

    PsiFile file = elements.get(0).getContainingFile();
    if (!(file instanceof XmlFile)) return;

    XmlFile xmlFile = (XmlFile)file;
    DomFileElement<Beans> fileElement = DomManager.getDomManager(xmlFile.getProject()).getFileElement(xmlFile, Beans.class);
    if (fileElement == null) return;

    for (PsiElement element : elements) {
      annotate(element, result);
    }
  }

  private static void annotate(PsiElement psiElement, Collection<LineMarkerInfo> result) {
    if (psiElement instanceof XmlToken && psiElement.getParent() instanceof XmlTag) {
      XmlTag tag = (XmlTag)psiElement.getParent();
      if (XmlTagUtil.getStartTagNameElement(tag) == psiElement) {
        DomElement element = DomManager.getDomManager(psiElement.getProject()).getDomElement(tag);
        if (element instanceof SpringAopAdvice) {
          SpringAopAdvice advice = (SpringAopAdvice)element;
          result.add(AopJavaAnnotator.addNavigationToInterceptedMethods(advice, advice.getSearcher()).createLineMarkerInfo(psiElement));
        }
        else if (element instanceof DeclareParents) {
          NavigationGutterIconBuilder<PsiElement> builder =
            AopJavaAnnotator.addNavigationToIntroducedClasses((DeclareParents)element);
          if (builder != null) {
            result.add(builder.createLineMarkerInfo(psiElement));
          }
        }
        else if (element instanceof DomSpringBean) {
          PsiClass psiClass = ((DomSpringBean)element).getBeanClass();
          if (psiClass != null) {
            Map<AopAdvice, Integer> advices = AopJavaAnnotator.getBoundAdvices(psiClass);
            if (!advices.isEmpty()) {
              result.add(AopJavaAnnotator.addNavigationToBoundAdvices(advices).createLineMarkerInfo(psiElement));
            }
            List<AopIntroduction> introductions = AopJavaAnnotator.getBoundIntroductions(psiClass);
            if (!introductions.isEmpty()) {
              result.add(AopJavaAnnotator.addNavigationToBoundIntroductions(introductions).createLineMarkerInfo(psiElement));
            }
          }
        }
      }
    }
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return XMLLanguage.INSTANCE;
  }
}
