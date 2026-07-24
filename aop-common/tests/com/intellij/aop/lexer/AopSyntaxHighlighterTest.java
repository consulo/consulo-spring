/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.lexer;

import com.intellij.aop.AopLiteFixture;
import com.intellij.aop.psi.AopElementTypes;
import com.intellij.aop.psi.AopSyntaxHighlighter;
import consulo.language.ast.TokenSet;
import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.IElementType;

/**
 * @author peter
 */
public class AopSyntaxHighlighterTest extends AopLiteFixture implements AopElementTypes {

  protected void setUp() throws Exception {
    super.setUp();
    initApplication();
  }

  public void testLexer() throws Throwable {
    assertInstanceOf(new AopSyntaxHighlighter().getHighlightingLexer(), AopLexer.class);
  }

  public void testTokenHighlights() throws Throwable {
    AopSyntaxHighlighter highlighter = new AopSyntaxHighlighter();
    
    assertOrderedEquals(highlighter.getTokenHighlights(AOP_THROWS), AopSyntaxHighlighter.AOP_KEYWORD);
    assertOrderedEquals(highlighter.getTokenHighlights(AOP_MODIFIER), AopSyntaxHighlighter.AOP_KEYWORD);
    assertOrderedEquals(highlighter.getTokenHighlights(AOP_BOOLEAN_LITERAL), AopSyntaxHighlighter.AOP_KEYWORD);
    assertTokenHighlights(highlighter, AOP_PRIMITIVE_TYPES, AopSyntaxHighlighter.AOP_KEYWORD);

    assertOrderedEquals(highlighter.getTokenHighlights(AOP_DOT), AopSyntaxHighlighter.AOP_DOT);
    assertOrderedEquals(highlighter.getTokenHighlights(AOP_DOT_DOT), AopSyntaxHighlighter.AOP_DOT);
    assertOrderedEquals(highlighter.getTokenHighlights(AOP_VARARGS), AopSyntaxHighlighter.AOP_DOT);

    assertOrderedEquals(highlighter.getTokenHighlights(AOP_LEFT_PAR), AopSyntaxHighlighter.AOP_PARENTHS);
    assertOrderedEquals(highlighter.getTokenHighlights(AOP_RIGHT_PAR), AopSyntaxHighlighter.AOP_PARENTHS);
    assertOrderedEquals(highlighter.getTokenHighlights(AOP_LT), AopSyntaxHighlighter.AOP_PARENTHS);
    assertOrderedEquals(highlighter.getTokenHighlights(AOP_GT), AopSyntaxHighlighter.AOP_PARENTHS);

    assertOrderedEquals(highlighter.getTokenHighlights(AOP_IDENTIFIER), AopSyntaxHighlighter.AOP_IDENTIFIER);

    assertTokenHighlights(highlighter, AOP_LOGICAL_OPS, AopSyntaxHighlighter.AOP_OPERATION_SIGN);
    assertOrderedEquals(highlighter.getTokenHighlights(AOP_ASTERISK), AopSyntaxHighlighter.AOP_OPERATION_SIGN);

    assertOrderedEquals(highlighter.getTokenHighlights(null));

  }

  private static void assertTokenHighlights(AopSyntaxHighlighter highlighter, TokenSet tokenSet, TextAttributesKey expected) {
    for (IElementType type : tokenSet.getTypes()) {
      assertOrderedEquals(highlighter.getTokenHighlights(type), expected);
    }
  }

}
