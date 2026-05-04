/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package com.intellij.spring.model.xml.integration.jsf;

import com.intellij.codeInspection.jsp.ELValidationInspection;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.jsf.model.FacesConfig;
import com.intellij.jsf.model.FacesDomModel;
import com.intellij.jsf.model.FacesDomModelManager;
import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.Collection;

public class JsfVariableResolverTest extends SpringHighlightingTestCase<WebModuleFixtureBuilder> {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  protected Class<WebModuleFixtureBuilder> getModuleFixtureBuilderClass() {
    return WebModuleFixtureBuilder.class;
  }

  protected boolean isWithTestSources() {
    return false;
  }

  protected void configureModule(WebModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    myFixture.enableInspections(new ELValidationInspection());
    //moduleBuilder.addWebRoot(myFixture.getTempDirPath(), "/");
    moduleBuilder.addWebRoot(myFixture.getTempDirPath() + "/WEB-INF/", "/WEB-INF");
    addSpringJar(moduleBuilder);

    moduleBuilder.addLibraryJars("myfaces", PathManager.getHomePath().replace(File.separatorChar, '/') + getBasePath(), "myfaces.jar");
    moduleBuilder
      .addLibraryJars("myfaces-jsf-api", PathManager.getHomePath().replace(File.separatorChar, '/') + getBasePath(), "myfaces-jsf-api.jar");
    moduleBuilder.addLibraryJars("SpringJavaConfig", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(),
                                 "spring-javaconfig-annotations.jar");
  }

