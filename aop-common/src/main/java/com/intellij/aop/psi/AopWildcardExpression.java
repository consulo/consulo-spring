/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.util.collection.ContainerUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class AopWildcardExpression extends AopElementBase implements AopTypeExpression {
  public AopWildcardExpression(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "AopWildcardExpression";
  }

  @Nullable
  @RequiredReadAction
  public AopTypeExpression getBound() {
    return findChildByClass(AopTypeExpression.class);
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public Collection<AopPsiTypePattern> getPatterns() {
    AopTypeExpression bound = getBound();
    boolean isSuper = isSuper();
    if (bound == null) return Arrays.<AopPsiTypePattern>asList(new WildcardPattern(null, isSuper));

    return ContainerUtil.map2List(bound.getPatterns(), aopPsiTypePattern -> new WildcardPattern(aopPsiTypePattern, isSuper));
  }

  @Override
  public String getTypePattern() {
    return "'_";
  }

  @RequiredReadAction
  public boolean isExtends() {
    return findChildByType(AopElementTypes.AOP_EXTENDS) != null;
  }

  @RequiredReadAction
  public boolean isSuper() {
    return findChildByType(AopElementTypes.AOP_SUPER) != null;
  }
}