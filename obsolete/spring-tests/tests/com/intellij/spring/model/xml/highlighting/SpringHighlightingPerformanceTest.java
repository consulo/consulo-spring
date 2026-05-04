/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import consulo.ide.impl.idea.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.SpringApplicationComponent;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import java.util.function.Function;
import consulo.util.lang.ref.PatchedWeakReference;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.semantic.SemService;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SpringHighlightingPerformanceTest extends SpringHighlightingTestCase {
  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
  }


  public void testLargeProject() throws Throwable {
    myFixture.copyFileToProject("performance.xml");
    final String fileText = consulo.ide.impl.idea.openapi.vfs.VfsUtil.loadText(myFixture.getTempDirFixture().getFile("performance.xml"));
    final SpringFileSet fileSet = configureFileSet();
    final String path = myFixture.getTempDirFixture().getTempDirPath();
    final List<PsiFile> files = new ArrayList<PsiFile>();
    for (int i = 0; i < 10; i++) {
      final String fileName = "performance" + i + ".xml";
      final File file = new File(path, fileName);
      file.createNewFile();
      final VirtualFile vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
      VfsUtil.saveText(vFile, fileText);

      fileSet.addFile(vFile);
      files.add(PsiManager.getInstance(myProject).findFile(vFile));
    }

    final List<LocalInspectionTool> inspections = getInspections();

    JavaPsiFacade.getInstance(myProject).findClass(CommonClassNames.JAVA_LANG_OBJECT, GlobalSearchScope.allScope(myProject)); //don't measure stubs rebuild

    final long time = System.currentTimeMillis();
    for (final PsiFile file : files) {
      for (final LocalInspectionTool inspection : inspections) {
        inspection.checkFile(file, InspectionManager.getInstance(myProject), false);

        PatchedWeakReference.clearAll();
      }
      System.out.println("file = " + file);
      SemService.getSemService(myProject).clearCache();
    }
    IdeaTestUtil.assertTiming("", 16000, System.currentTimeMillis() - time);
  }

  private static List<LocalInspectionTool> getInspections() {
    final List<LocalInspectionTool> inspections = ContainerUtil.map(
      ApplicationManager.getApplication().getComponent(SpringApplicationComponent.class).getInspectionClasses(), new Function<Class, LocalInspectionTool>() {
      public LocalInspectionTool fun(final Class aClass) {
        try {
          return (LocalInspectionTool)aClass.newInstance();
        }
        catch (Throwable e) {
          throw new RuntimeException(e);
        }
      }
    });
    return inspections;
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }
}
