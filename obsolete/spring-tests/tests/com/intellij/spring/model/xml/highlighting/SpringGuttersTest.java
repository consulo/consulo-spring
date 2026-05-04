/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.actionSystem.AnAction;
import org.jetbrains.annotations.NonNls;

/**
 * @author Dmitry Avdeev
 */
public class SpringGuttersTest extends SpringHighlightingTestCase {

  public void testParentGutter() throws Throwable {
    assertNotNull(myFixture.findGutter("parentGutter.xml"));
    assertNotNull(myFixture.findGutter("parentGutter2.xml"));
    assertNull(myFixture.findGutter("parentGutter3.xml"));
    assertNull(myFixture.findGutter("parentGutter_wrong.xml"));
  }

  public void testClassGutter() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "config.xml");
    assertNotNull(myFixture.findGutter("Bean.java"));
  }

  public void testPropertyGutter() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "propertyGutter.xml");
    final GutterIconRenderer renderer = myFixture.findGutter("BeanWithProperties.java");
    assertNotNull(renderer);
    final AnAction anAction = renderer.getClickAction();
    assert anAction != null;
    anAction.actionPerformed(null);
  }

  @NonNls
  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/gutters/";
  }
}
