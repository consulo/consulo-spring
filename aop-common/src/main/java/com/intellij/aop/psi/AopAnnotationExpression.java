/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class AopAnnotationExpression extends AopElementBase implements AopAnnotationPattern{
  public AopAnnotationExpression(@Nonnull final ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "AopAnnotationExpression";
  }

  @Nullable
  @RequiredReadAction
  public AopReferenceHolder getAnnotationPattern() {
    return findChildByClass(AopReferenceHolder.class);
  }

  @Override
  @RequiredReadAction
  public final Collection<AopPsiTypePattern> getPatterns() {
    AopReferenceHolder holder = getAnnotationPattern();
    return holder == null ? Collections.<AopPsiTypePattern>emptyList() : holder.getPatterns();
  }
}