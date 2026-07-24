/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.properties;

import com.intellij.java.impl.psi.impl.beanProperties.BeanProperty;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJavaBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringPropertyDefinition;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.language.editor.LangDataKeys;
import consulo.language.editor.PlatformDataKeys;
import consulo.language.editor.TargetElementUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomManager;
import consulo.xml.dom.DomUtil;
import consulo.xml.dom.GenericAttributeValue;

import jakarta.annotation.Nullable;

/**
 * @author Dmitry Avdeev
 */
public class SpringPropertiesUtil {
  private SpringPropertiesUtil() {
  }

  @Nullable
  public static BeanProperty getBeanProperty(DataContext dataContext) {
    Editor editor = dataContext.getData(PlatformDataKeys.EDITOR);
    PsiFile file = dataContext.getData(LangDataKeys.PSI_FILE);

    return getBeanProperty(editor, file);
  }

  @Nullable
  public static BeanProperty getBeanProperty(Editor editor, PsiFile file) {
    if (editor != null && file instanceof XmlFile) {
      int offset = editor.getCaretModel().getOffset();
      DomElement value = DomUtil.getContextElement(editor);
      SpringPropertyDefinition property = DomUtil.getParentOfType(value, SpringPropertyDefinition.class, false);

      if (property == null || isJavaBeanReference(file, offset)) return null;

      PsiReference reference = TargetElementUtil.findReference(editor, offset);
      if (reference != null) {
        PsiElement psiElement = reference.resolve();
        if (psiElement instanceof PsiMethod) {
          return BeanProperty.createBeanProperty((PsiMethod)psiElement);
        }
      }
    }
    return null;
  }

  @Nullable
  public static BeanProperty getBeanProperty(PsiElement element) {
    PsiFile file = element.getContainingFile();
    if (file instanceof XmlFile) {
      DomElement value = DomUtil.getDomElement(element);
      SpringPropertyDefinition property = DomUtil.getParentOfType(value, SpringPropertyDefinition.class, false);

      if (property == null || isJavaBeanReference(file, element)) return null;

      PsiReference reference = file.findReferenceAt(element.getTextOffset());
      if (reference != null) {
        PsiElement psiElement = reference.resolve();
        if (psiElement instanceof PsiMethod) {
          return BeanProperty.createBeanProperty((PsiMethod)psiElement);
        }
      }
    }
    return null;
  }

  static boolean isJavaBeanReference(PsiFile file, PsiElement element) {
    XmlAttribute xmlAttribute = PsiTreeUtil.getParentOfType(element, XmlAttribute.class);

    if (xmlAttribute != null) {
      DomElement value = DomManager.getDomManager(file.getProject()).getDomElement(xmlAttribute);
      if (value instanceof GenericAttributeValue) {
        Object attributeValue = ((GenericAttributeValue) value).getValue();
        if (attributeValue instanceof SpringBeanPointer) {
          return ((SpringBeanPointer) attributeValue).getSpringBean() instanceof SpringJavaBean;
        }
      }
    }

    return false;
  }

  static boolean isJavaBeanReference(PsiFile file, int offset) {
    XmlAttribute xmlAttribute = PsiTreeUtil.getParentOfType(file.findElementAt(offset), XmlAttribute.class);

    if (xmlAttribute != null) {
      DomElement value = DomManager.getDomManager(file.getProject()).getDomElement(xmlAttribute);
      if (value instanceof GenericAttributeValue) {
        Object attributeValue = ((GenericAttributeValue)value).getValue();
        if (attributeValue instanceof SpringBeanPointer) {
          return ((SpringBeanPointer)attributeValue).getSpringBean() instanceof SpringJavaBean;
        }
      }
    }

    return false;
  }
}
