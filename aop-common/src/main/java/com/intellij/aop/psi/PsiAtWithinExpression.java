/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiMember;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author peter
 */
public class PsiAtWithinExpression extends PsiTypedPointcutExpression implements PsiAtPointcutDesignator {
    public PsiAtWithinExpression(@Nonnull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "PsiAtWithinExpression";
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public PointcutMatchDegree acceptsSubject(PointcutContext context, PsiMember member) {
        //todo this is only Spring-specific!
        return PsiAtArgsExpression.canHaveAnnotation(
            member.getContainingClass(),
            getTypeReference(),
            context,
            PointcutMatchDegree.TRUE,
            PointcutMatchDegree.FALSE
        );
    }

    @Nonnull
    @Override
    public Collection<AopPsiTypePattern> getPatterns() {
        return Arrays.asList(AopPsiTypePattern.TRUE);
    }
}