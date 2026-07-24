/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopBundle;
import consulo.language.pratt.MutableMarker;
import consulo.language.pratt.PrattBuilder;
import org.jetbrains.annotations.NonNls;

import static com.intellij.aop.psi.AopElementTypes.*;
import static com.intellij.aop.psi.AopPrattParser.TYPE_PATTERN;

/**
 * @author peter
*/
public abstract class FieldPointcutDescriptor extends PointcutDescriptor{

protected FieldPointcutDescriptor(@NonNls String tokenText) {
  super(tokenText);
}

 public void parseToken(PrattBuilder builder) {
   if (builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("))) {
     MethodPointcutDescriptor.parseAnnotationsWithModifiers(builder);

     MutableMarker type = builder.mark();
     builder.parseChildren(TYPE_PATTERN, AopBundle.message("error.type.name.pattern.expected"));
     type.finish(AOP_REFERENCE_HOLDER);

     MutableMarker fieldName = builder.mark();
     builder.parseChildren(TYPE_PATTERN, AopBundle.message("error.field.name.pattern.expected"));
     fieldName.finish(AOP_MEMBER_REFERENCE_EXPRESSION);
     
     builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
   }

  }
}