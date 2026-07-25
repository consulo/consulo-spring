/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public interface AopTypeExpression extends AopPatternContainer {
    @Override
    AopPointcutExpressionFile getContainingFile();

    @Nullable
    String getTypePattern();
}
