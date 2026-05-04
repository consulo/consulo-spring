/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import org.jetbrains.annotations.NonNls;

public class SpringBeanAutowireHighlightingTest extends SpringHighlightingTestCase {


  public void testByTypeAutowire() throws Throwable {
    myFixture.testHighlighting(false, false, false, "spring-bean-autowire-by-type.xml");
  }

  public void testByConstructorAutowire() throws Throwable {
    myFixture.testHighlighting(false, false, false, "spring-bean-autowire-by-constructor.xml");
  }

  public void testAutodetectAutowire() throws Throwable {
    myFixture.testHighlighting(false, false, false, "spring-bean-autowire-autodetect.xml");
  }

  public void testAutowireCandidate() throws Throwable {
    myFixture.testHighlighting(false, false, false, "spring-bean-autowire-candidate.xml");
  }

    @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }
}