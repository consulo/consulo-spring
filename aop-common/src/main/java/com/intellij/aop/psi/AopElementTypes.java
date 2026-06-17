/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.aop.psi;

import consulo.language.ast.*;
import jakarta.annotation.Nonnull;

import consulo.language.impl.psi.PsiWhiteSpaceImpl;

/**
 * @author peter
 */
public interface AopElementTypes extends StandardTokenTypes {
  AopElementType AOP_ASTERISK = new AopElementType("AOP_ASTERISK");//*
  AopElementType AOP_DOT = new AopElementType("AOP_DOT");//.
  AopElementType AOP_DOT_DOT = new AopElementType("AOP_DOT_DOT");//..
  AopElementType AOP_LEFT_PAR = new AopElementType("AOP_LEFT_PAR");
  AopElementType AOP_LEFT_BRACE = new AopElementType("AOP_LEFT_BRACE");
  AopElementType AOP_RIGHT_PAR = new AopElementType("AOP_RIGHT_PAR");
  AopElementType AOP_RIGHT_BRACE = new AopElementType("AOP_RIGHT_BRACE");
  AopElementType AOP_LT = new AopElementType("AOP_LT");
  AopElementType AOP_GT = new AopElementType("AOP_GT");
  AopElementType AOP_COMMA = new AopElementType("AOP_COMMA");

  AopElementType ANNO_WHITE_SPACE = new AopWhitespaceElementType();

  AopElementType AOP_NOT = new AopElementType("AOP_NOT");
  AopElementType AOP_AND = new AopElementType("AOP_AND");
  AopElementType AOP_OR = new AopElementType("AOP_OR");
  TokenSet AOP_LOGICAL_OPS = TokenSet.create(AOP_NOT, AOP_AND, AOP_OR);

  AopElementType AOP_IDENTIFIER = new AopElementType("AOP_IDENTIFIER");

  TokenSet AOP_DOT_ONLY = TokenSet.create(AOP_DOT);
  TokenSet AOP_DOTS = TokenSet.create(AOP_DOT, AOP_DOT_DOT);

  TokenSet AOP_ID_ONLY = TokenSet.create(AOP_IDENTIFIER);
  TokenSet AOP_SIMPLE_NAME_PATTERN = TokenSet.create(AOP_IDENTIFIER, AOP_ASTERISK);

  AopElementType AOP_PLUS = new AopElementType("AOP_PLUS");
  AopElementType AOP_BRACES = new AopElementType("AOP_BRACES"); //[]
  AopElementType AOP_AT = new AopElementType("AOP_AT"); //@
  AopElementType AOP_QUESTION = new AopElementType("AOP_QUESTION"); //?
  AopElementType AOP_VARARGS = new AopElementType("AOP_VARARGS"); //...

  AopElementType AOP_NEW = new AopElementType("AOP_NEW"); //new
  AopElementType AOP_BOOLEAN_LITERAL = new AopElementType("AOP_BOOLEAN_LITERAL"); //true, false

  AopElementType AOP_THROWS = new AopElementType("AOP_THROWS");

  AopElementType AOP_VOID = new AopElementType("AOP_VOID");
  AopElementType AOP_INT = new AopElementType("AOP_INT");
  AopElementType AOP_BYTE = new AopElementType("AOP_BYTE");
  AopElementType AOP_CHAR = new AopElementType("AOP_CHAR");
  AopElementType AOP_LONG = new AopElementType("AOP_LONG");
  AopElementType AOP_SHORT = new AopElementType("AOP_SHORT");
  AopElementType AOP_DOUBLE = new AopElementType("AOP_DOUBLE");
  AopElementType AOP_FLOAT = new AopElementType("AOP_FLOAT");
  AopElementType AOP_BOOLEAN = new AopElementType("AOP_BOOLEAN");
  TokenSet AOP_PRIMITIVE_TYPES = TokenSet.create(AOP_VOID, AOP_INT, AOP_BYTE, AOP_CHAR, AOP_LONG, AOP_SHORT, AOP_DOUBLE, AOP_FLOAT, AOP_BOOLEAN);

  AopElementType AOP_EXTENDS = new AopElementType("AOP_EXTENDS");
  AopElementType AOP_SUPER = new AopElementType("AOP_SUPER");

  AopElementType AOP_MODIFIER = new AopElementType("AOP_MODIFIER");

  IFileElementType AOP_POINTCUT_EXPRESSION_FILE = new IFileElementType("AOP_POINTCUT_EXPRESSION_FILE", AopPointcutExpressionFileType.INSTANCE.getLanguage());

