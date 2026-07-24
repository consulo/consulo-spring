/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.JavaTestFixtureFactory;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public abstract class LightSpringTestCase extends BasicSpringTestCase {

  protected Project myProject;
  private IdeaProjectTestFixture myFixture;

  protected void setUp() throws Exception {
    super.setUp();

    myFixture = JavaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder().getFixture();
    myFixture.setUp();
    myProject = myFixture.getProject();

  }

  protected void tearDown() throws Exception {
    myFixture.tearDown();
    myFixture = null;
    myProject = null;
    super.tearDown();
  }

  @NotNull
  protected <T extends DomElement> DomFileElement<T> getFileElement(String path, Class<T> clazz, Project project) {
    String url = getTestDataPath() + path;
    try {
      File file = new File(url);
      char[] chars = FileUtil.loadFileText(file);
      String text = new String(chars);
      String fileName = file.getName();
      FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(fileName);
      PsiFile psiFile =
        PsiFileFactory.getInstance(PsiManager.getInstance(project).getProject()).createFileFromText(fileName, fileType, text);
      DomFileElement<T> element = DomManager.getDomManager(project).getFileElement((XmlFile)psiFile, clazz);
      assert element != null;
      return element;
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
