/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
public class AopConstructorReferenceExpression extends AopMemberReferenceExpression {
  public AopConstructorReferenceExpression(@Nonnull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "AopConstructorReferenceExpression";
  }

}
