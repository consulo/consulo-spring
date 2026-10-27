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

package consulo.spring.spel.language.impl.parsing;

import consulo.language.file.LanguageFileType;
import consulo.spring.spel.language.SpELFileType;
import consulo.test.junit.impl.language.SimpleParsingTest;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

public class SpELParsingTest extends SimpleParsingTest<Object> {
    public SpELParsingTest() {
        super("parsing", "spel");
    }

    @Test
    public void testLiteral(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testPropertyAccess(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testMethodCall(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testBeanReference(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testTernary(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testElvis(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testArithmetic(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testComparison(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testLogical(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testTypeReference(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testConstructor(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testSafeNavigation(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testIndexer(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testProjection(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testSelection(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testVariableReference(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testInlineList(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testComplex(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testPlaceholder(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testPlaceholderNestedDefault(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testPlaceholderKeyword(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testPlaceholderInString(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testPlaceholderInTernary(Context context) throws Exception {
        doTest(context, null);
    }

    @Override
    protected LanguageFileType getFileType(Context context, @Nullable Object o) {
        return SpELFileType.INSTANCE;
    }
}
