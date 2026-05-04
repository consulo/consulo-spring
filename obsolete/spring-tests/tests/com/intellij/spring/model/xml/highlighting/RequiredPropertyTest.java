package com.intellij.spring.model.xml.highlighting;

import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class RequiredPropertyTest extends SpringHighlightingTestCase<JavaModuleFixtureBuilder> {

  protected boolean isWithTestSources() {
    return false;
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);

    addSpring_2_5_Library(moduleBuilder);
  }

  public void testRequiredAnnotation() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "required_prop.xml");
    myFixture.testHighlighting(true, false, false, "RequiredBean.java");
  }

  public void testRequiredProperty() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "required.xml");
    myFixture.copyFileToProject("AnotherBean.java");
    myFixture.testHighlighting(true, false, false, "required.xml");
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/required/";
  }
}