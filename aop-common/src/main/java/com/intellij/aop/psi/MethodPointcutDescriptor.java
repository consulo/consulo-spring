/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import consulo.aop.localize.AopLocalize;
import consulo.language.ast.IElementType;
import consulo.language.pratt.MutableMarker;
import consulo.language.pratt.PrattBuilder;
import consulo.localize.LocalizeValue;
import org.jetbrains.annotations.NonNls;

import static com.intellij.aop.psi.AopElementTypes.*;
import static com.intellij.aop.psi.AopPrattParser.TYPE_PATTERN;
import static com.intellij.aop.psi.AopPrattParser.parseAnnotations;

/**
 * @author peter
 */
public abstract class MethodPointcutDescriptor extends PointcutDescriptor {
    private final boolean myConstructorOnly;

    protected MethodPointcutDescriptor(@NonNls String tokenText, boolean constructorOnly) {
        super(tokenText);
        myConstructorOnly = constructorOnly;
    }

    @Override
    public void parseToken(PrattBuilder builder) {
        if (builder.assertToken(AOP_LEFT_PAR, AopLocalize.error0Expected("(").get())) {
            parseAnnotationsWithModifiers(builder);

            MutableMarker type = builder.mark();
            LocalizeValue message = myConstructorOnly
                ? AopLocalize.errorConstructorPatternExpected()
                : AopLocalize.errorMethodReturnTypeExpected();
            IElementType result = builder.parseChildren(TYPE_PATTERN, message.get());
            boolean isConstructor = result == AOP_CONSTRUCTOR_REFERENCE_EXPRESSION;
            if (!isConstructor) {
                if (myConstructorOnly) {
                    builder.error(AopLocalize.error0Expected(".new").get());
                    type.finish(AOP_CONSTRUCTOR_REFERENCE_EXPRESSION);
                }
                else {
                    type.finish(AOP_REFERENCE_HOLDER);
                }
            }
            else {
                type.drop();
            }

            if (!isConstructor && !myConstructorOnly) {
                MutableMarker methodRef = builder.mark();
                builder.parseChildren(TYPE_PATTERN, AopLocalize.errorMethodNamePatternExpected().get());
                methodRef.finish(AOP_MEMBER_REFERENCE_EXPRESSION);
            }

            if (builder.assertToken(AOP_LEFT_PAR, AopLocalize.error0Expected("(").get())) {
                AopPointcutTypes.parseParameterList(builder, TYPE_PATTERN, AopLocalize.errorMethodArgsPatternExpected().get());

                if (builder.assertToken(AOP_RIGHT_PAR, AopLocalize.error0Expected(")").get())) {
                    if (builder.isToken(AOP_THROWS)) {
                        MutableMarker throwsList = builder.mark();
                        builder.advance();
                        while (true) {
                            MutableMarker exc = builder.mark();
                            builder.parseChildren(TYPE_PATTERN, AopLocalize.errorTypeNamePatternExpected().get());
                            exc.finish(AOP_REFERENCE_HOLDER);
                            if (!builder.checkToken(AOP_COMMA)) {
                                break;
                            }
                        }
                        throwsList.finish(AOP_THROWS_LIST);
                    }
                }
            }
            builder.assertToken(AOP_RIGHT_PAR, AopLocalize.error0Expected(")").get());
        }
    }

    public static void parseAnnotationsWithModifiers(PrattBuilder builder) {
        if (builder.isToken(AOP_AT) || builder.isToken(AOP_NOT)) {
            parseAnnotations(builder);
        }

        MutableMarker modList = builder.mark();
        while (true) {
            if (AOP_NOT == builder.getTokenType()) {
                MutableMarker not = builder.mark();
                builder.advance();
                if (builder.isToken(AopElementTypes.AOP_MODIFIER)) {
                    builder.advance();
                    not.finish(AOP_NOT_EXPRESSION);
                }
                else {
                    not.rollback();
                    break;
                }
            }
            else if (builder.isToken(AopElementTypes.AOP_MODIFIER)) {
                builder.advance();
            }
            else {
                break;
            }
        }
        modList.finish(AOP_MODIFIER_LIST);
    }
}
