/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import consulo.language.ast.ASTNode;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
public class AopElementBase extends ASTWrapperPsiElement {
  public AopElementBase(@Nonnull ASTNode node) {
    super(node);
  }

  @Nonnull
  @Override
  public AopPointcutExpressionFile getContainingFile() {
    return (AopPointcutExpressionFile)super.getContainingFile();
  }
}
