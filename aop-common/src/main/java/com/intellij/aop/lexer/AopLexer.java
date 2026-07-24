/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.lexer;

import com.intellij.aop.psi.AopElementType;
import com.intellij.aop.psi.AopElementTypes;
import com.intellij.aop.psi.AopPointcutTypes;
import consulo.language.ast.TokenSet;
import consulo.language.lexer.LexerPosition;
import consulo.language.ast.IElementType;
import consulo.language.lexer.DelegateLexer;
import consulo.language.lexer.FlexAdapter;
import consulo.language.lexer.MergingLexerAdapter;
import org.jetbrains.annotations.NonNls;

import java.util.HashMap;
import java.util.Map;

/**
 * @author peter
 */
public class AopLexer extends DelegateLexer implements AopElementTypes {
  @NonNls public static final Map<String,AopElementType> PRIMITIVE_TYPES = new HashMap<String, AopElementType>();
  @NonNls public static final Map<String,AopElementType> LOGICAL_OP_TYPES = new HashMap<String, AopElementType>();

  static {
    PRIMITIVE_TYPES.put("int", AOP_INT);
    PRIMITIVE_TYPES.put("char", AOP_CHAR);
    PRIMITIVE_TYPES.put("byte", AOP_BYTE);
    PRIMITIVE_TYPES.put("short", AOP_SHORT);
    PRIMITIVE_TYPES.put("long", AOP_LONG);
    PRIMITIVE_TYPES.put("double", AOP_DOUBLE);
    PRIMITIVE_TYPES.put("float", AOP_FLOAT);
    PRIMITIVE_TYPES.put("boolean", AOP_BOOLEAN);
    PRIMITIVE_TYPES.put("void", AOP_VOID);

    LOGICAL_OP_TYPES.put("and", AOP_AND);
    LOGICAL_OP_TYPES.put("or", AOP_OR);
    LOGICAL_OP_TYPES.put("not", AOP_NOT);
  }

  private int myInsideExecution = 0;
  private int myInsideIf = 0;

  public AopLexer() {
    super(new MergingLexerAdapter(new FlexAdapter(new _AopLexer()), TokenSet.EMPTY));
  }

  public IElementType getTokenType() {
    IElementType tokenType = super.getTokenType();
    if (tokenType == null) return tokenType;

    if (AOP_LOGICAL_OPS.contains(tokenType)) {
      return tokenType;
    }

    if (myInsideExecution > 0) {
      if (AOP_LEFT_PAR.equals(tokenType)) {
        myInsideExecution++;
      } else if (AOP_RIGHT_PAR.equals(tokenType)) {
        if (myInsideIf > 0) myInsideIf = 0;

        if (myInsideExecution == 2) myInsideExecution = 0;
        else if (myInsideExecution == 3) myInsideExecution = 2;
      }
    }

    if (AOP_IDENTIFIER == tokenType) {
      @NonNls String text = getTokenText();
      AopElementType primType = PRIMITIVE_TYPES.get(text);
      if (primType != null) return primType;

      if (_AopLexer.PATH_ELEMENT != getState()) {
        AopElementType pointcutType = AopPointcutTypes.getPointcutTokens().get(text);
        if (pointcutType != null) {
          LexerPosition position = getDelegate().getCurrentPosition();
          try {
            advance();
            while (WHITE_SPACE == super.getTokenType()) advance();
            if (AOP_LEFT_PAR != super.getTokenType() && null != super.getTokenType() || myInsideExecution > 0) {
              return tokenType;
            }
            if (AopPointcutTypes.canContainModifiers(text) && myInsideExecution == 0) {
              myInsideExecution = 1;
            } else {
              myInsideExecution = 0;
            }
            if ("if".equals(text)) {
              myInsideIf = 1;
            }
            return pointcutType;
          }
          finally {
            getDelegate().restore(position);
          }
        }

        if (myInsideIf > 0) {
          if ("true".equals(text) || "false".equals(text)) return AOP_BOOLEAN_LITERAL;
        }
      }

      AopElementType logType = LOGICAL_OP_TYPES.get(text);
      if (logType != null) {
        return logType;
      }
    }
    return tokenType;
  }
}
