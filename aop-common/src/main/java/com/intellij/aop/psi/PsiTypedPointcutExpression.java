/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import consulo.language.ast.ASTNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author peter
 */
public abstract class PsiTypedPointcutExpression extends AopElementBase implements PsiPointcutExpression {
  public PsiTypedPointcutExpression(@Nonnull final ASTNode node) {
    super(node);
  }

  @Nullable
  public AopReferenceHolder getTypeReference() {
    return (AopReferenceHolder)findChildByType(AopElementTypes.AOP_REFERENCE_HOLDER);
  }

}
