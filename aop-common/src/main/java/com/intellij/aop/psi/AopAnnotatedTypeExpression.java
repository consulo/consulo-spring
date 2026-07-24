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
import java.util.HashSet;
import java.util.Set;

/**
 * @author peter
 */
public class AopAnnotatedTypeExpression extends AopElementBase implements AopTypeExpression{
  public AopAnnotatedTypeExpression(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "AopAnnotatedTypeExpression";
  }

  @Nullable
  @RequiredReadAction
  public AopTypeExpression getTypeExpression() {
    return findChildByClass(AopTypeExpression.class);
  }

  @Nullable
  @RequiredReadAction
  public AopAnnotationHolder getAnnotationHolder() {
    return findChildByClass(AopAnnotationHolder.class);
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public Collection<AopPsiTypePattern> getPatterns() {
    return getPatterns(getTypeExpression());
  }

  @Override
  public String getTypePattern() {
    return "'_";
  }

  @Nonnull
  @RequiredReadAction
  public Collection<AopPsiTypePattern> getQualifierPatterns() {
    if (!(getTypeExpression() instanceof AopReferenceExpression refExpr)) return Collections.emptyList();

    return getPatterns(refExpr.getGeneralizedQualifier());
  }

  @RequiredReadAction
  private Collection<AopPsiTypePattern> getPatterns(AopTypeExpression typeExpression) {
    if (typeExpression == null) return Collections.emptyList();

    AopAnnotationHolder annotationHolder = getAnnotationHolder();
    if (annotationHolder == null) return Collections.emptyList();

    Collection<AopPsiTypePattern> typePatterns = typeExpression.getPatterns();
    Collection<AopPsiTypePattern> annoPatterns = annotationHolder.getPatterns();
    Set<AopPsiTypePattern> result = new HashSet<>();
    AopBinaryExpression.conjunctPatterns(typePatterns, annoPatterns, result);
    return result;
  }
}