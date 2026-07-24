/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiModifierListOwner;
import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
public class AopModifierList extends AopElementBase {

  public AopModifierList(@Nonnull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "AopModifierList";
  }

  public boolean accepts(PsiModifierListOwner owner) {
    for (ASTNode node : getNode().getChildren(null)) {
      if (node.getElementType() == AopElementTypes.AOP_MODIFIER &&  !owner.hasModifierProperty(node.getText())) return false;
    }

    for (AopNotExpression expression : findChildrenByClass(AopNotExpression.class)) {
      if (owner.hasModifierProperty(expression.getLastChild().getNode().getText())) return false;
    }

    return true;
  }
}