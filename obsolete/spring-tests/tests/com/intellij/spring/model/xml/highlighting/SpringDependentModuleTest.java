/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;

import java.io.File;
import java.io.IOException;

/**
 * @author Dmitry Avdeev
 */
public class SpringDependentModuleTest extends SpringHighlightingTestCase {
  private Module myDependency;

  public void testInclude() throws Throwable {
    myFixture.testHighlighting("main/spring-beans.xml", "dependent/include.xml");
  }

  public void testClassGutter() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "main/gutter.xml");
    final SpringFileSet dependency = configureFileSet("dependent", myDependency);
    addFileToSet(dependency, "dependent/include.xml");
    final GutterIconRenderer iconRenderer = myFixture.findGutter("dependent/Bean.java");
    assert iconRenderer != null;
  }


  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    addSpring_2_5_Library(moduleBuilder);
  }

  protected void setUp() throws Exception {

    super.setUp();
    final String path = myFixture.getTempDirPath();
    final Module module = myModule;
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {

        try {
          myFixture.copyFileToProject("/main/spring-beans.xml");
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
        final ModifiableRootModel rootModel = ModuleRootManager.getInstance(myModule).getModifiableModel();
        final VirtualFile root = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(path + "/main"));
        assert root != null;

        final ContentEntry contentEntry = rootModel.addContentEntry(root);
        contentEntry.addSourceFolder(root, false);

        // export spring library
        for (OrderEntry orderEntry : rootModel.getOrderEntries()) {
          if (orderEntry instanceof LibraryOrderEntry) {
            ((LibraryOrderEntry)orderEntry).setExported(true);
          }
        }

        rootModel.commit();

        configureDependentModule(module, path);
      }
    });

  }

  @Override
  protected void tearDown() throws Exception {
    myDependency = null;
    super.tearDown();
  }

  private void configureDependentModule(final Module module, final String path) {
    try {
      myFixture.copyFileToProject("/dependent/default.iml");
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    final ModifiableModuleModel moduleModel = ModuleManager.getInstance(module.getProject()).getModifiableModel();
    myDependency = moduleModel.newModule(path + "/dependent/default.iml", StdModuleTypes.JAVA);
    moduleModel.commit();
    final ModifiableRootModel rootModel = ModuleRootManager.getInstance(myDependency).getModifiableModel();
    final VirtualFile root = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(path + "/dependent"));
    assert root != null;

    final ContentEntry contentEntry = rootModel.addContentEntry(root);
    contentEntry.addSourceFolder(root, false);
    rootModel.commit();

    final ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
    model.addModuleOrderEntry(myDependency);
    model.commit();
  }

  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/dependentModules";
  }
}
