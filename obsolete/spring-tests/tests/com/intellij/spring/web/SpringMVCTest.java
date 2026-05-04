package com.intellij.spring.web;

import com.intellij.javaee.web.facet.WebFacet;
import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.spring.web.mvc.SpringMVCModel;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public class SpringMVCTest extends SpringHighlightingTestCase<WebModuleFixtureBuilder> {

  protected void setUp() throws Exception {
    super.setUp();
    createFacet();
  }

  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  @Override
  protected boolean isWithTestSources() {
    return false;
  }

  protected void configureModule(WebModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    moduleBuilder.addWebRoot(myFixture.getTempDirPath() + "/", "/");
    moduleBuilder.addWebRoot(getTestDataPath() + "/", "/");
    moduleBuilder.setWebXml(getTestDataPath() + "/WEB-INF/web.xml");
    addSpringJar(moduleBuilder);
    moduleBuilder.addLibraryJars("mvc", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(), "spring-webmvc.jar");
  }

  public void testJamModel() throws Throwable {
    myFixture.copyDirectoryToProject("", "");
    final WebFacet webFacet = WebFacet.getInstances(myModule).iterator().next();
    final SpringMVCModel mvcModel = SpringMVCModel.getModel(webFacet, SpringFacet.getInstance(myModule));
    assertNotNull(mvcModel);
    final Collection<SpringModel> springModels = mvcModel.getAllModels();
    assertEquals(1, springModels.size());
    Set<String> urls = mvcModel.getAllUrls();
    assertEquals(new HashSet<String>(Arrays.asList("/class.form", "/method.form", "/anotherUrl.form", "/register.form")), urls);

    myFixture.testHighlighting("test.html");
  }

  public void testCompletion() throws Throwable {
    myFixture.copyDirectoryToProject("", "");
    myFixture.testCompletion("completion.html","completion_after.html");
  }

  @Override
  protected String getBasePath() {
    return "/svnPlugins/spring/web/testData/mvc/";
  }
}
