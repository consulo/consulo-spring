/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.aop;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.ArgNamesManipulator;
import com.intellij.aop.LocalAopModel;
import com.intellij.spring.impl.ide.model.xml.aop.BasicAdvice;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;

import consulo.xml.language.psi.XmlTag;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public class SpringLocalAopModel extends LocalAopModel {

  public SpringLocalAopModel(@Nonnull PsiElement host, @Nullable BasicAdvice advice, @Nonnull AopAdvisedElementsSearcher searcher) {
    super(host, advice == null ? null : advice.getMethod().getValue(), searcher);
  }

  @Nonnull
  public ArgNamesManipulator getArgNamesManipulator() {
    return new SpringArgNamesManipulator(PsiTreeUtil.getParentOfType(getHost(), XmlTag.class));
  }

}
