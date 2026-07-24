/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.util.collection.ContainerUtil;

import jakarta.annotation.Nonnull;
import java.util.Collection;

/**
 * @author peter
 */
public class AopArrayExpression extends AopElementBase implements AopTypeExpression {
  public AopArrayExpression(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "AopArrayExpression";
  }

  @Nonnull
  @RequiredReadAction
  public AopTypeExpression getTypeReference() {
    return findNotNullChildByClass(AopTypeExpression.class);
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public Collection<AopPsiTypePattern> getPatterns() {
    boolean varargs = isVarargs();
    return ContainerUtil.map2List(getTypeReference().getPatterns(), aopPsiTypePattern -> new ArrayPattern(aopPsiTypePattern, varargs));
  }

  @Override
  @RequiredReadAction
  public String getTypePattern() {
    String pattern = getTypeReference().getTypePattern();
    return pattern == null ? null : isVarargs() ? pattern + "..." : pattern + "[]";
  }

  @RequiredReadAction
  public boolean isVarargs() {
    return findChildByType(AopElementTypes.AOP_VARARGS) != null;
  }
}
