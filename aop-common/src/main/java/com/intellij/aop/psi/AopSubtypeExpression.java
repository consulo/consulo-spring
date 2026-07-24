/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import consulo.language.ast.ASTNode;
import consulo.util.collection.ContainerUtil;

import jakarta.annotation.Nonnull;
import java.util.Collection;

/**
 * @author peter
 */
public class AopSubtypeExpression extends AopElementBase implements AopReferenceQualifier {
  public AopSubtypeExpression(@Nonnull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "AopSubtypeExpression";
  }

  @Nonnull
  public AopTypeExpression getSupertypeExpression() {
    return findNotNullChildByClass(AopTypeExpression.class);
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    return ContainerUtil.map2List(getSupertypeExpression().getPatterns(), SubtypePattern::new);
  }

  public String getTypePattern() {
    return "'_";
  }

  public AopReferenceExpression.Resolvability getResolvability() {
    return AopReferenceExpression.Resolvability.NONE;
  }

}