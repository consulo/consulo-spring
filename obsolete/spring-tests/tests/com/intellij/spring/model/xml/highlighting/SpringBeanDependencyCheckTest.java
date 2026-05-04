/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.spring.model.xml.SpringHighlightingTestCase;

public class SpringBeanDependencyCheckTest extends SpringHighlightingTestCase {


  public void testDependencyCheck() throws Throwable {
    myFixture.testHighlighting(false, false, false, "spring-bean-dependency-check.xml");
  }

  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }
}