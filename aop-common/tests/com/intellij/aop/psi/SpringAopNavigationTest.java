/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.facet.FacetManager;
import consulo.application.Result;
import consulo.language.editor.WriteCommandAction;
import com.intellij.spring.facet.SpringFacetType;

/**
 * @author peter
 */
public class SpringAopNavigationTest extends AopNavigationTestCase {

  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/aop/navigation/";
  }

  protected void setUp() throws Exception {
    super.setUp();

    myFixture.addClass("package org.springframework.aop.aspectj.annotation;" +
                       "public class AnnotationAwareAspectJAutoProxyCreator{}");

    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        FacetManager.getInstance(myModule).addFacet(SpringFacetType.INSTANCE, "z", null);
      }
    }.execute();
  }

  public void testPointcutsInitialization() throws Throwable {
    String javaFile = "PointcutsInitialization.java";
    myFixture.copyFileToProject(javaFile, "foo/bar/testcase/Classes.java");
    String xmlFile = "pointcuts-initialization.xml";

    checkNavigation(xmlFile, false, "aop:after", "foo.bar.testcase.Target#foo()");
    checkNavigation("foo/bar/testcase/Classes.java", false, "aop:after");
  }

  public void testThisAwareOfDeclareParents() throws Throwable {
    String javaFile = "ThisAwareOfDeclareParents.java";
    myFixture.copyFileToProject(javaFile, "foo/bar/aop/Classes.java");
    String xmlFile = "this-aware-of-declare-parents.xml";
    myFixture.copyFileToProject(xmlFile);

    checkNavigation(xmlFile, false, "aop:around",
                    "aop:declare-parents",
                    "aop:around",
                    "aop:around",
                    "aop:declare-parents",
                    "foo.bar.aop.Target\n" + "foo.bar.aop.Subclass",
                    "foo.bar.aop.Target#foo()\n" +
                    "foo.bar.aop.Subclass#foo()\n" +
                    "foo.bar.aop.Impl#foo()");
    checkNavigation("foo/bar/aop/Classes.java", false, "aop:declare-parents",
                    "aop:around",
                    "aop:around",
                    "aop:declare-parents",
                    "aop:around");
  }

  public void testDefaultProxyType() throws Throwable {
    myFixture.copyFileToProject("ProxyTypeBeans.java", "foo/PTB.java");
    String xmlFile = "default-proxy-type.xml";
    myFixture.copyFileToProject(xmlFile);
    checkNavigation(xmlFile, false, "aop:around", "aop:around", "foo.TargetClassNoInterface#classMethod()\n" +
                              "foo.TargetClassWithInterface#interfaceMethod()");
  }

  public void testCglibProxyType() throws Throwable {
    myFixture.copyFileToProject("ProxyTypeBeans.java", "foo/PTB.java");
    String xmlFile = "cglib-proxy-type.xml";
    myFixture.copyFileToProject(xmlFile);
    checkNavigation(xmlFile, false, "aop:around", "aop:around", "foo.TargetClassWithInterface#interfaceMethod()\n" +
                       "foo.TargetClassWithInterface#classMethod()\n" +
                       "foo.TargetClassNoInterface#classMethod()");
  }

  public void testAdviceAppliedToInnerBeans() throws Throwable {
    String javaFile = "AdviceAppliedToInnerBeans.java";
    myFixture.copyFileToProject(javaFile);
    String xmlFile = "advice-applied-to-inner-beans.xml";
    myFixture.copyFileToProject(xmlFile);

    checkNavigation(xmlFile, false, "aop:before", "aop:before",
                    "foo.bar.testcase.TargetA#methodA()\n" +
                    "foo.bar.testcase.TargetB#methodB()");
    checkNavigation(javaFile, false, "aop:before", "aop:before");
  }

  public void testCircularInheritance() throws Throwable {
    String javaFile = "CircularInheritance.java";
    myFixture.copyFileToProject(javaFile);
    String xmlFile = "circular-inheritance.xml";
    myFixture.copyFileToProject(xmlFile);

    checkNavigation(xmlFile, false, "a.b.C#foo()", "aop:before");
    checkNavigation(javaFile, false, "aop:before");
  }

  public void testBeanPointcut() throws Throwable {
    String javaFile = "BeanPointcut.java";
    myFixture.copyFileToProject(javaFile);
    String xmlFile = "BeanPointcut.xml";
    myFixture.copyFileToProject(xmlFile);

    checkNavigation(javaFile, false, "aop:before", "aop:before");
    checkNavigation(xmlFile, false, "aop:before", "aop:before", "Bean3#foo()\n" +
                             "Bean2#foo()");
  }

  public void testComponentScan() throws Throwable {
    myFixture.addClass("package org.springframework.stereotype; public @interface Component { String value(); }");

    String javaFile = "foo/bar/classes.java";
    myFixture.copyFileToProject(getTestName(false) + ".java", javaFile);
    String xmlFile = getTestName(false) + ".xml";
    myFixture.copyFileToProject(xmlFile);

    checkNavigation(javaFile, false, "aop:before");
    checkNavigation(xmlFile, false, "foo.bar.Bean#foo()");
  }

  public void testComponentScanAspectBean() throws Throwable {
    myFixture.addClass("package org.springframework.stereotype; public @interface Component { String value(); }");

    String javaFile = "foo/bar/classes.java";
    myFixture.copyFileToProject(getTestName(false) + ".java", javaFile);
    String xmlFile = getTestName(false) + ".xml";
    myFixture.copyFileToProject(xmlFile);

    checkNavigation(javaFile, false, "aop:after\naop:before");
    checkNavigation(xmlFile, false, "foo.bar.Bean#foo()", "foo.bar.Bean#foo()");
  }

  public void testAdviceOrder() throws Throwable {
    myFixture.addClass("package org.springframework.core; public interface Ordered { int getOrder(); }");
    myFixture.addClass("package org.springframework.core.annotation; public @interface Order(int value();}");
    myFixture.addClass("package org.springframework.transaction.annotation; public @interface Transactional{}");

    String javaFile = getTestName(false) + ".java";
    myFixture.copyFileToProject(javaFile);
    String xmlFile = getTestName(false) + ".xml";
    myFixture.copyFileToProject(xmlFile);

    checkNavigation(javaFile, true, "tx:annotation-driven\n" +
                                    "aop:advisor\n" +
                                    "AspectBean#inAspect()\n" +
                                    "OrderedAspectBean#inOrderedAspect()\n" +
                                    "aop:before\n" +
                                    "aop:after", "Bean#foo()", "Bean#foo()");
    checkNavigation(xmlFile, true, "tx:annotation-driven\n" +
                                    "aop:advisor\n" +
                                    "AspectBean#inAspect()\n" +
                                    "OrderedAspectBean#inOrderedAspect()\n" +
                                    "aop:before\n" +
                                    "aop:after",
                    "Bean#foo()", "Bean#foo()", "Bean#foo()", "Bean#foo()");
  }

  public void testAfterReturningAdviceOrder() throws Throwable {
    String javaFile = getTestName(false) + ".java";
    myFixture.copyFileToProject(javaFile);
    String xmlFile = getTestName(false) + ".xml";
    myFixture.copyFileToProject(xmlFile);

    checkNavigation(javaFile, true,
                    "aop:before\n" +
                    "aop:around\n" +
                    "aop:after-throwing\n" +
                    "aop:after\n" +
                    "aop:after-returning");
  }

  public void testTargetInheritedMethods() throws Throwable {
    String javaFile = getTestName(false) + ".java";
    myFixture.copyFileToProject(javaFile);
    String xmlFile = getTestName(false) + ".xml";
    myFixture.copyFileToProject(xmlFile);

    checkNavigation(xmlFile, false, "aop:before", "Bean#bar()\n" +
                             "Super#foo()");
    checkNavigation(javaFile, false, "aop:before", "aop:before");
  }

  public void testHonorDeclareParentsWithParameter() throws Throwable {
    String javaFile = getTestName(false) + ".java";
    myFixture.copyFileToProject(javaFile);
    String xmlFile = getTestName(false) + ".xml";
    myFixture.copyFileToProject(xmlFile);

    checkNavigation(xmlFile, false, "aop:before", "aop:declare-parents", "Bean", "Bean#bar()");
    checkNavigation(javaFile, false, "aop:declare-parents", "aop:before");
  }

  public void testExactReturnType() throws Throwable {
    String javaFile = getTestName(false) + ".java";
    myFixture.copyFileToProject(javaFile);
    String xmlFile = getTestName(false) + ".xml";
    myFixture.copyFileToProject(xmlFile);

    checkNavigation(xmlFile, false, "aop:after\naop:before", "Bean#xFoo()\nBean#yFoo()", "Bean#xFoo()\nBean#xBar()\nBean#yFoo()\nBean#yBar()");
    checkNavigation(javaFile, false, "aop:before\naop:after", "aop:after", "aop:before\naop:after", "aop:after");
  }

  public void testAnnotationDrivenPrivateMethods() throws Throwable {
    myFixture.addClass("package org.springframework.transaction.annotation; public @interface Transactional{}");

    String javaFile = getTestName(false) + ".java";
    myFixture.copyFileToProject(javaFile);
    String xmlFile = getTestName(false) + ".xml";
    myFixture.copyFileToProject(xmlFile);

    checkNavigation(xmlFile, false, "tx:annotation-driven", "Bean#foo()");
    checkNavigation(javaFile, false, "tx:annotation-driven");
  }

  public void testAnnotationDrivenSuperMethods() throws Throwable {
    myFixture.addClass("package org.springframework.transaction.annotation; public @interface Transactional{}");

    String javaFile = getTestName(false) + ".java";
    myFixture.copyFileToProject(javaFile);
    String xmlFile = getTestName(false) + ".xml";
    myFixture.copyFileToProject(xmlFile);

    checkNavigation(javaFile, false, "tx:annotation-driven", "tx:annotation-driven", "tx:annotation-driven");
    checkNavigation(xmlFile, false, "tx:annotation-driven", "tx:annotation-driven",
                    "Sub#fromSuper()\n" +
                    "Super#fromSuper()\n" +
                    "Bean#foo()");
  }

}
