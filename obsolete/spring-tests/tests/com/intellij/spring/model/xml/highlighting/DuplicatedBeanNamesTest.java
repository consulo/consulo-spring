/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import org.jetbrains.annotations.NonNls;

public class DuplicatedBeanNamesTest extends SpringHighlightingTestCase {

  public void testDublicateBeanNames() throws Throwable {
    myFixture.testHighlighting(false, false, false, "duplicated-bean-names.xml");
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }
}