/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.web;

import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import org.jetbrains.annotations.NonNls;

import java.io.File;

/**
 * @author Dmitry Avdeev
 */
public class PetstoreTest extends SpringHighlightingTestCase<WebModuleFixtureBuilder> {

  protected void setUp() throws Exception {
    super.setUp();
    createFacet();
  }

  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  protected void configureModule(WebModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    
    moduleBuilder.addWebRoot(myFixture.getTempDirPath() + "/web/", "/");
    moduleBuilder.addWebRoot(getTestDataPath() + "/web/", "/");
    moduleBuilder.setWebXml(getTestDataPath() + "/web/WEB-INF/web.xml");
    addSpringJar(moduleBuilder);
    moduleBuilder.addLibraryJars("jpet", PathManager.getHomePath().replace(File.separatorChar, '/') + getBasePath(), "jpet.jar");
  }

  public void testPetstore() throws Throwable {
    myFixture.testHighlighting(true, false, false, "web/WEB-INF/applicationContext.xml");
    myFixture.testHighlighting(true, false, false, "web/WEB-INF/petstore-servlet.xml");
  }

  public void testJsp() throws Throwable {
    myFixture.testHighlighting(true, false, false, "web/test.jsp");
  }

  @NonNls
  protected String getBasePath() {
    return "/svnPlugins/spring/web/testData/petstore/";
  }
}
