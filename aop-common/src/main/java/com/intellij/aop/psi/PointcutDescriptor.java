/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import consulo.language.ast.ASTNode;
import consulo.language.pratt.PrattBuilder;
import org.jetbrains.annotations.NonNls;

/**
 * @author peter
 */
public abstract class PointcutDescriptor {
  private final String myTokenText;

  protected PointcutDescriptor(@NonNls String tokenText) {
    myTokenText = tokenText;
  }

  public String getTokenText() {
    return myTokenText;
  }

  public abstract void parseToken(PrattBuilder builder);

  public abstract PsiPointcutExpression createPsi(ASTNode node);
}
