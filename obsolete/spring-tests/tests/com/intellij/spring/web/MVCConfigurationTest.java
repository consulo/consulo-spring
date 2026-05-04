/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package com.intellij.spring.web;

import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

/**
 * @author Dmitry Avdeev
 */
public class MVCConfigurationTest extends SpringHighlightingTestCase<WebModuleFixtureBuilder> {

  protected void setUp() throws Exception {
    super.setUp();
    createFacet();
  }

  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  protected void configureModule(WebModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addWebRoot(myFixture.getTempDirPath() + "/", "/");
    moduleBuilder.setWebXml(myFixture.getTempDirPath() + "/WEB-INF/web.xml");
    addSpringJar(moduleBuilder);
  }

  public void testMVCConfiguration() throws Throwable {
    myFixture.configureByFiles("WEB-INF/web.xml");
    new SpringWebConfigurator().configure(myModule);
    myFixture.checkResultByFile("WEB-INF/web_after.xml");
    myFixture.checkResultByFile("WEB-INF/applicationContext.xml", "WEB-INF/context_after.xml", true);
    myFixture.checkResultByFile("WEB-INF/dispatcher-servlet.xml", "WEB-INF/context_after.xml", true);
  }

  @NonNls
  protected String getBasePath() {
    return "/svnPlugins/spring/web/testData/configure/";
  }
}
