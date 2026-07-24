/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiType;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
public class ConcatenationPattern extends AopPsiTypePattern{
  private final AopPsiTypePattern myLeft;
  private final AopPsiTypePattern myRight;
  private final boolean myDoubleDot;

  public ConcatenationPattern(AopPsiTypePattern left, AopPsiTypePattern right, boolean doubleDot) {
    myLeft = left;
    myRight = right;
    myDoubleDot = doubleDot;
  }

  public boolean isDoubleDot() {
    return myDoubleDot;
  }

  public AopPsiTypePattern getLeft() {
    return myLeft;
  }

  public AopPsiTypePattern getRight() {
    return myRight;
  }

  public boolean accepts(@Nonnull PsiType type) {
    if (type instanceof PsiClassType) {
      PsiClassType classType = (PsiClassType)type;
      PsiClass psiClass = classType.resolve();
      if (psiClass != null) {
        String qname = psiClass.getQualifiedName();
        if (qname != null && accepts(qname)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean accepts(@Nonnull String qualifiedName) {
    String[] strings = qualifiedName.split("\\.");
    int[] indices = new int[strings.length];
    indices[0] = -1;
    for (int i = 1; i < indices.length; i++) {
      indices[i] = indices[i - 1] + strings[i - 1].length() + 1;
    }
    boolean[] rights = new boolean[strings.length];
    if (myDoubleDot) {
      for (int i = 1; i < indices.length; i++) {
        rights[i] = myRight.accepts(qualifiedName.substring(indices[i] + 1));
      }
    }

    for (int i = 1; i < indices.length; i++) {
      int index = indices[i];
      if (myLeft.accepts(qualifiedName.substring(0, index))) {
        if (myDoubleDot) {
          for (int j = i; j < indices.length; j++) {
            if (rights[j]) return true;
          }
        } else {
          if (myRight.accepts(qualifiedName.substring(index + 1))) return true;
        }
      }
    }
    return false;
  }
}
