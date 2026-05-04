/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"JUnitTestClassNamingConvention"})
public class SpringJdk15HighlightingTest extends SpringHighlightingTestCase {

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }

  public void testMapEntries() throws Throwable {
    myFixture.testHighlighting("mapEntries.xml");
  }

  public void testParameterizedCtorArgs() throws Throwable {
    myFixture.testHighlighting("constructor-arg-parameters.xml");
  }

  public void testEnums() throws Throwable {
    myFixture.testHighlighting("enums.xml", "MyApp.java");
  }


}
