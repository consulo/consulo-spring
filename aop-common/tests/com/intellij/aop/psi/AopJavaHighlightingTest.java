/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopLiteFixture;
import com.intellij.aop.jam.*;
import consulo.application.Result;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.LocalInspectionTool;
import com.intellij.codeInspection.unusedSymbol.UnusedSymbolLocalInspection;
import com.intellij.facet.FacetManager;
import com.intellij.spring.facet.SpringFacetType;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;

/**
 * @author peter
 */
public class AopJavaHighlightingTest extends JavaCodeInsightFixtureTestCase {

  protected void setUp() throws Exception {
    super.setUp();

    AopLiteFixture.addAopAnnotations(myFixture);

    myFixture.addClass("package org.aspectj.lang; public interface JoinPoint  { static interface StaticPart {} }");
    myFixture.addClass("package org.aspectj.lang; public interface ProceedingJoinPoint  {}");
  }

  private void doTest(LocalInspectionTool... tools) throws Throwable {
    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        FacetManager.getInstance(myModule).addFacet(SpringFacetType.INSTANCE, SpringFacetType.INSTANCE.getPresentableName(), null);
      }
    }.execute();

    myFixture.enableInspections(tools);
    String path = getTestName(true) + ".java";
    myFixture.copyFileToProject(path);
    myFixture.testHighlighting(true, false, false, path);
  }

  protected String getBasePath() {
    return "/testData/aop/highlighting/java";
  }

  public void testArgNamesErrors() throws Throwable { doTest(new ArgNamesErrorsInspection()); }

  public void testArgNamesWarnings() throws Throwable { doTest(new ArgNamesWarningsInspection()); }

  public void testAfterReturning() throws Throwable { doTest(new ArgNamesErrorsInspection()); }

  public void testAfterThrowing() throws Throwable { doTest(new ArgNamesErrorsInspection()); }

  public void testDeclareParents() throws Throwable { doTest(new DeclareParentsInspection()); }

  public void testRecursivePointcuts() throws Throwable { doTest(); }

  public void testUnusedPointcutParameter() throws Throwable { doTest(new UnusedSymbolLocalInspection()); }

  public void testProceedingJoinPointInNonAround() throws Throwable { doTest(new ArgNamesWarningsInspection()); }

  public void testPointcutMethodStyle() throws Throwable { doTest(new PointcutMethodStyleInspection(), new AroundAdviceStyleInspection()); }

  public void testAroundAdviceStyle() throws Throwable { doTest(new AroundAdviceStyleInspection(), new PointcutMethodStyleInspection()); }

  public void testInferParameterNames() throws Throwable {
    myFixture.addClass("package java.lang.annotation; public interface Annotation {}");

    myFixture.enableInspections(new AopInspectionToolProvider());
    doTest();
  }

  public void testMultipleArgNamesWarnings() throws Throwable {
    myFixture.enableInspections(new AopInspectionToolProvider());
    doTest();
  }

}