  private void createFileSet() throws Throwable {
    final String path = myFixture.getTempDirPath() + File.separatorChar + "spring-beans.xml";
    final String url = VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, path);
    configureFileSet().addFile(url);
  }

  public void testDelegatingVariableResolverHighlighting() throws Throwable {
    configureResolver("org.springframework.web.jsf.DelegatingVariableResolver");

    springBeansHighlighting();
  }

  public void testDelegatingVariableResolverRename() throws Throwable {
    configureResolver("org.springframework.web.jsf.DelegatingVariableResolver");

    springBeansRename();
  }

  public void testDelegatingVariableResolverJavaBeansRename() throws Throwable {
    configureResolver("org.springframework.web.jsf.DelegatingVariableResolver");

    springJavaBeansRename();
  }

  public void testSpringBeanVariableResolverHighlighting() throws Throwable {
    configureResolver("org.springframework.web.jsf.SpringBeanVariableResolver");

    springBeansHighlighting();
  }

  public void testSpringBeanVariableResolverRename() throws Throwable {
    configureResolver("org.springframework.web.jsf.SpringBeanVariableResolver");

    springBeansRename();
  }

  public void testSpringBeanVariableResolverJavaBeansRename() throws Throwable {
    configureResolver("org.springframework.web.jsf.SpringBeanVariableResolver");

    springJavaBeansRename();
  }


  public void testSpringBeanFacesELResolverHighlighting() throws Throwable {
    configureELResolver("org.springframework.web.jsf.el.SpringBeanFacesELResolver");

    springBeansHighlighting();
  }

  public void testSpringBeanFacesELResolverRename() throws Throwable {
    configureELResolver("org.springframework.web.jsf.el.SpringBeanFacesELResolver");

    springBeansRename();
  }

  public void testSpringBeanFacesELResolverJavaBeansRename() throws Throwable {
    configureELResolver("org.springframework.web.jsf.el.SpringBeanFacesELResolver");

    springJavaBeansRename();
  }

  public void testWebApplicationContextVariableResolverHighlighting() throws Throwable {
    configureResolver("org.springframework.web.jsf.WebApplicationContextVariableResolver");

    myFixture.configureByFiles("SpringBeansWebContext.jsp", "beans/JavaConfig.java", "spring-beans.xml");

    VirtualFile file = getFile(myFixture.getTempDirPath() + "/beans/JavaConfig.java");
    assertNotNull(file);
    myFixture.allowTreeAccessForFile(file);
    
    myFixture.testHighlighting(true, true, true, "SpringBeansWebContext.jsp", "beans/JavaConfig.java", "spring-beans.xml");
  }

  public void testWebApplicationContextVariableResolverRename() throws Throwable {
    configureResolver("org.springframework.web.jsf.WebApplicationContextVariableResolver");

    myFixture.testRename("SpringBeansWebContextBefore.jsp", "SpringBeansWebContextAfter.jsp", "newSpringBeanName", "spring-beans.xml");
    myFixture.checkResultByFile("spring-beans.xml", "spring-beans-after.xml", true);
  }

  public void testWebApplicationContextFacesELResolverHighlighting() throws Throwable {
    configureELResolver("org.springframework.web.jsf.el.WebApplicationContextFacesELResolver");

    myFixture.configureByFiles( "beans/JavaConfig.java", "SpringBeansWebContext.jsp", "spring-beans.xml");
    
    VirtualFile file = getFile(myFixture.getTempDirPath() + "/beans/JavaConfig.java");
    assertNotNull(file);
    myFixture.allowTreeAccessForFile(file); 
    myFixture.testHighlighting(true, true, true, "SpringBeansWebContext.jsp", "beans/JavaConfig.java", "spring-beans.xml");
  }

  public void testWebApplicationContextFacesELResolverRename() throws Throwable {
    configureELResolver("org.springframework.web.jsf.el.WebApplicationContextFacesELResolver");

    myFixture.testRename("SpringBeansWebContextBefore.jsp", "SpringBeansWebContextAfter.jsp", "newSpringBeanName", "spring-beans.xml");
    myFixture.checkResultByFile("spring-beans.xml", "spring-beans-after.xml", true);
  }

  private void configureResolver(final String s) throws Throwable {
    myFixture.copyDirectoryToProject("WEB-INF", "WEB-INF");
    createFileSet();

    new WriteCommandAction.Simple(myFixture.getProject()) {
      protected void run() throws Throwable {
        final FacesConfig facesConfig = getFacesConfig();
        facesConfig.getApplication().getVariableResolver().setStringValue(s);
      }
    }.execute();
  }

  private void configureELResolver(final String s) throws Throwable {
    myFixture.copyDirectoryToProject("WEB-INF", "WEB-INF");
    createFileSet();

    new WriteCommandAction.Simple(myFixture.getProject()) {
      protected void run() throws Throwable {
        final FacesConfig facesConfig = getFacesConfig();
        facesConfig.getApplication().getElResolver().setStringValue(s);
      }
    }.execute();
  }

  private void springBeansHighlighting() throws Throwable {
    myFixture.configureByFiles("beans/JavaConfig.java", "SpringBeans.jsp",  "spring-beans.xml");
    VirtualFile file = getFile(myFixture.getTempDirPath() + "/beans/JavaConfig.java");
    assertNotNull(file);
    myFixture.allowTreeAccessForFile(file);
    myFixture.testHighlighting(true, true, true, "SpringBeans.jsp", "beans/JavaConfig.java", "spring-beans.xml");
  }

  private void springBeansRename() throws Throwable {
    myFixture.testRename("SpringBeansBefore.jsp", "SpringBeansAfter.jsp", "newSpringBeanName", "spring-beans.xml");
    myFixture.checkResultByFile("spring-beans.xml", "spring-beans-after.xml", true);
  }

  private void springJavaBeansRename() throws Throwable {
    myFixture
      .testRename("SpringBeansBefore2.jsp", "SpringBeansAfter2.jsp", "javaConfiguredBeanNewName", "beans/JavaConfig.java", "spring-beans.xml");
    myFixture.checkResultByFile("beans/JavaConfig.java", "beans/JavaConfig.after", true);
  }

  @NonNls
  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/jsf/web/";
  }

  private FacesConfig getFacesConfig() {
    final VirtualFile file = getFile(myFixture.getTempDirPath() + "/WEB-INF/faces-config.xml");

    final Collection<WebFacet> webFacets = WebFacet.getInstances(myFixture.getModule());
    for (WebFacet webFacet : webFacets) {

      for (FacesDomModel model : FacesDomModelManager.getInstance(myFixture.getProject()).getAllModels(webFacet)) {
        if (file.equals(DomUtil.getFile(model.getFacesConfig()).getVirtualFile())) {
          return model.getFacesConfig();
        }
      }
    }
    return null;
  }
}
