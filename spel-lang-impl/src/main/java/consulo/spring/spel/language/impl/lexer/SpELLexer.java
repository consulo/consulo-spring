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
import consulo.language.lexer.DelegateLexer;
import consulo.language.lexer.MergingLexerAdapter;
import consulo.language.ast.TokenSet;
import consulo.spring.spel.language.SpELTokenTypes;

import java.util.Map;

public class SpELLexer extends DelegateLexer {
    private static final Map<String, IElementType> KEYWORDS = Map.ofEntries(
        Map.entry("true", SpELTokenTypes.TRUE),
        Map.entry("false", SpELTokenTypes.FALSE),
        Map.entry("null", SpELTokenTypes.NULL),
        Map.entry("new", SpELTokenTypes.NEW),
        Map.entry("instanceof", SpELTokenTypes.INSTANCEOF),
        Map.entry("matches", SpELTokenTypes.MATCHES),
        Map.entry("and", SpELTokenTypes.AND),
        Map.entry("or", SpELTokenTypes.OR),
        Map.entry("not", SpELTokenTypes.NOT),
        Map.entry("T", SpELTokenTypes.T_KEYWORD)
    );

    private static final TokenSet MERGE_TOKENS = TokenSet.create(SpELTokenTypes.STRING_LITERAL, SpELTokenTypes.PLACEHOLDER_CONTENT);

    public SpELLexer() {
        super(new MergingLexerAdapter(new _SpELLexer(), MERGE_TOKENS));
    }

    @Override
    public IElementType getTokenType() {
        IElementType tokenType = super.getTokenType();
        if (tokenType == SpELTokenTypes.IDENTIFIER) {
            String text = getTokenText();
            IElementType keyword = KEYWORDS.get(text);
            if (keyword != null) {
                return keyword;
            }
        }
        return tokenType;
    }
}
