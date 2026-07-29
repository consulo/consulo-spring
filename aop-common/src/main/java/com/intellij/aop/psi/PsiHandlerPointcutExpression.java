/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiMember;
import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiHandlerPointcutExpression extends PsiTypedPointcutExpression {
    public PsiHandlerPointcutExpression(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "PsiHandlerPointcutExpression";
    }

    @Nonnull
    @Override
    public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
        return PointcutMatchDegree.FALSE;
    }

    @Nonnull
    @Override
    public Collection<AopPsiTypePattern> getPatterns() {
        return Arrays.asList(AopPsiTypePattern.FALSE);
    }
}