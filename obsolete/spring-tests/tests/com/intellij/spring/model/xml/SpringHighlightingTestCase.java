/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml;

import com.intellij.facet.FacetManager;
import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringApplicationComponent;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.facet.SpringFacetConfiguration;
import com.intellij.spring.facet.SpringFacetType;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.highlighting.SpringFacetInspection;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public abstract class SpringHighlightingTestCase<T extends JavaModuleFixtureBuilder> extends BasicSpringTestCase {

  protected JavaCodeInsightTestFixture myFixture;
  protected ModuleFixture myModuleTestFixture;
  protected Project myProject;
  protected Module myModule;
  private SpringFacet myFacet;

  protected Class<T> getModuleFixtureBuilderClass() {
    return (Class<T>)JavaModuleFixtureBuilder.class;
  }

  protected void setUp() throws Exception {
    super.setUp();

    final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder();

    myFixture = JavaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.getFixture());

    final T moduleBuilder = projectBuilder.addModule(getModuleFixtureBuilderClass());

    myFixture.setTestDataPath(getTestDataPath());
    final Class[] inspectionClasses = new SpringApplicationComponent().getInspectionClasses();
    myFixture.enableInspections(ArrayUtil.remove(inspectionClasses, SpringFacetInspection.class));

    configureModule(moduleBuilder);

    myFixture.setUp();

    myProject = myFixture.getProject();
    myModuleTestFixture = moduleBuilder.getFixture();
    myModule = myModuleTestFixture.getModule();

  }

  protected void configureModule(final T moduleBuilder) throws Exception {
    moduleBuilder.addContentRoot(myFixture.getTempDirPath());
    if (isWithTestSources()) {
      moduleBuilder.addContentRoot(getTestDataPath());
    }
    moduleBuilder.addSourceRoot("");
  }

  protected boolean isWithTestSources() {
    return true;
  }

  protected void addSpringJar(final JavaModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addLibraryJars("spring", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(), "spring-2.0.6.jar");
  }

  protected void addSpringCoreJars(final JavaModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addLibraryJars("spring-core", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(), "spring2_5-core.jar");
    moduleBuilder.addLibraryJars("spring-beans", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(), "spring2_5-beans.jar");
  }

  protected void addSpring_2_5_Library(final JavaModuleFixtureBuilder moduleBuilder) {
    moduleBuilder
        .addLibraryJars("spring2_5", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(), "spring2_5.jar");
  }

  protected SpringFacet createFacet() {
    final RunResult<SpringFacet> runResult = new WriteCommandAction<SpringFacet>(myProject) {
      protected void run(final Result<SpringFacet> result) throws Throwable {
        String name = SpringFacetType.INSTANCE.getPresentableName();
        final SpringFacet facet = FacetManager.getInstance(myModule).addFacet(SpringFacetType.INSTANCE, name, null);
        result.setResult(facet);
      }
    }.execute();
    final Throwable throwable = runResult.getThrowable();
    if (throwable != null) {
      throw new RuntimeException(throwable);
    }

    return runResult.getResultObject();
  }

  protected void tearDown() throws Exception {
    myFixture.tearDown();
    myFixture = null;
    myModuleTestFixture = null;
    myProject = null;
    myModule = null;
    myFacet = null;
    super.tearDown();
  }

  @NotNull
  protected <T extends DomElement> DomFileElement<T> getFileElement(String path, Class<T> clazz, Project project) throws IOException {
    final XmlFile psiFile = getXmlFile(path);

    final DomFileElement<T> element = DomManager.getDomManager(project).getFileElement(psiFile, clazz);
    assertNotNull("Cannot get DomFileElement for '" + path + "' file", element);

    return element;
  }

  protected XmlFile getXmlFile(final String path) throws IOException {

    final VirtualFile file = myFixture.copyFileToProject(path);
    final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);

    assertNotNull(psiFile);
    assertTrue("'" + path + "' must be a xml file", psiFile instanceof XmlFile);

    return (XmlFile)psiFile;
  }

  protected SpringFileSet configureFileSet() throws Throwable {
    return configureFileSet("noname", myModule);
  }

  protected SpringFileSet configureFileSet(final String filesetId, final Module module) throws Throwable {
    if (myFacet == null) {
      new WriteCommandAction(myProject) {
        protected void run(final Result result) throws Throwable {
          myFacet = FacetManager.getInstance(module).addFacet(SpringFacetType.INSTANCE, SpringFacetType.INSTANCE.getPresentableName(), null);
        }
      }.execute().throwException();
    }

    final SpringFacetConfiguration configuration = myFacet.getConfiguration();
    final Set<SpringFileSet> list = configuration.getFileSets();
    final SpringFileSet fileSet = new SpringFileSet(filesetId, "default", configuration);
    list.add(fileSet);
    return fileSet;
  }


  protected VirtualFile addFileToSet(final SpringFileSet fileSet, @NonNls final String path)  throws IOException {
    final XmlFile file = getXmlFile(path);
    assert file != null : "cannot find file: " + path;
    fileSet.addFile(file.getVirtualFile());
    return file.getVirtualFile();
  }

  @NotNull
  protected SpringBean getBeanFromFile(String fileName, String beanId) throws IOException {
    final DomFileElement<Beans> fileElement = getFileElement(fileName, Beans.class, myProject);

    final SpringBean springBean = findBeanById(fileElement.getRootElement(), beanId);
    assertNotNull("Cannot find bean = " + beanId, springBean);

    return springBean;
  }

  protected SpringBean findBeanById(final Beans rootElement, @NotNull final String beanId) {
    for (SpringBean bean : rootElement.getBeans()) {
      if (beanId.equals(bean.getId().getStringValue())) return bean;
    }
    return null;
  }

  protected Beans getBeans(final String fileName) throws IOException {
    return getFileElement(fileName, Beans.class, myProject).getRootElement();
  }
}
