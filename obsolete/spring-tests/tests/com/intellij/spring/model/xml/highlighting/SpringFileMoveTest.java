/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

/**
 * @author Dmitry Avdeev
 */
public class SpringFileMoveTest extends SpringHighlightingTestCase {

  public void testMoveFile() throws Throwable {
    myFixture.copyFileToProject("src2/import2.xml");
    myFixture.moveFile("src1/move.xml", "src2");
    myFixture.checkResultByFile("src2/move.xml", "src1/move_after.xml", false);
  }

  public void testMoveFileToPackage() throws Throwable {
    myFixture.copyFileToProject("src2/foo/fooImport2.xml");
    myFixture.moveFile("src1/move.xml", "src2/foo");
    myFixture.checkResultByFile("src2/foo/move.xml", "src1/move_after.xml", false);
  }

  public void testMoveImport() throws Throwable {
    myFixture.moveFile("src1/import.xml", "src2", "src1/import.xml", "src2/import2.xml", "src1/move.xml");
    myFixture.checkResultByFile("src1/move.xml", "src1/move_after_import.xml", false);
  }

  public void testMoveFooImport() throws Throwable {
    myFixture.moveFile("src1/foo/fooImport.xml", "src2/foo", "src1/import.xml", "src2/foo/fooImport2.xml", "src1/move.xml");
    myFixture.checkResultByFile("src1/move.xml", "src1/move_after_foo_import.xml", false);
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.addSourceRoot("src1");
    moduleBuilder.addSourceRoot("src2");
  }

  @NonNls
  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/moving";
  }
}
