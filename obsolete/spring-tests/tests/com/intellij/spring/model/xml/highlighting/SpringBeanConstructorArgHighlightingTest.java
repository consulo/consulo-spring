/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class SpringBeanConstructorArgHighlightingTest extends SpringHighlightingTestCase {

  @Override
  protected void configureModule(JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
  }

  public void testEmptyConstructor() throws Throwable {
    myFixture.testHighlighting("spring-bean-empty-constructor.xml");
  }
                                                                                         
  public void testWrongNumberOfConstructorArgs() throws Throwable {
    myFixture.testHighlighting("spring-bean-wrong-number-constructor-arg.xml");
  }

  public void testIndexedConstructorArgs() throws Throwable {
    myFixture.testHighlighting(false, false, false, "spring-bean-indexed-constructor-arg.xml");
  }                                                                                            

  public void testTypedConstructorArgs() throws Throwable {
    myFixture.testHighlighting("spring-bean-typed-constructor-arg.xml");
  }
  public void testStringConverableTypedConstructorArgs() throws Throwable {
    myFixture.testHighlighting("spring-bean-string-convertable-constructor-arg.xml");
  }

  public void testConstructorArgIndexes() throws Throwable {
    myFixture.testHighlighting("spring-bean-constructor-arg-indexes.xml");
  }

  public void testGenerics() throws Throwable {
    myFixture.testHighlighting("spring-bean-constructor-arg-generics.xml");
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }
}