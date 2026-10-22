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

package com.intellij.spring.impl.ide.inject;

import com.intellij.spring.impl.ide.constants.SpringAnnotationsConstants;
import com.intellij.java.language.patterns.PsiJavaPatterns;
import com.intellij.java.language.psi.PsiLiteralExpression;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.inject.ConcatenationAwareInjector;
import consulo.language.inject.MultiHostRegistrar;
import consulo.language.pattern.ElementPattern;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.spring.spel.language.SpELLanguage;

import static consulo.language.pattern.StandardPatterns.string;

@ExtensionImpl
public class SpELValueInjector implements ConcatenationAwareInjector {
    private static final ElementPattern<PsiLiteralExpression> VALUE_PATTERN =
        PsiJavaPatterns.literalExpression()
            .withText(string().matches("(?s).*[#$]\\{.*"))
            .insideAnnotationParam(string().oneOf(SpringAnnotationsConstants.VALUE_ANNOTATION), "value");

    @Override
    @RequiredReadAction
    public void inject(MultiHostRegistrar registrar, PsiElement... operands) {
        PsiElement host = operands[0];
        if (!VALUE_PATTERN.accepts(host)) {
            return;
        }

        String text = host.getText();
        if (text.length() < 2) {
            return;
        }

        // remove surrounding quotes from the literal
        String content = text.substring(1, text.length() - 1);

        int pos = 0;
        while (pos < content.length() - 1) {
            char c = content.charAt(pos);
            if ((c != '#' && c != '$') || content.charAt(pos + 1) != '{') {
                pos++;
                continue;
            }

            int end = findClosingBrace(content, pos + 2);
            if (end < 0) {
                break;
            }

            // #{expression} injects the expression between the braces, a property placeholder ${key:default}
            // is SpEL syntax by itself and injects whole. Text offsets are +1 for the opening quote of the literal.
            TextRange range = c == '#' ? new TextRange(pos + 2 + 1, end + 1) : new TextRange(pos + 1, end + 1 + 1);
            if (!range.isEmpty()) {
                registrar.startInjecting(SpELLanguage.INSTANCE);
                registrar.addPlace(null, null, (PsiLanguageInjectionHost) host, range);
                registrar.doneInjecting();
            }

            pos = end + 1;
        }
    }

    /**
     * @return the index of the brace which closes the one opened right before {@code from}, or -1
     */
    private static int findClosingBrace(String content, int from) {
        int depth = 1;
        for (int i = from; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                depth++;
            }
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
}
