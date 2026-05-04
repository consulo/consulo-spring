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

package com.intellij.spring.model.xml.highlighting;

import com.intellij.codeInsight.intention.IntentionAction;
import consulo.ide.impl.idea.codeInspection.ex.QuickFixWrapper;
import com.intellij.codeInspection.jsp.ELValidationInspection;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;

import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class SpringElTest extends SpringHighlightingTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new ELValidationInspection());
  }

  public void testEl() throws Throwable {
    myFixture.testHighlighting(true, false, true, "el.xml");
  }

  public void testElCompletion() throws Throwable {
    myFixture
      .testCompletionVariants("elCompletion.xml", "class", "equals", "hashCode", "integer", "notify", "notifyAll", "toString", "wait",
                              "wait", "wait");
  }

  public void testElFixes() throws Exception {
    List<IntentionAction> intentions = myFixture.getAvailableIntentions("elFixes.xml");
    for (IntentionAction intention : intentions) {
      assertFalse(intention instanceof QuickFixWrapper && ((QuickFixWrapper)intention).getFix() instanceof ELValidationInspection.DeclareELVarFix);
    }
  }

  @Override
  protected String getBasePath() {
    return super.getBasePath() + "el/";
  }
}
