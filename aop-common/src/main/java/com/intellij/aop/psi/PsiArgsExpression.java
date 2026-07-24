/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.java.language.impl.psi.impl.PsiElementFactoryImpl;
import com.intellij.java.language.psi.*;
import consulo.language.ast.ASTNode;
import consulo.util.lang.Comparing;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiArgsExpression extends AopElementBase implements PsiPointcutExpression{
  private static final TypeArgumentMatcher ARGS_MATCHER = new TypeArgumentMatcher() {
      public PointcutMatchDegree fun(PsiType actualType, AopReferenceTarget target) {
        if (super.fun(actualType, target) == PointcutMatchDegree.TRUE || target.isAssignableFrom(actualType)) {
          return PointcutMatchDegree.TRUE;
        }

        String typeText = target.getQualifiedName();
        PsiPrimitiveType primitiveType = PsiElementFactoryImpl.getPrimitiveType(typeText);
        if (primitiveType != null && !(actualType instanceof PsiPrimitiveType)) {
          if (primitiveType == PsiPrimitiveType.getUnboxedType(actualType)) return PointcutMatchDegree.TRUE;
        }
        else if (actualType instanceof PsiPrimitiveType) {
          return PointcutMatchDegree.valueOf(Comparing.equal(((PsiPrimitiveType)actualType).getBoxedTypeName(), typeText) ||
                                             CommonClassNames.JAVA_LANG_NUMBER.equals(typeText) ||
                                             CommonClassNames.JAVA_LANG_OBJECT.equals(typeText));
        }
        return PointcutMatchDegree.FALSE;
      }
    };

  public PsiArgsExpression(@Nonnull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiArgsExpression";
  }

  @Nullable
  public AopParameterList getParameterList() {
    return findChildByClass(AopParameterList.class);
  }

  @Nonnull
  public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
    if (member instanceof PsiMethod) {
      AopParameterList parameterList = getParameterList();
      return parameterList != null ? parameterList.matches(context, ((PsiMethod)member).getParameterList(), ARGS_MATCHER) : PointcutMatchDegree.FALSE;
    }
    return PointcutMatchDegree.FALSE;
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Arrays.asList(AopPsiTypePattern.TRUE);
  }
}