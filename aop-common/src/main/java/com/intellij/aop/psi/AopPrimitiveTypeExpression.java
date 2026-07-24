/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiPrimitiveType;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class AopPrimitiveTypeExpression extends AopElementBase implements AopTypeExpression{
  public AopPrimitiveTypeExpression(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "AopPrimitiveTypeExpression";
  }

  @Nonnull
  @Override
  @RequiredReadAction
  public Collection<AopPsiTypePattern> getPatterns() {
    PsiPrimitiveType psiType = getPsiType();
    return psiType == null ? Collections.<AopPsiTypePattern>emptyList() : Collections.singletonList((AopPsiTypePattern) new PsiPrimitiveTypePattern(psiType)); 

  }

  @Override
  @RequiredReadAction
  public String getTypePattern() {
    return getText();
  }

  @Nullable
  @RequiredReadAction
  public PsiPrimitiveType getPsiType() {
    return JavaPsiFacade.getInstance(getProject()).getElementFactory().createPrimitiveType(getText());
  }
}