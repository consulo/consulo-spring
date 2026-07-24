/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

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

  public String toString() {
    return "AopWildcardExpression";
  }

  @Nullable
  public AopTypeExpression getBound() {
    return findChildByClass(AopTypeExpression.class);
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    AopTypeExpression bound = getBound();
    boolean isSuper = isSuper();
    if (bound == null) return Arrays.<AopPsiTypePattern>asList(new WildcardPattern(null, isSuper));

    return ContainerUtil.map2List(bound.getPatterns(), aopPsiTypePattern -> new WildcardPattern(aopPsiTypePattern, isSuper));
  }

  public String getTypePattern() {
    return "'_";
  }

  public boolean isExtends() {
    return findChildByType(AopElementTypes.AOP_EXTENDS) != null;
  }

  public boolean isSuper() {
    return findChildByType(AopElementTypes.AOP_SUPER) != null;
  }
}