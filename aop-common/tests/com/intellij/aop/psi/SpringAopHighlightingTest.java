/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopLiteFixture;
import com.intellij.aop.jam.AopConstants;
import com.intellij.aop.jam.ArgNamesErrorsInspection;
import com.intellij.aop.jam.ArgNamesWarningsInspection;
import com.intellij.aop.jam.DeclareParentsInspection;
import com.intellij.codeInspection.deadCode.DeadCodeInspection;
import com.intellij.codeInspection.unusedSymbol.UnusedSymbolLocalInspection;
import com.intellij.facet.FacetManager;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.xml.util.XmlUtil;
import consulo.application.Result;
import consulo.ide.impl.idea.openapi.progress.impl.ProgressManagerImpl;
import com.intellij.spring.SpringApplicationComponent;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.facet.SpringFacetType;
import com.intellij.spring.model.highlighting.*;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.LocalInspectionTool;

/**
 * @author peter
 */
public class SpringAopHighlightingTest extends JavaCodeInsightFixtureTestCase {

  protected void setUp() throws Exception {
    super.setUp();

    AopLiteFixture.addAopAnnotations(myFixture);

    ExternalResourceManagerEx.getInstanceEx().addIgnoredResource(SpringConstants.AOP_NAMESPACE);
    ExternalResourceManagerEx.getInstanceEx().addIgnoredResource(SpringConstants.TX_NAMESPACE);
    ExternalResourceManagerEx.getInstanceEx().addIgnoredResource(SpringConstants.BEANS_XSD);
    ExternalResourceManagerEx.getInstanceEx().addIgnoredResource(XmlUtil.XML_SCHEMA_INSTANCE_URI);
  }

  public void testAdviceShouldDefinePointcut() throws Throwable {
    myFixture.addClass("package java.lang; public class String { }");
    doTest("advice-should-define-pointcut.xml", new SpringAopErrorsInspection());
  }

  public void testArgNamesErrors() throws Throwable {
    myFixture.addClass("package org.aspectj.lang; class ProceedingJoinPoint {}");
    myFixture.addClass("class BeanClass {" +
                       "public void writeRes(int arg1, int arg2) {} " +
                       "public void writeRes2(org.aspectj.lang.ProceedingJoinPoint arg1) {} " +
                       "}");
    myFixture.enableInspections(new ArgNamesErrorsInspection(), new ArgNamesWarningsInspection());
    myFixture.copyFileToProject("arg-names-errors.xml");
    createFacet();
    myFixture.testHighlighting(true, false, false, "arg-names-errors.xml");
  }

