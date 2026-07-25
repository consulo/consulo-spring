/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiMember;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class FieldPatternPointcut extends AopElementBase implements PsiPointcutExpression {
  public FieldPatternPointcut(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "FieldPatternPointcut";
  }

  @Nullable
  @RequiredReadAction
  public AopModifierList getModifierList() {
    return findChildByClass(AopModifierList.class);
  }

  @Nullable
  @RequiredReadAction
  public AopAnnotationHolder getAnnotationHolder() {
    return findChildByClass(AopAnnotationHolder.class);
  }

  @Nonnull
  @Override
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    return PointcutMatchDegree.FALSE;
  }

  @Nonnull
  @Override
  public Collection<AopPsiTypePattern> getPatterns() {
    return Collections.emptyList();
  }
}