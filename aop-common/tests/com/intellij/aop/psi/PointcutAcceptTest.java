/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.AopPointcut;
import com.intellij.aop.LocalAopModel;
import com.intellij.aop.jam.AopConstants;
import com.intellij.aop.jam.AopPointcutImpl;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.testFramework.LiteFixture;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import consulo.application.util.function.Processor;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.virtualFileSystem.fileType.FileType;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nullable;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author peter
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class PointcutAcceptTest extends JavaCodeInsightFixtureTestCase {
  private MockAopModel myAopModel;

  protected void setUp() throws Exception {
    super.setUp();
    myFixture.addClass("package java.lang; public class Object {}");
    myFixture.addClass("package annos; public @interface FooAnno {}");
    myFixture.addClass("package annos; @FooAnno public @interface BarAnno {}");
    myFixture.addClass("package java.lang; public @interface Deprecated {}");
    myFixture.addClass("package java.lang.annotation; public @interface Inherited {}");

    myFixture.addClass("package java.lang; public class Long {}");
    myFixture.addClass("package java.lang; public class Integer {}");
    myFixture.addClass("package java.lang; public class AbstractString {}");
    myFixture.addClass("package java.lang; @annos.FooAnno public class String extends AbstractString {}");
    myFixture.addClass("package java.lang; public class Exception {}");
    myFixture.addClass("package java.util; public class SomeExc {}");

    PsiClass psiClass = myFixture.addClass(
      "package java.lang;" +
      "import annos.*;" +
      "@" + CommonClassNames.JAVA_LANG_DEPRECATED + " " +
      "public class YObject {" +
      "public void foo();" +//0
      "public void foo(int a);" +//1
      "private void foo(int a);" +//2
      "public static void foo(int a, String b);" +//3
      "@FooAnno public final String foo(int a, String b);" +//4
      "@FooAnno @BarAnno public final String foobar(int a, String b);" +//5
      "@BarAnno public final String bar(int a, String b);" +//6
      "public final int bar(int a, long xxx, String b);" +//7
      "public final int bar(int a, long xxx, String b) throws java.lang.Exception;" + //8
      "public final int bar(int a, long xxx, String b) throws java.util.SomeExc, java.lang.Exception;" +//9
      "@" + CommonClassNames.JAVA_LANG_DEPRECATED + " public final int[] bar(int a, long xxx, String b) throws java.util.SomeExc, java.lang.Exception;" +//10
      "@" + AopConstants.POINTCUT_ANNO + "(\"execution(void foo(*))\") void pointcut();" + //11
      "@" + AopConstants.POINTCUT_ANNO + "(\"args(..,arg,..)\") public void paramPointcut(Object arg);" + //12
      "}");
    createAopPoincut(psiClass.findMethodsByName("pointcut", false)[0]);
    createAopPoincut(psiClass.findMethodsByName("paramPointcut", false)[0]);
    myAopModel = new MockAopModel(new AllAdvisedElementsSearcher(PointcutAcceptTest.this.getPsiManager())) {
      public List<? extends AopPointcut> getPointcuts() {
        return Collections.emptyList();
      }
    };
  }

  @Override
  protected void tuneFixture(JavaModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addJdk("nonexistent");
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
  }

  private void createAopPoincut(final PsiMethod method) {
    new AopPointcutImpl() {
      @Nonnull
      public PsiMethod getPsiElement() {
        return method;
      }

      @Nullable
      protected PsiPointcutExpression getPointcutExpression(@Nullable PsiAnnotationMemberValue value) {
        PsiPointcutExpression expression = parsePointcutExpression((String)((PsiLiteralExpression)value).getValue());
        expression.getContainingFile().setAopModel(new LocalAopModel(null, method, new AllAdvisedElementsSearcher(getPsiManager())));
        return expression;
      }
    };
  }

  public void testTypeReference() throws Throwable {
    AopReferenceHolder pattern = parseTypeExpression(CommonClassNames.JAVA_LANG_OBJECT);
    PsiClassType object = getJavaFacade().getElementFactory().createTypeByFQClassName(CommonClassNames.JAVA_LANG_OBJECT);
    PsiArrayType array = new PsiArrayType(object);
    assertEquals(PointcutMatchDegree.TRUE, pattern.accepts(object));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(PsiType.VOID));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(array));

    pattern = parseTypeExpression("void");
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(object));
    assertEquals(PointcutMatchDegree.TRUE, pattern.accepts(PsiType.VOID));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(array));

    pattern = parseTypeExpression("Object");
    assertEquals(PointcutMatchDegree.TRUE, pattern.accepts(object));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(PsiType.VOID));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(array));

    pattern = parseTypeExpression("int[]");
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(object));
    assertEquals(PointcutMatchDegree.TRUE, pattern.accepts(new PsiArrayType(PsiType.INT)));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(PsiType.INT));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(array));

    pattern = parseTypeExpression(CommonClassNames.JAVA_LANG_OBJECT + "[]");
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(object));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(PsiType.VOID));
    assertEquals(PointcutMatchDegree.TRUE, pattern.accepts(array));

    pattern = parseTypeExpression("java..* && *.lang.*");
    assertEquals(PointcutMatchDegree.TRUE, pattern.accepts(object));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(PsiType.VOID));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(array));
  }

  private JavaPsiFacade getJavaFacade() {
    return JavaPsiFacade.getInstance(getProject());
  }

  public void testArrays() throws Throwable {
    PsiType string = getJavaFacade().getElementFactory().createTypeFromText("String", null);
    PsiType stringArray = getJavaFacade().getElementFactory().createTypeFromText("String[]", null);
    PsiType stringVarargs = getJavaFacade().getElementFactory().createTypeFromText("String...", null);

    AopReferenceHolder pattern = parseTypeExpression("String...");
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(PsiType.VOID));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(string));
    assertEquals(PointcutMatchDegree.TRUE, pattern.accepts(stringVarargs));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(stringArray));

    pattern = parseTypeExpression("String[]");
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(PsiType.VOID));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(string));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(stringVarargs));
    assertEquals(PointcutMatchDegree.TRUE, pattern.accepts(stringArray));
  }

  public void testArgsArray() throws Throwable {
    assertEquals(PointcutMatchDegree.TRUE, parsePointcutExpression("args(Object[])").acceptsSubject(new PointcutContext(), parseMethod("void a(String[] ss) {}")));
  }

  private PsiMethod parseMethod(String s) {
    return parseClass("class A {" + s + "}").getMethods()[0];
  }

  private PsiClass parseClass(String text) {
    return ((PsiJavaFile)createLightFile(StdFileTypes.JAVA, text)).getClasses()[0];
  }

  public void testSubtypes() throws Throwable {
    PsiClass xObjectClass = myFixture.addClass("public class XObject { void foo() {}; }");
    PsiClass stringClass = myFixture.addClass("public class XString extends XObject { void foo(); }");
    PsiClass nonObjectClass = myFixture.addClass("public class NonObject { void foo(); }");

    PsiType object = getJavaFacade().getElementFactory().createTypeFromText("XObject", null);
    PsiType string = getJavaFacade().getElementFactory().createTypeFromText("XString", null);
    PsiType nonObject = getJavaFacade().getElementFactory().createTypeFromText("NonObject", null);

    AopReferenceHolder pattern = parseTypeExpression("XObject+");
    assertEquals(PointcutMatchDegree.TRUE, pattern.accepts(object));
    assertEquals(PointcutMatchDegree.TRUE, pattern.accepts(string));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(nonObject));
    assertEquals(PointcutMatchDegree.FALSE, pattern.accepts(PsiType.VOID));

    PsiPointcutExpression expression = parsePointcutExpression("execution (void XObject+.foo())");
    assertEquals(PointcutMatchDegree.TRUE, expression.acceptsSubject(new PointcutContext(), xObjectClass.getMethods()[0]));
    assertEquals(PointcutMatchDegree.TRUE, expression.acceptsSubject(new PointcutContext(), stringClass.getMethods()[0]));
    assertEquals(PointcutMatchDegree.FALSE, expression.acceptsSubject(new PointcutContext(), nonObjectClass.getMethods()[0]));
  }

  private AopReferenceHolder parseTypeExpression(String type) {
    AopPointcutExpressionFile file =
      (AopPointcutExpressionFile)createLightFile(AopPointcutExpressionFileType.INSTANCE, "execution(* *(" + type + "))");
    AopReferenceHolder type1 =
      (AopReferenceHolder)((PsiExecutionExpression)file.getPointcutExpression()).getParameterList().getParameters()[0];
    assertEquals(type, type1.getText());
    LiteFixture.setContext(file, parseClass( "class Foo {}"));
    file.setAopModel(new LocalAopModel(new AopAdvisedElementsSearcher(getPsiManager()) {
      public boolean process(Processor<PsiClass> processor) {
        throw new UnsupportedOperationException("Method doProcess is not yet implemented");
      }
    }));
    return type1;
  }

  private PsiFile createLightFile(FileType fileType, String s) {
    return PsiFileFactory.getInstance(getProject()).createFileFromText("a.b", fileType, s);
  }

  public void testThrows() throws Throwable {
    assertThrows("*", 8, 9, 10);
    assertThrows("java.lang.*", 8, 9, 10);
    assertThrows("!java.lang.*");
    assertThrows("!java.util.*", 8);
    assertThrows("!(java.util.*)", 8);
    assertThrows("(!java.util.*)", 8, 9, 10);
    assertThrows("(!java.lang.*)", 9, 10);
    assertThrows("( not java.lang.*)", 9, 10);
    assertThrows("java.util.*", 9, 10);
    assertThrows("java.util.*, java.lang.*", 9, 10);
    assertThrows("java.util.*, *", 9, 10);
  }

  public void testExecution() throws Throwable {
    assertExecution("public void java.util..foo()");
    assertExecution("public void java.util.YObject.foo()");
    assertExecution("public void java..foo()", 0);
    assertExecution("public void java.lang..*.foo()", 0);
    assertExecution("public void java.lang..foo()", 0);
    assertExecution("public void java.lang.*.foo()", 0);
    assertExecution("public void java.lang.YObject.foo()", 0);
    assertExecution("public void java.lang.YObject.foo(..)", 0, 1, 3);
    assertExecution("private void java.lang.YObject.foo(..)", 2);
    assertExecution("void java.lang.YObject.foo(..)", 0, 1, 2, 3);
    assertExecution("!static * java.lang.YObject.foo(..)", 0, 1, 2, 4);
    assertExecution("!static * java.lang.YObject.f*(..)", 0, 1, 2, 4, 5);
    assertExecution("not static * java.lang.YObject.*bar(..)", 5, 6, 7, 8, 9, 10);
    assertExecution("* java.lang.YObject.*(..)", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
    assertExecution("* java.lang.YObject.*()", 0, 11);
    assertExecution("* java.lang.YObject.*(..,String)", 3, 4, 5, 6, 7, 8, 9, 10);
    assertExecution("* java.lang.YObject.*(..,int)", 1, 2);
    assertExecution("* java.lang.YObject.*(..,int,..)", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    assertExecution("* java.lang.YObject.*(..,String,..)", 3, 4, 5, 6, 7, 8, 9, 10);
    assertExecution("* java.lang.YObject.*(..,int,String,..)", 3, 4, 5, 6);
    assertExecution("* java.lang.YObject.*(..,int,*)", 3, 4, 5, 6);
    assertExecution("* java.lang.YObject.*(..,int,*,..)", 3, 4, 5, 6, 7, 8, 9, 10);
    assertExecution("* java.lang.YObject.*(*, String)", 3, 4, 5, 6);
    assertExecution("* java.lang.YObject.*(*, int)");
    assertExecution("AbstractString java.lang.YObject.*(..)");
    assertExecution("int java.lang.YObject.*(..,long,..)", 7, 8, 9);
    assertExecution("int java.lang.YObject.*(..,long,..) throws *", 8, 9);
    assertExecution("int java.lang.YObject.*(..,long,..) throws java.util.*", 9);
    assertExecution("int java.lang.YObject.*(..,long,..) throws java.lang.*", 8, 9);
    assertExecution("int java.lang.YObject.*(..,long,..) throws java.lang.*, java.util.*", 9);
    assertExecution("int java.lang.YObject.*(..,long,..) throws java.util.*, *", 9);
    assertExecution("int[] java.lang.YObject.*(..,long,..) throws java.util.*, *", 10);
  }

  public void testAnnotations() throws Throwable {
    assertExecution("@annos.FooAnno * *(..)", 4, 5);
    assertExecution("@annos.FooAnno !@annos.BarAnno * *(..)", 4);
    assertExecution("!@annos.FooAnno @annos.BarAnno * *(..)", 6);
    assertExecution("@(annos.FooAnno || annos.BarAnno) * *(..)", 4, 5, 6);
    assertExecution("@(@annos.FooAnno *) * *(..)", 5, 6);

    assertExecution("* java.lang.YObject.*(..,@annos.FooAnno *)", 3, 4, 5, 6, 7, 8, 9, 10);
    assertExecution("(@(annos.FooAnno || *) *) java.lang.YObject.*(..)", 4, 5, 6);
  }

  public void testAnnotationsInheritance() throws Throwable {
    myFixture.addClass("public @interface Anno {}");
    myFixture.addClass("@" + CommonClassNames.JAVA_LANG_ANNOTATION_INHERITED + " public @interface InheritedAnno {}");

    myFixture.addClass("@InheritedAnno public class SuperClass {}");
    myFixture.addClass("@Anno public class Intf {}");
    myFixture.addClass("public class Impl extends SuperClass implements Intf {}");
    myFixture.addClass("public class Impl1 extends Impl1 {}");
    myFixture.addClass("@InheritedAnno public interface AnnotatedIntf {}");
    
    PsiClass aClass = myFixture.addClass("class Foo { " +
               "SuperClass foo0(SuperClass t) {} " +
               "Intf foo1(Intf t) {} " +
               "Impl foo2(Impl t) {} " +
               "Impl1 foo3(Impl1 t) {} " +
               "AnnotatedIntf foo4(AnnotatedIntf t) {} " +
               "}");

    assertSuitableMethods(parsePointcutExpression("execution(* *(@Anno *))"), aClass, 1);
    assertSuitableMethods(parsePointcutExpression("execution(* *(@InheritedAnno *))"), aClass, 0, 2, 4);
    assertSuitableMethods(parsePointcutExpression("execution((@Anno *) *(*))"), aClass, 1);
    assertSuitableMethods(parsePointcutExpression("execution((@InheritedAnno *) *(*))"), aClass, 0, 2, 4);

    assertSuitableMethods(parsePointcutExpression("@args(InheritedAnno)"), aClass, 0, 2, 4);
    assertSuitableMethods(parsePointcutExpression("@args(Anno)"), aClass, 1);
  }

  public void testReference() throws Throwable {
    assertSuitableMethods("pointcut()", 1, 2);
    assertSuitableMethods("!pointcut()", 0, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
  }

  public void testArgs() throws Throwable {
    assertArgs("", 0, 11);
    assertArgs("..", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
    assertArgs("..,String", 3, 4, 5, 6, 7, 8, 9, 10);
    assertArgs("..,AbstractString", 3, 4, 5, 6, 7, 8, 9, 10);
    assertArgs("..,int", 1, 2);
    assertArgs("..,java.lang.Integer", 1, 2);
    assertArgs("..,Integer", 1, 2);
    assertArgs("..,int,..", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    assertArgs("..,String,..", 3, 4, 5, 6, 7, 8, 9, 10);
    assertArgs("..,int,String,..", 3, 4, 5, 6);
    assertArgs("..,int,..,String,..", 3, 4, 5, 6, 7, 8, 9, 10);
    assertArgs("..,int,*", 3, 4, 5, 6);
    assertArgs("int,..,String", 3, 4, 5, 6, 7, 8, 9, 10);
    assertArgs("..,int,*,..", 3, 4, 5, 6, 7, 8, 9, 10);
    assertArgs("*, String", 3, 4, 5, 6);
    assertArgs("*, int");
    assertArgs("..,long,..", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    assertArgs("..,java.lang.Long,..", 7, 8, 9, 10);
  }

  public void testArgsWithParamaters() throws Throwable {
    PsiPointcutExpression expression = parsePointcutExpression("args(bar)");
    LiteFixture.setContext(expression.getContainingFile(), parseClass( "class Foo {}"));
    expression.getContainingFile().setAopModel(new LocalAopModel(null, parseClass(
      "class Foo { void foo(java.lang.Number bar) {} }").getMethods()[0],
                                                                 new AllAdvisedElementsSearcher(getPsiManager())));
    assertSuitableMethods(expression, 1, 2);

    expression = parsePointcutExpression("args(..,bar)");
    LiteFixture.setContext(expression.getContainingFile(), parseClass( "class Foo {}"));
    expression.getContainingFile().setAopModel(new LocalAopModel(null, parseClass(
       "class Foo { void foo(java.lang.AbstractString bar) {} }").getMethods()[0],
                                                                 new AllAdvisedElementsSearcher(getPsiManager())));
    assertSuitableMethods(expression, 3, 4, 5, 6, 7, 8, 9, 10);
  }

  public void testComposites() throws Throwable {
    assertSuitableMethods("args(..) && args(..,int)", 1, 2);
    assertSuitableMethods("args(*, String) || args(..,int)", 1, 2, 3, 4, 5, 6);
    assertSuitableMethods("(args(*, String)) or !(args(..,String))", 0, 1, 2, 3, 4, 5, 6, 11, 12);
  }

  private void assertThrows(@NonNls String text, int... acceptedMethods) {
    assertSuitableMethods("execution(* *(..) throws " + text + ")", acceptedMethods);
  }

  private void assertExecution(@NonNls String text, int... acceptedMethods) {
    assertSuitableMethods("execution(" + text + ")", acceptedMethods);
  }

  private void assertArgs(@NonNls String text, int... acceptedMethods) {
    assertSuitableMethods("args(" + text + ")", acceptedMethods);
  }

  private void assertSuitableMethods(@NonNls String text, int... acceptedMethods) {
    assertSuitableMethods(parsePointcutExpression(text), acceptedMethods);
  }

  public void testAtAnnotation() throws Throwable {
    assertSuitableMethods("@annotation(" + CommonClassNames.JAVA_LANG_DEPRECATED + ")", 10);
  }

  public void testAtArgs() throws Throwable {
    myFixture.addClass("public @interface Anno {}");
    myFixture.addClass("public @Anno class Foo {}");
    myFixture.addClass("public class Bar extends Foo {}");
    myFixture.addClass("public interface Goo {}");
    myFixture.addClass("public @Anno interface Zoo {}");
    myFixture.addClass("public final class Boo {}");

    PsiPointcutExpression expression = parsePointcutExpression("@args(Anno)");
    assertEquals(PointcutMatchDegree.FALSE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method() {}")));
    assertEquals(PointcutMatchDegree.FALSE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method(int b) {}")));
    assertEquals(PointcutMatchDegree.FALSE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method(Bar b, int a) {}")));
    assertEquals(PointcutMatchDegree.MAYBE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method(Bar b) {}")));
    assertEquals(PointcutMatchDegree.TRUE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method(Foo b) {}")));
    assertEquals(PointcutMatchDegree.MAYBE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method(Goo b) {}")));
    assertEquals(PointcutMatchDegree.FALSE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method(Boo b) {}")));
    assertEquals(PointcutMatchDegree.TRUE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method(Zoo b) {}")));
  }

  public void testAtArgsInherited() throws Throwable {
    myFixture.addClass("public @" + CommonClassNames.JAVA_LANG_ANNOTATION_INHERITED + " @interface Anno {}");
    myFixture.addClass("public @Anno class Foo {}");
    myFixture.addClass("public class Bar extends Foo {}");
    myFixture.addClass("public interface Goo {}");
    myFixture.addClass("public @Anno interface Zoo {}");
    myFixture.addClass("public final class Boo {}");

    PsiPointcutExpression expression = parsePointcutExpression("@args(Anno)");
    assertEquals(PointcutMatchDegree.FALSE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method() {}")));
    assertEquals(PointcutMatchDegree.FALSE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method(int b) {}")));
    assertEquals(PointcutMatchDegree.FALSE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method(Bar b, int a) {}")));
    assertEquals(PointcutMatchDegree.TRUE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method(Bar b) {}")));
    assertEquals(PointcutMatchDegree.TRUE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method(Foo b) {}")));
    assertEquals(PointcutMatchDegree.MAYBE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method(Goo b) {}")));
    assertEquals(PointcutMatchDegree.FALSE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method(Boo b) {}")));
    assertEquals(PointcutMatchDegree.TRUE, expression.acceptsSubject(new PointcutContext(), parseMethod("void method(Zoo b) {}")));
  }

  public void testAtTargetThisWithin() throws Throwable {
    myFixture.addClass("public @interface Anno {}");
    myFixture.addClass("public @Anno class Bar {}");
    assertAtTargetThisWithin("class Foo { void foo() {}; }", PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE);
    assertAtTargetThisWithin("@Anno class Foo { void foo() {}; }", PointcutMatchDegree.TRUE, PointcutMatchDegree.FALSE);
    assertAtTargetThisWithin("class Foo extends Bar { void foo() {}; }", PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE);
  }

  public void testAtTargetThisWithinInherited() throws Throwable {
    myFixture.addClass("@" + CommonClassNames.JAVA_LANG_ANNOTATION_INHERITED + " public @interface Anno {}");
    myFixture.addClass("public @Anno class Bar {}");

    assertAtTargetThisWithin("class Foo { void foo() {}; }", PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE);
    assertAtTargetThisWithin("@Anno class Foo { void foo() {}; }", PointcutMatchDegree.TRUE, PointcutMatchDegree.TRUE);
    assertAtTargetThisWithin("class Foo extends Bar { void foo() {}; }", PointcutMatchDegree.TRUE, PointcutMatchDegree.TRUE);

    myFixture.addClass("@Anno interface Goo {}");
    assertAtTargetThisWithin("class Foo implements Goo { void foo() {}; }", PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE);
  }

  private void assertAtTargetThisWithin(@NonNls String classText, PointcutMatchDegree targetWithin, PointcutMatchDegree _this) {
    assertEquals(targetWithin, parsePointcutExpression("@target(Anno)").acceptsSubject(new PointcutContext(), parseClass(
       classText).getMethods()[0]));
    assertEquals(targetWithin, parsePointcutExpression("@within(Anno)").acceptsSubject(new PointcutContext(), parseClass(
       classText).getMethods()[0]));
    assertEquals(_this, parsePointcutExpression("@this(Anno)").acceptsSubject(new PointcutContext(), parseClass(
       classText).getMethods()[0]));
  }

  public void testTargetThisWithin() throws Throwable {
    assertTargetThisWithin("*", parseClass("public class A { public void foo() {} }"), PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE, PointcutMatchDegree.TRUE);
    assertTargetThisWithin("java.*", parseClass("package java; public class A {public void foo() {}}"), PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE, PointcutMatchDegree.TRUE);
    assertTargetThisWithin("util.*", parseClass("package java; public class A {public void foo() {}}"), PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE);

    assertTargetThisWithin("java.Ad", myFixture.addClass("package java; public class Ad {public void foo() {}}"), PointcutMatchDegree.TRUE, PointcutMatchDegree.TRUE, PointcutMatchDegree.TRUE);
    assertTargetThisWithin("java.Ae+", myFixture.addClass("package java; public class Ae {public void foo() {}}"), PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE, PointcutMatchDegree.TRUE);
    assertTargetThisWithin("java.Af", myFixture.addClass("package java; public class Af { public class B {public void foo() {}} }").getInnerClasses()[0], PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE, PointcutMatchDegree.TRUE);

    myFixture.addClass("package java; public class Foo {}");
    assertTargetThisWithin("java.Foo+", parseClass("package java; public class A extends Foo { public class B {public void foo() {}} }").getInnerClasses()[0], PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE, PointcutMatchDegree.TRUE);

    myFixture.addClass("package util; public class Foo {}");
    assertTargetThisWithin("util.Foo", parseClass("package java; public class A {public void foo() {}}"), PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE);

    myFixture.addClass("package util; public interface Bar {}");
    assertTargetThisWithin("util.Bar", parseClass("package java; public class A {public void foo() {}}"), PointcutMatchDegree.FALSE, PointcutMatchDegree.MAYBE, PointcutMatchDegree.FALSE);
    assertTargetThisWithin("util.Bar", parseClass("package java; public final class A {public void foo() {}}"), PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE, PointcutMatchDegree.FALSE);

    PsiClass clazz = myFixture.addClass("package java; public class A {public void foo() {}}");
    PsiPointcutExpression expression = parsePointcutExpression("target(x)");
    expression.getContainingFile().setAopModel(new LocalAopModel(null, parseMethod("public void foo(java.A x) {}"), new AopAdvisedElementsSearcher(getPsiManager()) {
      public boolean process(Processor<PsiClass> processor) {
        throw new UnsupportedOperationException("Method doProcess is not yet implemented in " + getClass().getName());
      }
    }));
    assertEquals(PointcutMatchDegree.TRUE, expression.acceptsSubject(new PointcutContext(expression), clazz.getMethods()[0]));
  }

  private void assertTargetThisWithin(String pattern, PsiClass psiClass, PointcutMatchDegree target, PointcutMatchDegree _this, PointcutMatchDegree within) {
    assertEquals(target, parsePointcutExpression("target(" + pattern + ")").acceptsSubject(new PointcutContext(), psiClass.getMethods()[0]));
    assertEquals(_this, parsePointcutExpression("this(" + pattern + ")").acceptsSubject(new PointcutContext(), psiClass.getMethods()[0]));
    assertEquals(within, parsePointcutExpression("within(" + pattern + ")").acceptsSubject(new PointcutContext(), psiClass.getMethods()[0]));
  }

  private void assertSuitableMethods(PsiPointcutExpression expression, int... acceptedMethods) {
    assertSuitableMethods(expression, getJavaFacade().findClass("java.lang.YObject"), acceptedMethods);
  }

  private static void assertSuitableMethods(PsiPointcutExpression expression, PsiClass aClass, int... acceptedMethods) {
    PsiMethod[] methods = aClass.getMethods();
    List<PsiMethod> actual = new ArrayList<PsiMethod>();
    for (PsiMethod psiMethod : methods) {
      if (expression.acceptsSubject(new PointcutContext(expression), psiMethod) == PointcutMatchDegree.TRUE) {
        actual.add(psiMethod);
      }
    }
    PsiMethod[] expected = new PsiMethod[acceptedMethods.length];
    for (int i = 0; i < acceptedMethods.length; i++) {
      expected[i] = methods[acceptedMethods[i]];
    }
    assertOrderedEquals(actual, expected);
  }

  private PsiPointcutExpression parsePointcutExpression(@NonNls String text) {
    AopPointcutExpressionFile file = (AopPointcutExpressionFile)createLightFile(AopPointcutExpressionFileType.INSTANCE, text);
    PsiPointcutExpression expression = file.getPointcutExpression();
    PsiClass objectClass = getJavaFacade().findClass("java.lang.YObject");
    LiteFixture.setContext(file, objectClass);
    file.setAopModel(myAopModel);
    return expression;
  }

}
