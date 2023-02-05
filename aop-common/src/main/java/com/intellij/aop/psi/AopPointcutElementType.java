/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import consulo.language.ast.ASTNode;
import org.jetbrains.annotations.NonNls;
import javax.annotation.Nonnull;

/**
 * @author peter
 */
public abstract class AopPointcutElementType extends AopElementType{
  public AopPointcutElementType(@Nonnull @NonNls String debugName) {
    super(debugName);
  }

  public abstract PsiPointcutExpression createPsi(ASTNode node);
}
