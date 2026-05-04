/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml;

import com.intellij.facet.FacetManager;
import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.RunResult;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.facet.SpringFacetConfiguration;
import com.intellij.spring.facet.SpringFacetType;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;

import java.io.File;
import java.util.Set;

public abstract class HeavySpringTestCase extends BasicSpringTestCase {

  protected Project myProject;
  protected IdeaProjectTestFixture myFixture;
  protected TempDirTestFixture myTempDirTestFixture;
  private final boolean myModifiable;
  protected Module myModule;
  protected SpringFacet myFacet;

  protected HeavySpringTestCase(boolean modifiable) {

    myModifiable = modifiable;
  }

  protected void setUp() throws Exception {
    super.setUp();

    final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = JavaTestFixtureFactory.createFixtureBuilder();

    myTempDirTestFixture = IdeaTestFixtureFactory.getFixtureFactory().createTempDirTestFixture();
    myTempDirTestFixture.setUp();

    final JavaModuleFixtureBuilder moduleBuilder = projectBuilder.addModule(JavaModuleFixtureBuilder.class);
//    moduleBuilder.addLibraryJars("spring", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(), "spring2.jar");
    if (myModifiable) {
      moduleBuilder.addContentRoot(myTempDirTestFixture.getTempDirPath());
      FileUtil.copyDir(new File(getTestDataPath()), new File(myTempDirTestFixture.getTempDirPath()));
    }
    else {
      moduleBuilder.addContentRoot(getTestDataPath());
    }
    configureModule(moduleBuilder);
    myFixture = projectBuilder.getFixture();
    myFixture.setUp();

    myProject = myFixture.getProject();
    myModule = myFixture.getModule();
    ((ProjectComponent)ProjectRootManager.getInstance(myProject)).projectOpened();
    PsiDocumentManager.getInstance(myProject).commitAllDocuments();

    VirtualFileManager.getInstance().refresh(false);
  }

  protected void configureModule(JavaModuleFixtureBuilder moduleBuilder) throws Exception {}

  protected void addSpringJar(final JavaModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addLibraryJars("spring", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(), "spring2.jar");
  }

  protected void tearDown() throws Exception {
    ((ProjectComponent)ProjectRootManager.getInstance(myProject)).projectClosed();
    myFixture.tearDown();
    myTempDirTestFixture.tearDown();
    myFixture = null;
    myTempDirTestFixture = null;
    myFacet = null;
    myModule = null;
    myProject = null;
    super.tearDown();
  }

  protected SpringFileSet configureFileSet() throws Throwable {
    myFacet = createFacet();

    final SpringFacetConfiguration configuration = myFacet.getConfiguration();
    final Set<SpringFileSet> list = configuration.getFileSets();
    final SpringFileSet fileSet = new SpringFileSet("", "default", configuration);
    list.add(fileSet);
    return fileSet;
  }

  protected SpringFacet createFacet() {
    final RunResult<SpringFacet> runResult = new WriteCommandAction<SpringFacet>(myFixture.getProject()) {
      protected void run(final Result<SpringFacet> result) throws Throwable {
        String name = SpringFacetType.INSTANCE.getPresentableName();
        final SpringFacet facet = FacetManager.getInstance(myFixture.getModule()).addFacet(SpringFacetType.INSTANCE, name, null);
        result.setResult(facet);
      }
    }.execute();
    final Throwable throwable = runResult.getThrowable();
    if (throwable != null) {
      throw new RuntimeException(throwable);
    }

    return runResult.getResultObject();
  }

  protected VirtualFile addFile(final SpringFileSet fileSet, final String path) {
    
    final String dir = myModifiable ? myTempDirTestFixture.getTempDirPath() : getTestDataPath();
    final VirtualFile file = getFile(dir + "/" + path);
    assert file != null : "cannot find file: " + path;
    fileSet.addFile(file);

    final SpringFacet springFacet = SpringFacet.getInstance(myModule);
    assert springFacet != null;
    springFacet.getConfiguration().setModified();

    return file;
  }
}
