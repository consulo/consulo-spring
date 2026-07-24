/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.gutter;

import consulo.language.editor.ui.PsiElementListCellRenderer;
import consulo.language.psi.PsiElement;
import consulo.ui.image.Image;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomManager;
import org.jetbrains.annotations.Nls;

import jakarta.annotation.Nullable;

/**
 * @author Dmitry Avdeev
 */
public class DomElementListCellRenderer extends PsiElementListCellRenderer<XmlTag> {

  protected final String myUnknown;

  public DomElementListCellRenderer(@Nls String unknownElementText) {
    myUnknown = unknownElementText;
  }

  public String getElementText(XmlTag element) {
    DomElement domElement = getDomElement(element);
    if (domElement == null) return element.getName();

    String elementName = domElement.getPresentation().getElementName();
    return elementName == null ? myUnknown : elementName;
  }

  protected String getContainerText(XmlTag element, String name) {
    return getContainerText(element);
  }

  public static String getContainerText(PsiElement element) {
    return " (" + element.getContainingFile().getName() + ")";
  }

  protected int getIconFlags() {
    return 0;
  }

  protected Image getIcon(PsiElement element) {
    DomElement domElement = getDomElement((XmlTag)element);
    if (domElement != null && domElement.getPresentation().getIcon() != null) {
      return domElement.getPresentation().getIcon();
    }

    return super.getIcon(element);
  }

  @Nullable
  protected static DomElement getDomElement(XmlTag tag) {
    return DomManager.getDomManager(tag.getProject()).getDomElement(tag);
  }
}
