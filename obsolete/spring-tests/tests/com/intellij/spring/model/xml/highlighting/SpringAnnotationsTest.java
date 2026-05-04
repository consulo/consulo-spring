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

import com.intellij.codeInspection.deadCode.DeadCodeInspection;
import com.intellij.codeInspection.unusedSymbol.UnusedSymbolLocalInspection;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

/**
 * @author Dmitry Avdeev
 */
public class SpringAnnotationsTest extends SpringHighlightingTestCase<JavaModuleFixtureBuilder> {

  @Override
  protected void configureModule(JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    addSpringJar(moduleBuilder);
  }

  @Override
  protected boolean isWithTestSources() {
    return false;
  }

  public void testJMXAnnotations() throws Exception {
    myFixture.enableInspections(new UnusedSymbolLocalInspection(), new DeadCodeInspection());
    myFixture.testHighlighting(true, false, false, "JMXAnnotations.java");
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }
}
