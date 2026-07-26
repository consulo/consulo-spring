/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.Arrays;

/**
 * @author peter
 */
public abstract class MethodPatternPointcut extends AopElementBase implements PsiPointcutExpression {
    public MethodPatternPointcut(@Nonnull ASTNode node) {
        super(node);
    }

    @Nullable
    @RequiredReadAction
    public AopReferenceHolder getReturnType() {
        return findChildByClass(AopReferenceHolder.class);
    }

    @Nullable
    @RequiredReadAction
    public AopModifierList getModifierList() {
        return findChildByClass(AopModifierList.class);
    }

    @Nullable
    @RequiredReadAction
    public AopMemberReferenceExpression getMethodReference() {
        return findChildByClass(AopMemberReferenceExpression.class);
    }

    @Nullable
    @RequiredReadAction
    public AopParameterList getParameterList() {
        return findChildByClass(AopParameterList.class);
    }

    @Nullable
    @RequiredReadAction
    public AopThrowsList getThrowsList() {
        return findChildByClass(AopThrowsList.class);
    }

    @Nullable
    @RequiredReadAction
    public AopAnnotationHolder getAnnotationHolder() {
        return findChildByClass(AopAnnotationHolder.class);
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public Collection<AopPsiTypePattern> getPatterns() {
        AopMemberReferenceExpression methodReference = getMethodReference();
        if (methodReference == null) {
            return Arrays.asList(AopPsiTypePattern.FALSE);
        }
        return methodReference.getQualifierPatterns();
    }
}
