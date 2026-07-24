/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.lexer.AopLexer;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
@ExtensionImpl
public class AopPointcutExpressionParserDefinition implements ParserDefinition, AopElementTypes {
  private static final TokenSet WHITE_SPACES = TokenSet.create(AopElementTypes.WHITE_SPACE);

  @Nonnull
  @Override
  public Language getLanguage() {
    return AopPointcutExpressionLanguage.INSTANCE;
  }

  @Nonnull
  public Lexer createLexer(@Nonnull LanguageVersion languageVersion) {
    return new AopLexer();
  }

  @Nonnull
  public IFileElementType getFileNodeType() {
    return AOP_POINTCUT_EXPRESSION_FILE;
  }

  @Nonnull
  public TokenSet getWhitespaceTokens(@Nonnull LanguageVersion languageVersion) {
    return WHITE_SPACES;
  }

  @Nonnull
  public TokenSet getCommentTokens(@Nonnull LanguageVersion languageVersion) {
    return TokenSet.EMPTY;
  }

  @Nonnull
  public TokenSet getStringLiteralElements(@Nonnull LanguageVersion languageVersion) {
    return TokenSet.EMPTY;
  }

  @Nonnull
  public PsiParser createParser(@Nonnull LanguageVersion languageVersion) {
    return new AopPrattParser();
  }

  @Nonnull
  public PsiElement createElement(ASTNode node) {
    IElementType elementType = node.getElementType();
    if (elementType == AOP_ANNOTATION_EXPRESSION) return new AopAnnotationExpression(node);
    if (elementType == AOP_ANNOTATION_HOLDER) return new AopAnnotationHolder(node);
    if (elementType == AOP_ANNOTATION_VALUES) return new AopElementBase(node) {
      @Override
      public String toString() {
        return "AopAnnotationValues";
      }
    };
    if (elementType == AOP_ANNOTATED_TYPE_EXPRESSION) return new AopAnnotatedTypeExpression(node);

    if (elementType == AOP_POINTCUT_REFERENCE) return new PsiPointcutReferenceExpression(node);
    if (elementType == AOP_MEMBER_REFERENCE_EXPRESSION) return new AopMemberReferenceExpression(node);
    if (elementType == AOP_CONSTRUCTOR_REFERENCE_EXPRESSION) return new AopConstructorReferenceExpression(node);

    if (elementType == AOP_REFERENCE_EXPRESSION) return new AopReferenceExpression(node);
    if (elementType == AOP_REFERENCE_HOLDER) return new AopReferenceHolder(node);
    if (elementType == AOP_ARRAY_EXPRESSION) return new AopArrayExpression(node);
    if (elementType == AOP_GENERIC_TYPE_EXPRESSION) return new AopGenericTypeExpression(node);
    if (elementType == AOP_SUBTYPE_EXPRESSION) return new AopSubtypeExpression(node);
    if (elementType == AOP_WILDCARD_EXPRESSION) return new AopWildcardExpression(node);
    if (elementType == AOP_PRIMITIVE_TYPE_EXPRESSION) return new AopPrimitiveTypeExpression(node);

    if (elementType == AOP_PARAMETER_LIST) return new AopParameterList(node);
    if (elementType == AOP_TYPE_PARAMETER_LIST) return new AopTypeParameterList(node);
    if (elementType == AOP_THROWS_LIST) return new AopThrowsList(node);
    if (elementType == AOP_MODIFIER_LIST) return new AopModifierList(node);

    if (elementType == AOP_BINARY_EXPRESSION || elementType == AOP_POINTCUT_BINARY_EXPRESSION) return new AopBinaryExpression(node);
    if (elementType == AOP_NOT_EXPRESSION || elementType == AOP_POINTCUT_NOT_EXPRESSION) return new AopNotExpression(node);
    if (elementType == AOP_PARENTHESIZED_EXPRESSION || elementType == AOP_POINTCUT_PARENTHESIZED_EXPRESSION)
      return new AopParenthesizedExpression(node);

    if (elementType instanceof AopPointcutElementType) {
      return ((AopPointcutElementType)elementType).createPsi(node);
    }

    throw new UnsupportedOperationException(elementType.toString());
  }

  @Nonnull
  public PsiFile createFile(@Nonnull FileViewProvider viewProvider) {
    return new AopPointcutExpressionFile(viewProvider);
  }

  @Nonnull
  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return ParserDefinition.SpaceRequirements.MAY;
  }

}
