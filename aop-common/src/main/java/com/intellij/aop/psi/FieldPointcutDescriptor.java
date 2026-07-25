/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import consulo.aop.localize.AopLocalize;
import consulo.language.pratt.MutableMarker;
import consulo.language.pratt.PrattBuilder;

import static com.intellij.aop.psi.AopElementTypes.*;
import static com.intellij.aop.psi.AopPrattParser.TYPE_PATTERN;

/**
 * @author peter
 */
public abstract class FieldPointcutDescriptor extends PointcutDescriptor {
    protected FieldPointcutDescriptor(String tokenText) {
        super(tokenText);
    }

    @Override
    public void parseToken(PrattBuilder builder) {
        if (builder.assertToken(AOP_LEFT_PAR, AopLocalize.error0Expected("(").get())) {
            MethodPointcutDescriptor.parseAnnotationsWithModifiers(builder);

            MutableMarker type = builder.mark();
            builder.parseChildren(TYPE_PATTERN, AopLocalize.errorTypeNamePatternExpected().get());
            type.finish(AOP_REFERENCE_HOLDER);

            MutableMarker fieldName = builder.mark();
            builder.parseChildren(TYPE_PATTERN, AopLocalize.errorFieldNamePatternExpected().get());
            fieldName.finish(AOP_MEMBER_REFERENCE_EXPRESSION);

            builder.assertToken(AOP_RIGHT_PAR, AopLocalize.error0Expected(")").get());
        }
    }
}
