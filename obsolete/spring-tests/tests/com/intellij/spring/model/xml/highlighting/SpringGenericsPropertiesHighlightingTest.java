package com.intellij.spring.model.xml.highlighting;

import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.codeInsight.completion.CompletionType;
import org.jetbrains.annotations.NonNls;

public class SpringGenericsPropertiesHighlightingTest extends SpringHighlightingTestCase<JavaModuleFixtureBuilder> {
   protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
  }

  public void testSpringPropertyRef() throws Throwable {
    myFixture.testHighlighting("spring-generics-properties.xml");
  }

  public void testSpringPropertyRefCompletion() throws Throwable {
    myFixture.configureByFile("spring-generics-properties-befor.xml");
    myFixture.complete(CompletionType.SMART);
    myFixture.checkResultByFile("spring-generics-properties-after.xml");
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting";
  }
}