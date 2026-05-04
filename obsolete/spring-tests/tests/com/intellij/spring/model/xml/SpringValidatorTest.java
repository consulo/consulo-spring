/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml;

import com.intellij.codeInspection.InspectionManager;
import consulo.ide.impl.idea.codeInspection.ex.InspectionProfileImpl;
import com.intellij.compiler.options.ValidationConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import consulo.compiler.CompileContext;
import consulo.compiler.CompileStatusNotification;
import consulo.compiler.Compiler;
import consulo.compiler.CompilerManager;
import com.intellij.openapi.compiler.util.InspectionValidatorWrapper;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiManager;
import com.intellij.spring.SpringApplicationComponent;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.highlighting.SpringValidator;
import com.intellij.util.concurrency.Semaphore;

/**
 * @author Dmitry Avdeev
 */
public class SpringValidatorTest extends HeavySpringTestCase {

  public SpringValidatorTest() {
    super(false);
  }

  public void testSpringValidator() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFile(fileSet, "first.xml");

    ValidationConfiguration.getInstance(myProject).setSelected(new SpringValidator(new SpringApplicationComponent()).getDescription(), true);
    ValidationConfiguration.getInstance(myProject).VALIDATE_ON_BUILD = true;
    final InspectionProfileImpl inspectionProfile =
      (InspectionProfileImpl)InspectionProjectProfileManager.getInstance(myProject).getProjectProfileImpl();
    inspectionProfile.initInspectionTools();
    final CompilerManager compilerManager = CompilerManager.getInstance(myProject);
    final com.intellij.openapi.compiler.Compiler[] compilers = compilerManager.getCompilers(Compiler.class);
    for (Compiler compiler: compilers) {
      compilerManager.removeCompiler(compiler);
    }
    final int[] counter = new int[] { 0 };
    final SpringValidator validator =
      new SpringValidator(ApplicationManager.getApplication().getComponent(SpringApplicationComponent.class));
    compilerManager.addCompiler(new InspectionValidatorWrapper(compilerManager,
                                                               InspectionManager.getInstance(myProject),
                                                               InspectionProjectProfileManager.getInstance(myProject),
                                                               PsiDocumentManager.getInstance(myProject),
                                                               PsiManager.getInstance(myProject),
                                                               validator) {
      public ProcessingItem[] process(final CompileContext context, final ProcessingItem[] items) {
        counter[0] += items.length;
        return super.process(context, items);
      }
    });



    final Semaphore semaphore = new Semaphore();
    semaphore.down();
    compilerManager.make(myModule, new CompileStatusNotification() {
      public void finished(final boolean aborted, final int errors, final int warnings, final CompileContext compileContext) {
        assertEquals(1, counter[0]);
        assertEquals(0, errors);
        assertEquals(0, warnings);
        semaphore.up();
      }
    });
    assertTrue("timeout", semaphore.waitFor(10000));
  }

  @Override
  protected void setUp() throws Exception {
    InspectionProfileImpl.INIT_INSPECTIONS = true;
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    InspectionProfileImpl.INIT_INSPECTIONS = false;
  }

  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/validation";
  }
}
