package com.intellij.spring.model.xml.highlighting;

import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class UtilSchemaHighlightingTest extends SpringHighlightingTestCase {
  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
  }

  public void testByTypeAutowire() throws Throwable {
    myFixture.testHighlighting(false, false, false, "util-schema-highlighting.xml");
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }
}