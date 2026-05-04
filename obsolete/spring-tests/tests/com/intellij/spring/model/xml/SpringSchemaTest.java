/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml;

import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

import java.io.File;

/**
 * @author Dmitry Avdeev
 */
public class SpringSchemaTest extends SpringHighlightingTestCase<JavaModuleFixtureBuilder> {

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.addLibraryJars("spring", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(), "spring-2.0.6.jar");
    moduleBuilder.addLibraryJars("spring", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(), "spring-modules-cache.jar");
  }

  public void testSchemasUpdate() throws Throwable {
    myFixture.testHighlighting("testSchemasUpdate.xml");
  }

  public void testXsdInLibrary() throws Throwable {
    final VirtualFile file = JarFileSystem.getInstance().findFileByPath(PathManager.getHomePath().replace(File.separatorChar, '/') +
                                                                        super.getBasePath() +
                                                                        "spring-modules-cache.jar!/org/springmodules/cache/config/springmodules-cache.xsd");
    myFixture.testHighlighting(true, true, true, file);
  }

  public void testXsdInLibraryWithImport() throws Throwable {
    final VirtualFile file = JarFileSystem.getInstance().findFileByPath(PathManager.getHomePath().replace(File.separatorChar, '/') +
                                                                        super.getBasePath() +
                                                                        "spring-modules-cache.jar!/org/springmodules/cache/config/ehcache/springmodules-ehcache.xsd");
    myFixture.testHighlighting(true, true, true, file);
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }

}
