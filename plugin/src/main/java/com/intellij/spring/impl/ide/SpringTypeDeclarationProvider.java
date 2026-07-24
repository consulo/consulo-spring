/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.impl.ide;

import com.intellij.jam.JamService;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMember;
import com.intellij.spring.impl.ide.model.jam.JamPsiMemberSpringBean;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.custom.CustomBeanFakePsiElement;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.action.TypeDeclarationProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomManager;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
@ExtensionImpl
public class SpringTypeDeclarationProvider extends TypeDeclarationProvider {
  public PsiElement[] getSymbolTypeDeclarations(@Nonnull PsiElement symbol, @Nullable Editor editor, int offset) {
    if (symbol instanceof CustomBeanFakePsiElement) {
      return getBeanTypeDeclaration(((CustomBeanFakePsiElement)symbol).getBean());
    }
    if (symbol instanceof XmlTag) {
      XmlTag tag = (XmlTag)symbol;
      DomElement element = DomManager.getDomManager(tag.getProject()).getDomElement(tag);
      if (element instanceof DomSpringBean) {
        return getBeanTypeDeclaration((DomSpringBean)element);
      }
    }
    if (symbol instanceof PsiAnnotation) {
      PsiMember member = PsiTreeUtil.getParentOfType(symbol, PsiMember.class);
      CommonSpringBean springBean =
        member == null ? null : JamService.getJamService(symbol.getProject()).getJamElement(JamPsiMemberSpringBean.class, member);
      if (springBean != null) {
        return getBeanTypeDeclaration(springBean);
      }
    }
    return null;
  }

  @Nullable
  private static PsiElement[] getBeanTypeDeclaration(CommonSpringBean bean) {
    PsiClass psiClass = bean.getBeanClass(true);
    return psiClass != null ? new PsiElement[]{psiClass} : null;
  }
}
