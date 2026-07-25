/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model.aop;

import jakarta.annotation.Nullable;

import consulo.language.psi.PsiFile;
import com.intellij.spring.impl.ide.model.xml.aop.DeclareParents;
import consulo.xml.dom.DomUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;

/**
 * @author peter
 */
public abstract class DeclareParentsImpl implements DeclareParents {
  @Nullable
  @Override
  public PsiFile getContainingFile() {
    return DomUtil.getFile(this);
  }

  @Nullable
  @Override
  public PsiElement getIdentifyingPsiElement() {
    return getXmlTag();
  }

  @Override
  public PsiManager getPsiManager() {
    return PsiManager.getInstance(getManager().getProject());
  }
}