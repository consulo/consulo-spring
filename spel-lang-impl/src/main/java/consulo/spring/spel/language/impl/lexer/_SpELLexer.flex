/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.spring.spel.language.impl.lexer;

import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.lexer.LexerBase;
import consulo.spring.spel.language.SpELTokenTypes;

%%

%public
%class _SpELLexer
%extends LexerBase
%function advanceImpl
%type IElementType
%unicode
%eof{  return;
%eof}

%{
    // nesting of ${...} inside a placeholder default value, like ${a:${b:1}}
    private int myDefaultDepth;
%}

%state STRING
%state PLACEHOLDER
%state PLACEHOLDER_DEFAULT
%state STR_PLACEHOLDER
%state STR_PLACEHOLDER_DEFAULT

WHITE_SPACE=[ \t\r\n]+
DIGIT=[0-9]
LETTER=[a-zA-Z_$]
IDENTIFIER_PART=[a-zA-Z0-9_$]

%%

<YYINITIAL> {
    {WHITE_SPACE}                           { return TokenType.WHITE_SPACE; }

    // property placeholder ${...}
    "${"                                    { yybegin(PLACEHOLDER); return SpELTokenTypes.DOLLAR_LBRACE; }

    // string literals (single-quoted, '' is escape for ')
    "'"                                     { yybegin(STRING); return SpELTokenTypes.STRING_LITERAL; }

    // real literals (must be before integer to match correctly)
    {DIGIT}+ "." {DIGIT}+ ([eE] [+-]? {DIGIT}+)? [fFdD]?
                                            { return SpELTokenTypes.REAL_LITERAL; }
    {DIGIT}+ [eE] [+-]? {DIGIT}+ [fFdD]?   { return SpELTokenTypes.REAL_LITERAL; }
    {DIGIT}+ [fFdD]                         { return SpELTokenTypes.REAL_LITERAL; }

    // integer literals
    "0x" [0-9a-fA-F]+  [lL]?               { return SpELTokenTypes.INTEGER_LITERAL; }
    {DIGIT}+ [lL]?                          { return SpELTokenTypes.INTEGER_LITERAL; }

    // multi-character operators (must be before single-char)
    ".!["                                   { return SpELTokenTypes.PROJECTION; }
    ".?["                                   { return SpELTokenTypes.SELECTION; }
    ".$["                                   { return SpELTokenTypes.SELECT_LAST; }
    ".^["                                   { return SpELTokenTypes.SELECT_FIRST; }
    "?."                                    { return SpELTokenTypes.SAFE_NAV; }
    "?:"                                    { return SpELTokenTypes.ELVIS; }
    "=="                                    { return SpELTokenTypes.EQ; }
    "!="                                    { return SpELTokenTypes.NE; }
    "<="                                    { return SpELTokenTypes.LE; }
    ">="                                    { return SpELTokenTypes.GE; }
    "&&"                                    { return SpELTokenTypes.AND_AND; }
    "||"                                    { return SpELTokenTypes.OR_OR; }

    // single-character operators and delimiters
    "+"                                     { return SpELTokenTypes.PLUS; }
    "-"                                     { return SpELTokenTypes.MINUS; }
    "*"                                     { return SpELTokenTypes.STAR; }
    "/"                                     { return SpELTokenTypes.DIV; }
    "%"                                     { return SpELTokenTypes.MOD; }
    "^"                                     { return SpELTokenTypes.POWER; }
    "<"                                     { return SpELTokenTypes.LT; }
    ">"                                     { return SpELTokenTypes.GT; }
    "="                                     { return SpELTokenTypes.ASSIGN; }
    "?"                                     { return SpELTokenTypes.QMARK; }
    ":"                                     { return SpELTokenTypes.COLON; }
    "!"                                     { return SpELTokenTypes.BANG; }
    "("                                     { return SpELTokenTypes.LPAREN; }
    ")"                                     { return SpELTokenTypes.RPAREN; }
    "["                                     { return SpELTokenTypes.LBRACKET; }
    "]"                                     { return SpELTokenTypes.RBRACKET; }
    "{"                                     { return SpELTokenTypes.LBRACE; }
    "}"                                     { return SpELTokenTypes.RBRACE; }
    "."                                     { return SpELTokenTypes.DOT; }
    ","                                     { return SpELTokenTypes.COMMA; }
    "#"                                     { return SpELTokenTypes.HASH; }
    "@"                                     { return SpELTokenTypes.AT; }

    // identifiers (keywords are remapped in SpELLexer wrapper)
    {LETTER} {IDENTIFIER_PART}*             { return SpELTokenTypes.IDENTIFIER; }

    [^]                                     { return TokenType.BAD_CHARACTER; }
}

// String state: recognizes ${...} inside strings
<STRING> {
    "''"                                    { return SpELTokenTypes.STRING_LITERAL; }
    "'"                                     { yybegin(YYINITIAL); return SpELTokenTypes.STRING_LITERAL; }
    "${"                                    { yybegin(STR_PLACEHOLDER); return SpELTokenTypes.DOLLAR_LBRACE; }
    [^'$]+                                  { return SpELTokenTypes.STRING_LITERAL; }
    "$"                                     { return SpELTokenTypes.STRING_LITERAL; }
}

// Standalone placeholder: ${key.name:default} at top level
<PLACEHOLDER> {
    "}"                                     { yybegin(YYINITIAL); return SpELTokenTypes.RBRACE; }
    ":"                                     { myDefaultDepth = 0; yybegin(PLACEHOLDER_DEFAULT); return SpELTokenTypes.COLON; }
    // the whole key is one token: property names may hold '-', '[0]' or words which are SpEL keywords
    [^}:]+                                  { return SpELTokenTypes.PLACEHOLDER_CONTENT; }
}

<PLACEHOLDER_DEFAULT> {
    "${"                                    { myDefaultDepth++; return SpELTokenTypes.PLACEHOLDER_CONTENT; }
    "}"                                     { if (myDefaultDepth > 0) { myDefaultDepth--; return SpELTokenTypes.PLACEHOLDER_CONTENT; } yybegin(YYINITIAL); return SpELTokenTypes.RBRACE; }
    [^}$]+                                  { return SpELTokenTypes.PLACEHOLDER_CONTENT; }
    "$"                                     { return SpELTokenTypes.PLACEHOLDER_CONTENT; }
}

// Placeholder inside a string: ${key.name:default} returns to STRING on }
<STR_PLACEHOLDER> {
    "}"                                     { yybegin(STRING); return SpELTokenTypes.RBRACE; }
    ":"                                     { myDefaultDepth = 0; yybegin(STR_PLACEHOLDER_DEFAULT); return SpELTokenTypes.COLON; }
    [^}:']+                                 { return SpELTokenTypes.PLACEHOLDER_CONTENT; }
    "'"                                     { yybegin(YYINITIAL); return SpELTokenTypes.STRING_LITERAL; }
}

<STR_PLACEHOLDER_DEFAULT> {
    "${"                                    { myDefaultDepth++; return SpELTokenTypes.PLACEHOLDER_CONTENT; }
    "}"                                     { if (myDefaultDepth > 0) { myDefaultDepth--; return SpELTokenTypes.PLACEHOLDER_CONTENT; } yybegin(STRING); return SpELTokenTypes.RBRACE; }
    [^}$]+                                  { return SpELTokenTypes.PLACEHOLDER_CONTENT; }
    "$"                                     { return SpELTokenTypes.PLACEHOLDER_CONTENT; }
}
