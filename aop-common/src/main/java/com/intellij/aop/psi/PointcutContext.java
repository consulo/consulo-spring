/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author peter
 */
public class PointcutContext {
  private final Map<String,AopReferenceTarget> myMap = new HashMap<String, AopReferenceTarget>();

  public PointcutContext() {
  }

  public PointcutContext(@Nullable PsiPointcutExpression expression) {
    this(expression == null ? null : expression.getContainingFile().getAopModel().getPointcutMethod());
  }

  public PointcutContext(@Nullable PsiMethod method) {
    if (method != null) {
      for (final PsiParameter parameter : method.getParameterList().getParameters()) {
        final String paramName = parameter.getName();
        if (paramName != null) {
          addParameter(paramName, new AopParameterReferenceTarget(parameter));
        }
      }
    }
  }

  private AopReferenceTarget getParameter(String paramName) {
    return myMap.get(paramName);
  }

  public void addParameter(@Nonnull String paramName, AopReferenceTarget holder) {
    myMap.put(paramName, holder);
  }

  @Nonnull
  public AopReferenceTarget resolve(@Nonnull AopReferenceHolder pattern) {
    final AopTypeExpression typeExpression = pattern.getTypeExpression();
    if (typeExpression instanceof AopReferenceExpression) {
      final AopReferenceTarget target = getParameter(pattern.getText());
      if (target != null) {
        return target;
      }
    }
    return pattern;
  }

}
