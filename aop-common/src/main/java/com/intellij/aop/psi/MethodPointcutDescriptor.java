/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopBundle;
import consulo.language.ast.IElementType;
import consulo.language.pratt.MutableMarker;
import consulo.language.pratt.PrattBuilder;
import org.jetbrains.annotations.NonNls;

import static com.intellij.aop.psi.AopElementTypes.*;
import static com.intellij.aop.psi.AopPrattParser.TYPE_PATTERN;
import static com.intellij.aop.psi.AopPrattParser.parseAnnotations;

/**
 * @author peter
*/
public abstract class MethodPointcutDescriptor extends PointcutDescriptor{
  private final boolean myConstructorOnly;

  protected MethodPointcutDescriptor(@NonNls String tokenText, boolean constructorOnly) {
    super(tokenText);
    myConstructorOnly = constructorOnly;
  }

   public void parseToken(PrattBuilder builder) {
     if (builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("))) {
       parseAnnotationsWithModifiers(builder);

       MutableMarker type = builder.mark();
       String message = myConstructorOnly
                              ? AopBundle.message("error.constructor.pattern.expected")
                              : AopBundle.message("error.method.return.type.expected");
       IElementType result = builder.parseChildren(TYPE_PATTERN, message);
       boolean isConstructor = result == AOP_CONSTRUCTOR_REFERENCE_EXPRESSION;
       if (!isConstructor) {
         if (myConstructorOnly) {
           builder.error(AopBundle.message("error.0.expected", ".new"));
           type.finish(AOP_CONSTRUCTOR_REFERENCE_EXPRESSION);
         } else {
           type.finish(AOP_REFERENCE_HOLDER);
         }
       } else {
         type.drop();
       }

       if (!isConstructor && !myConstructorOnly) {
         MutableMarker methodRef = builder.mark();
         builder.parseChildren(TYPE_PATTERN, AopBundle.message("error.method.name.pattern.expected"));
         methodRef.finish(AOP_MEMBER_REFERENCE_EXPRESSION);
       }

       if (builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("))) {
         AopPointcutTypes.parseParameterList(builder, TYPE_PATTERN, AopBundle.message("error.method.args.pattern.expected"));

         if (builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"))) {
           if (builder.isToken(AOP_THROWS)) {
             MutableMarker throwsList = builder.mark();
             builder.advance();
             while (true) {
               MutableMarker exc = builder.mark();
               builder.parseChildren(TYPE_PATTERN, AopBundle.message("error.type.name.pattern.expected"));
               exc.finish(AOP_REFERENCE_HOLDER);
               if (!builder.checkToken(AOP_COMMA)) break;
             }
             throwsList.finish(AOP_THROWS_LIST);
           }
         }
       }
       builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
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
        } else {
          not.rollback();
          break;
        }
      } else if (builder.isToken(AopElementTypes.AOP_MODIFIER)) {
        builder.advance();
      } else {
        break;
      }
    }
    modList.finish(AOP_MODIFIER_LIST);
  }
}
