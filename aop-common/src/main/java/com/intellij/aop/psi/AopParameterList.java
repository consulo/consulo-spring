/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiParameter;
import com.intellij.java.language.psi.PsiParameterList;
import com.intellij.java.language.psi.PsiType;
import consulo.language.ast.ASTNode;
import consulo.util.lang.function.PairFunction;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
public class AopParameterList extends AopAbstractList<PsiParameter> {

  public AopParameterList(@Nonnull ASTNode node) {
    super(node);
  }

  protected PsiType getPsiType(@Nonnull PsiParameter psiParameter) {
    return psiParameter.getType();
  }

  public String toString() {
    return "AopParameterList";
  }

  public PointcutMatchDegree matches(PointcutContext context, PsiParameterList list, PairFunction<PsiType, AopReferenceTarget, PointcutMatchDegree> matcher) {
    return accepts(context, list.getParameters(), matcher);
  }

}
