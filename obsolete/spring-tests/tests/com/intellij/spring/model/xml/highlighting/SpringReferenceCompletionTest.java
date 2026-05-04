package com.intellij.spring.model.xml.highlighting;

import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class SpringReferenceCompletionTest extends SpringHighlightingTestCase {
  protected void setUp() throws Exception {
    super.setUp();
    try {
      final SpringFileSet fileSet = configureFileSet();

      addFileToSet(fileSet, "spring-beans.xml");
    }
    catch (Throwable t) {
      throw new Exception(t);
    }
  }
   protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
     super.configureModule(moduleBuilder);
     addSpringJar(moduleBuilder);
   }

  public void testSpringReferences() throws Throwable {
    myFixture.testCompletion("FooBeanClass.java", "FooBeanClass_after.java");
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/completion/";
  }
}