  AopElementType AOP_ANNOTATION_EXPRESSION = new AopElementType("AOP_ANNOTATION_EXPRESSION");
  AopElementType AOP_ANNOTATION_VALUES = new AopElementType("AOP_ANNOTATION_VALUES") {
    /*
    @Override
    public ASTNode parseContents(final ASTNode chameleon) {
      final ASTNode parent = chameleon.getTreeParent();
      final ASTNode node = parent.findChildByType(AOP_REFERENCE_HOLDER);
      assert node != null;
      final String nodeText = node.getText();
      @NonNls final String annoText = "@" + (nodeText.contains("..") || nodeText.contains("*") ? "Dummy" : nodeText);
      final PsiElement psi = chameleon.getPsi();
      final PsiElementFactory factory = JavaPsiFacade.getInstance(psi.getProject()).getElementFactory();
      try {
        return factory.createAnnotationFromText(annoText + chameleon.getText(), psi).getParameterList().getNode();
      }
      catch (IncorrectOperationException e) {
        return PsiFileFactory.getInstance(psi.getProject()).createFileFromText("a.txt", chameleon.getText()).getFirstChild().getFirstChild().getNode();
      }
    }
    */
  };
  AopElementType AOP_ANNOTATION_HOLDER = new AopElementType("AOP_ANNOTATION_HOLDER");
  AopElementType AOP_ANNOTATED_TYPE_EXPRESSION = new AopElementType("AOP_ANNOTATED_TYPE_EXPRESSION");
  
  AopElementType AOP_MEMBER_REFERENCE_EXPRESSION = new AopElementType("AOP_MEMBER_REFERENCE_EXPRESSION");
  AopElementType AOP_CONSTRUCTOR_REFERENCE_EXPRESSION = new AopElementType("AOP_CONSTRUCTOR_REFERENCE_EXPRESSION");

  AopElementType AOP_REFERENCE_EXPRESSION = new AopElementType("AOP_REFERENCE_EXPRESSION");
  AopElementType AOP_PARENTHESIZED_EXPRESSION = new AopElementType("AOP_PARENTHESIZED_EXPRESSION");
  AopElementType AOP_POINTCUT_PARENTHESIZED_EXPRESSION = new AopElementType("AOP_POINTCUT_PARENTHESIZED_EXPRESSION");
  AopElementType AOP_QUALIFIED_PARENTHESIZED_EXPRESSION = new AopElementType("AOP_QUALIFIED_PARENTHESIZED_EXPRESSION");
  AopElementType AOP_POINTCUT_REFERENCE = new AopElementType("AOP_POINTCUT_REFERENCE");
  AopElementType AOP_REFERENCE_HOLDER = new AopElementType("AOP_REFERENCE_HOLDER");
  AopElementType AOP_NOT_EXPRESSION = new AopElementType("AOP_NOT_EXPRESSION");
  AopElementType AOP_POINTCUT_NOT_EXPRESSION = new AopElementType("AOP_POINTCUT_NOT_EXPRESSION");
  AopElementType AOP_BINARY_EXPRESSION = new AopElementType("AOP_BINARY_EXPRESSION");
  AopElementType AOP_POINTCUT_BINARY_EXPRESSION = new AopElementType("AOP_POINTCUT_BINARY_EXPRESSION");
  AopElementType AOP_ARRAY_EXPRESSION = new AopElementType("AOP_ARRAY_EXPRESSION");
  AopElementType AOP_GENERIC_TYPE_EXPRESSION = new AopElementType("AOP_GENERIC_TYPE_EXPRESSION");
  AopElementType AOP_SUBTYPE_EXPRESSION = new AopElementType("AOP_SUBTYPE_EXPRESSION");
  AopElementType AOP_WILDCARD_EXPRESSION = new AopElementType("AOP_WILDCARD_EXPRESSION");
  AopElementType AOP_PRIMITIVE_TYPE_EXPRESSION = new AopElementType("AOP_PRIMITIVE_TYPE_EXPRESSION");

  AopElementType AOP_MODIFIER_LIST = new AopElementType("AOP_MODIFIER_LIST");
  AopElementType AOP_PARAMETER_LIST = new AopElementType("AOP_PARAMETER_LIST");
  AopElementType AOP_THROWS_LIST = new AopElementType("AOP_THROWS_LIST");
  AopElementType AOP_THROWS_LIST_ITEM = new AopElementType("AOP_THROWS_LIST_ITEM");
  AopElementType AOP_TYPE_PARAMETER_LIST = new AopElementType("AOP_TYPE_PARAMETER_LIST");

  public static class AopWhitespaceElementType extends AopElementType implements ILeafElementType
  {
    public AopWhitespaceElementType() {
      super("ANNO_WHITE_SPACE");
    }

    @Nonnull
    public ASTNode createLeafNode(CharSequence leafText) {
      return new PsiWhiteSpaceImpl(leafText);
    }
  }

}
