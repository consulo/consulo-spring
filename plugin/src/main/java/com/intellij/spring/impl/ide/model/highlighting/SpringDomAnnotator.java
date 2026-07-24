/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.gutter.DomElementListCellRenderer;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.ui.navigation.NavigationGutterIconBuilder;
import consulo.language.psi.PsiElement;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.util.collection.ContainerUtil;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomManager;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class SpringDomAnnotator implements Annotator {

  private static final DomElementListCellRenderer RENDERER = new DomElementListCellRenderer(SpringBundle.message("spring.bean.with.unknown.name"));

  private static final Function<SpringBaseBeanPointer, Collection<? extends PsiElement>> CONVERTER =
    new Function<SpringBaseBeanPointer, Collection<? extends PsiElement>>() {

      @Nonnull
      public Collection<? extends PsiElement> apply(SpringBaseBeanPointer pointer) {
        return ContainerUtil.createMaybeSingletonList(pointer.getPsiElement());
      }
    };

  private static void annotateBean(DomSpringBean bean, AnnotationHolder holder) {
    XmlTag tag = bean.getXmlTag();
    if (tag == null) return;

    SpringModel model = SpringUtils.getSpringModel(bean);
    List<SpringBaseBeanPointer> children = model.getChildren(SpringBeanPointer.createSpringBeanPointer(bean));
    if (children.size() > 0) {

      NavigationGutterIconBuilder<SpringBaseBeanPointer> iconBuilder =
        NavigationGutterIconBuilder.create(SpringImplIconGroup.gutterParentbeangutter(), CONVERTER);
      iconBuilder.
                   setTargets(children).
                   setPopupTitle(SpringBundle.message("spring.bean.class.navigate.choose.class.title")).
                   setCellRenderer(RENDERER).
                   setTooltipText(SpringBundle.message("spring.parent.bean.tooltip")).
                   install(holder, bean.getXmlTag());
    }
  }

  public void annotate(PsiElement psiElement, AnnotationHolder holder) {
    if (psiElement instanceof XmlTag) {
      DomElement element = DomManager.getDomManager(psiElement.getProject()).getDomElement((XmlTag)psiElement);
      if (element instanceof DomSpringBean) {
        annotateBean((DomSpringBean)element, holder);
      }
    }
  }
}
