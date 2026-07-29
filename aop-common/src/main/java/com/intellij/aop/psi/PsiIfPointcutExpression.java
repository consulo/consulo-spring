/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiMember;
import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class PsiIfPointcutExpression extends AopElementBase implements PsiPointcutExpression {
    public PsiIfPointcutExpression(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "PsiIfPointcutExpression";
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