  private void createFacet() {
    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        FacetManager.getInstance(myModule).addFacet(SpringFacetType.INSTANCE, "s", null);
      }
    }.execute();
  }

  public void testIntroductions() throws Throwable {
    myFixture.addClass("package java.lang; public interface Runnable { void run(); }");
    myFixture.addClass("package java.lang; public abstract class AbstractRunnable implements Runnable { }");
    myFixture.addClass("package java.lang; public class RunnableImpl implements Runnable { }");
    myFixture.addClass("package java.lang; public class Object { }");

    doTest("introductions.xml", new DeclareParentsInspection());
  }

  public void testAdvisors() throws Throwable {
    myFixture.addClass("package org.aopalliance.aop; public interface Advice {}");
    myFixture.addClass("public class BeanClass implements org.aopalliance.aop.Advice {}");
    myFixture.addClass("public class BeanClass2 {}");

    doTest("advisors.xml", new SpringModelInspection(), new InjectionValueTypeInspection(), new JdkProxiedBeanTypeInspection());
  }

  public void testNonDeclaredPointcutParameters() throws Throwable {
    myFixture.addClass("public class BeanClass {" +
                       "void before1(int param) {} " +
                       "void before2(" + AopConstants.PROCEEDING_JOIN_POINT + " p) {} " +
                       "}");
    doTest("nonDeclaredParams.xml");
  }

  public void testPointcutIdAndAspectRef() throws Throwable {
    doTest("pointcut-id-and-aspect-ref.xml", new SpringAopWarningsInspection());
  }

  public void testAtThis() throws Throwable {
    myFixture.addClass("public @interface Anno {}");

    doTest("at-this.xml", new SpringAopErrorsInspection());
  }

  public void testUnboundParameters() throws Throwable {
    myFixture.addClass("public class BeanClass {" +
                       "void before1(int param) {} " +
                       "void before2() {} " +
                       "}");
    doTest(getTestName(true) + ".xml", new ArgNamesErrorsInspection());
  }

  private void doTest(String fileName, LocalInspectionTool... inspections) throws Throwable {
    myFixture.enableInspections(inspections);
    myFixture.copyFileToProject(fileName);
    myFixture.testHighlighting(true, false, false, fileName);
  }

  public void testAopDeclareParents() throws Throwable {
    myFixture.enableInspections(SpringApplicationComponent.getSpringInspectionClasses());
    createFacet();

    myFixture.copyFileToProject("spring-aop-declare-parents.java");
    myFixture.testHighlighting(false, false, false, "spring-aop-declare-parents.xml");
  }

  public void testAopJdkProxyType() throws Throwable {
    myFixture.enableInspections(SpringApplicationComponent.getSpringInspectionClasses());
    createFacet();

    myFixture.copyFileToProject("spring-proxy-type.java");
    myFixture.testHighlighting(true, false, false, "spring-jdk-proxy-type.xml");
  }

  public void testAopJdkProxyTimeout() throws Throwable {
    myFixture.enableInspections(SpringApplicationComponent.getSpringInspectionClasses());
    createFacet();

    myFixture.configureByFiles("spring-jdk-proxy-timeout.xml", "spring-proxy-type.java");
    consulo.ide.impl.idea.openapi.progress.impl.ProgressManagerImpl.setNeedToCheckCancel(true);
    try {
      JdkProxiedBeanTypeInspection.TEST_ME = true;
      myFixture.testHighlighting(false, false, true);
    }
    finally {
      JdkProxiedBeanTypeInspection.TEST_ME = false;
    }
  }

  public void testAopJdkProxyTypeFix() throws Throwable {
    myFixture.enableInspections(SpringApplicationComponent.getSpringInspectionClasses());
    createFacet();

    myFixture.configureByFiles("spring-jdk-proxy-type-fix.xml", "spring-proxy-type.java");
    myFixture.launchAction(assertOneElement(myFixture.filterAvailableIntentions("Use CGLIB proxying")));
    myFixture.checkResultByFile("spring-jdk-proxy-type-fix_after.xml");
  }

  public void testAopCglibProxyType() throws Throwable {
    myFixture.enableInspections(SpringApplicationComponent.getSpringInspectionClasses());
    createFacet();

    myFixture.copyFileToProject("spring-proxy-type.java");
    myFixture.testHighlighting(false, false, false, "spring-cglib-proxy-type.xml");
  }

  public void testAdviceMatchingAspectBean() throws Throwable {
    myFixture.enableInspections(SpringApplicationComponent.getSpringInspectionClasses());
    createFacet();
    
    myFixture.copyFileToProject("AdviceMatchingAspectBean.java");
    doTest("advice-matching-aspect-bean.xml");
  }

  public void testDoublePointcutUsageBoundParameterError() throws Throwable {
    myFixture.enableInspections(new ArgNamesErrorsInspection());
    createFacet();

    myFixture.addClass("class Aspect1 {\n" +
                       "  void foo(String s);\n" +
                       "  void bar(String s);\n" +
                       "}");
    doTest(getTestName(false) + ".xml");
  }

  public void testDuplicateMethodVariants() throws Throwable {
    myFixture.addClass("package foo; public class Map { void clear(); void clone(); }");
    myFixture.addClass("package foo; public class HashMap implements Map { public void clear(); public void clone(); }");

    assertSameElements(myFixture.getCompletionVariants(getTestName(false) + ".xml"), "clear", "clone");
  }

  public void testAspectMethodsImplicitUsage() throws Exception {
    myFixture.enableInspections(new UnusedSymbolLocalInspection(), new DeadCodeInspection());
    myFixture.testHighlighting(true, false, false, "MyAspect.java");
  }
  
  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/aop/";
  }


}
