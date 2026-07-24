/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopPointcut;
import com.intellij.aop.jam.AopConstants;
import com.intellij.aop.jam.AopPointcutImpl;
import com.intellij.openapi.application.ex.PathManagerEx;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.LiteFixture;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import consulo.language.util.IncorrectOperationException;

import jakarta.annotation.Nullable;
import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.List;

/**
 * @author peter
 */
public class AopHighlightingTest extends JavaCodeInsightFixtureTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    myFixture.addClass("public class SomeClass {" +
                                        "public void nonPointcut() {}" +
                                        "public void nonPointcut(int a) {}" +
                                        "}");
    myFixture.addClass("package abc; public class SomeClass {}");
    myFixture.addClass("package abc; public @interface Anno {}");
    myFixture.addClass("public class List<T> {}");

    final PsiClass contextClass = myFixture.addClass(
      "public class Context {" +
      "@" + AopConstants.POINTCUT_ANNO + "(\"\") void pointcut() {}" +
      " void contextMethod(String s) {}" +
      "}");
    final PsiMethod method = contextClass.findMethodsByName("pointcut", false)[0];
    final AopPointcutImpl pointcut = new AopPointcutImpl() {
      @Nonnull
      public PsiMethod getPsiElement() {
        return method;
      }
    };

    contextClass.putUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL, new MockAopModel(new AllAdvisedElementsSearcher(myFixture.getPsiManager())) {
      @Nullable
      public PsiMethod getPointcutMethod() {
        return contextClass.findMethodsByName("contextMethod", false)[0];
      }

      public List<? extends AopPointcut> getPointcuts() {
        return Arrays.asList(pointcut);
      }
    });
    myFixture.setFileContext(contextClass);
  }

  public void testInvalidReference1() throws Throwable { doTest(); }
  public void testInvalidReference2() throws Throwable { doTest(); }
  public void testInvalidReference3() throws Throwable { doTest(); }
  public void testInvalidReference4() throws Throwable { doTest(); }
  public void testInvalidReference5() throws Throwable { doTest(); }
  public void testInvalidReference6() throws Throwable { doTest(); }
  public void testInvalidReference7() throws Throwable { doTest(); }
  public void testInvalidReference8() throws Throwable { doTest(); }
  public void testInvalidReference9() throws Throwable { doTest(); }
  public void testInvalidReference10() throws Throwable { doTest(); }

  public void testIncompatibleArgumentNumber() throws Throwable { doTest(); }

  public void testUnresolvedParameterInJava() throws Throwable { doTest(); }
  
  public void testOverloadedMethodReference() throws Throwable { doTest(); }

  public void testStartEllipsis() throws Throwable { doTest(); }
  public void testEndEllipsis() throws Throwable { doTest(); }
  public void testDoubleEllipsis() throws Throwable { doTest(); }
  public void testDoubleEllipsis2() throws Throwable { doTest(); }
  public void testValidVarargs() throws Throwable { doTest(); }
  public void testInvalidVarargs() throws Throwable { doTest(); }
  public void testVarargsInGenerics() throws Throwable { doTest(); }
  public void testVarargsInArgs() throws Throwable { doTest(); }
  public void testAsteriskInGenerics1() throws Throwable { doTest(); }
  public void testAsteriskInGenerics2() throws Throwable { doTest(); }
  public void testPlusInGenerics1() throws Throwable { doTest(); }
  public void testPlusInGenerics2() throws Throwable { doTest(); }

  public void testArrayInThis() throws Throwable { doTest(); }

  public void testAnnotationCheckAtArgs() throws Throwable { doTest(); }
  public void testAnnotationCheckAtAnnotation() throws Throwable { doTest(); }
  public void testAnnotationCheckAtTarget() throws Throwable { doTest(); }
  public void testAnnotationCheckAtThis() throws Throwable { doTest(); }
  public void testAnnotationCheckAtWithin() throws Throwable { doTest(); }

  public void testAnnotationCheckAtAnnotationParameter() throws Throwable {
    myFixture.getJavaFacade().findClass("Context").putUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL, new MockAopModel(new AllAdvisedElementsSearcher(myFixture.getPsiManager())) {
      @Nullable
      public PsiMethod getPointcutMethod() {
        String text = "void m(abc.Anno abc, int i) {}";
        try {
          return myFixture.getJavaFacade().getParserFacade().createMethodFromText(text, null);
        }
        catch (IncorrectOperationException e) {
          throw new RuntimeException(e);
        }
      }
    });
    doTest();
  }

  public void testAndOrNotInJava() throws Throwable { doTest(); }
  public void testAndOrNotInXml() throws Throwable {
    XmlTag tag = XmlElementFactory.getInstance(getProject()).createTagFromText("<a/>");
    myFixture.setFileContext(tag);
    tag.putUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL, new MockAopModel(new AllAdvisedElementsSearcher(myFixture.getPsiManager())) { });
    doTest();
  }

  public void testBeanDesignator() throws Throwable { doTest(); }

  public void testResolveInSamePackage() throws Throwable {
    PsiClass context = myFixture.addClass("package foo; class Context {}");
    myFixture.addClass("package foo; class Foo {}");
    context.putUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL, new MockAopModel(new AllAdvisedElementsSearcher(myFixture.getPsiManager())){});
    myFixture.setFileContext(context);
    doTest();
  }


  private void doTest() throws Throwable {
    String path = PathManagerEx.getTestDataPath() + "/aop/highlighting/" + getTestName(true) + ".txt";
    myFixture.configureByText(AopPointcutExpressionFileType.INSTANCE, LiteFixture.loadFileText(path));
    myFixture.checkHighlighting();
  }
}
