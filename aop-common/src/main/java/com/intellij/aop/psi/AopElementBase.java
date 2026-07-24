/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import jakarta.annotation.Nonnull;

import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.ast.ASTNode;

/**
 * @author peter
 */
public class AopElementBase extends ASTWrapperPsiElement {
  public AopElementBase(@Nonnull ASTNode node) {
    super(node);
  }

  @Nonnull
  public AopPointcutExpressionFile getContainingFile() {
    return (AopPointcutExpressionFile)super.getContainingFile();
  }
}
