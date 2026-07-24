/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.AopLiteFixture;
import com.intellij.aop.AopPointcut;
import com.intellij.aop.LocalAopModel;
import com.intellij.aop.jam.AopConstants;
import com.intellij.aop.lexer.AopLexer;
import com.intellij.mock.MockPsiClassType;
import com.intellij.mock.MockXmlTag;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.ResolveResult;
import consulo.virtualFileSystem.fileType.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import consulo.document.util.TextRange;
import consulo.language.psi.meta.PsiMetaData;
import com.intellij.testFramework.LiteFixture;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import consulo.language.util.IncorrectOperationException;
import consulo.application.util.function.Processor;
import consulo.util.collection.ContainerUtil;
import consulo.language.editor.completion.lookup.LookupElement;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author peter
 */

@SuppressWarnings({"ConstantConditions", "deprecation"})
public class AopReferencesTest extends JavaCodeInsightFixtureTestCase {

  protected void setUp() throws Exception {
    super.setUp();

    myFixture.addClass("package nonjava.nonlang; " +
                                   "public class NonObject {" +
                                   "public void toHell() {}" +
                                   "public void toHes() {}" +
                                   "public void foo() {}" +
                                   "}");
    myFixture.addClass("package nonjava.nonlang; " +
                                   "public class NonString {" +
                                   "}");

    myFixture.addClass("package abc;" +
                                   "public interface Intf {" +
                                   "void intfMethod();" +
                                   "}");

    myFixture.addClass("package foobar; " +
                                   "public class String239 implements abc.Intf {" +
                                   "public void intfMethod() {}" +
                                   "}");

    AopLiteFixture.addAopAnnotations(myFixture);

    myFixture.addClass("import foobar.*; " +
                                   "public class Object239 extends nonjava.nonlang.NonObject { " +
                                   "public static class Foo {" +
                                   " public void somePointcut() {}" +
                                   "}" +
                                   "@" + AopConstants.POINTCUT_ANNO + " " +
                                   "public void somePointcut() {} " +
                                   "@" + AopConstants.POINTCUT_ANNO + " " +
                                   "public void ObjsomePointcut() {} " +
                                   "}");


    myFixture.addClass("package abc; " +
                                   "public class Def { " +
                                   "@" + AopConstants.POINTCUT_ANNO + " " +
                                   "private void priv() {} " +
                                   "@" + AopConstants.POINTCUT_ANNO + " " +
                                   "public void publ() {} " +
                                   "}");
    myFixture.addClass("package abc; class ABC {}");
  }

  public void testJavaClassReferences() throws Throwable {
    AopReferenceExpression expression = parseMethodReference("nonjava.nonlang.*").getReferenceExpression();
    AopReferenceExpression javaLang = expression.getQualifier();
    AopReferenceExpression java = javaLang.getQualifier();
    assertEquals("nonjava", java.getCanonicalText());
    assertEquals(TextRange.from(0, 7), java.getRangeInElement());
    assertEquals(JavaPsiFacade.getInstance(getProject()).findPackage("nonjava"), java.resolve());

    assertEquals("nonjava.nonlang", javaLang.getCanonicalText());
    assertEquals(TextRange.from(8, 7), javaLang.getRangeInElement());
    assertEquals(JavaPsiFacade.getInstance(getProject()).findPackage("nonjava.nonlang"), javaLang.resolve());

    assertEquals("nonjava.nonlang.*", expression.getCanonicalText());
    assertEquals(TextRange.from(16, 1), expression.getRangeInElement());
    assertNull(expression.resolve());
    assertUnorderedCollection(expression.multiResolve(false), new Consumer<ResolveResult>() {
      public void consume(ResolveResult resolveResult) {
        assertEquals(getJavaFacade().findClass("nonjava.nonlang.NonObject"), resolveResult.getElement());
      }
    }, new Consumer<ResolveResult>() {
      public void consume(ResolveResult resolveResult) {
        assertEquals(getJavaFacade().findClass("nonjava.nonlang.NonString"), resolveResult.getElement());
      }
    });
  }

  private JavaPsiFacade getJavaFacade() {
    return JavaPsiFacade.getInstance(getProject());
  }

  public void testJavaReferencesInXml() throws Throwable {
    AopMemberReferenceExpression expression = parseMethodReference("java.lang.Object");
    LiteFixture.setContext(expression.getContainingFile(), createXmlFile("<xml/>"));
    expression.getContainingFile().setAopModel(new LocalAopModel(new AopAdvisedElementsSearcher(getPsiManager()) {
      public boolean process(Processor<PsiClass> processor) {
        throw new UnsupportedOperationException("Method doProcess is not yet implemented");
      }
    }));
    assertEquals(getJavaFacade().findClass(CommonClassNames.JAVA_LANG_OBJECT), expression.getReferenceExpression().resolve());

    expression = parseMethodReference("Object");
    LiteFixture.setContext(expression.getContainingFile(), createXmlFile("<xml/>"));
    expression.getContainingFile().setAopModel(new LocalAopModel(new AopAdvisedElementsSearcher(getPsiManager()) {
      public boolean process(Processor<PsiClass> processor) {
        throw new UnsupportedOperationException("Method doProcess is not yet implemented");
      }
    }));
    assertEquals(getJavaFacade().findClass(CommonClassNames.JAVA_LANG_OBJECT), expression.getReferenceExpression().resolve());
  }

  private PsiElement createXmlFile(String s) {
    return createLightFile(StdFileTypes.XML, s);
  }

  private AopMemberReferenceExpression parseMethodReference(String refText) {
    AopMemberReferenceExpression reference =
      assertInstanceOf(parse("execution(* " + refText + "())"), PsiExecutionExpression.class).getMethodReference();
    LiteFixture.setContext(reference.getContainingFile(), getJavaFacade().findClass("Object239"));
    reference.getContainingFile().setAopModel(new LocalAopModel(new AllAdvisedElementsSearcher(getPsiManager())));
    return reference;
  }

  private AopReferenceExpression parsePointcutReference(String refText) {
    AopReferenceExpression reference =
      assertInstanceOf(parse(refText + "()"), PsiPointcutReferenceExpression.class).getReferenceExpression();

    AopPointcutExpressionFile psiFile = reference.getContainingFile();
    LiteFixture.setContext(psiFile, getJavaFacade().findClass("Object239"));
    psiFile.setAopModel(new MockAopModel(new AopAdvisedElementsSearcher(getPsiManager()) {
      public boolean process(Processor<PsiClass> processor) {
        throw new UnsupportedOperationException("Method doProcess is not yet implemented");
      }
    }) {
      final List<AopPointcut> pointcuts = Arrays.asList(createMockPointcut("xa.b.c.d"), createMockPointcut("fubar"));
      public List<? extends AopPointcut> getPointcuts() {
        return pointcuts;
      }
    });
    return reference;
  }

  public void testPointcutReferences() throws Throwable {
    assertNull(parsePointcutReference("nonExistentPointcut").resolve());
    AopReferenceExpression referenceExpression = parsePointcutReference("foo");
    assertResolves(referenceExpression);
    assertResolves(parsePointcutReference("Foo"));
    assertNull(parsePointcutReference("String239").resolve());
    assertResolves(parsePointcutReference("foobar.String239"));
    assertNull(parsePointcutReference("fubar").resolve());
    assertInstanceOf(assertResolves(parsePointcutReference("Object239.somePointcut")), PsiMethod.class);
    assertEquals(parsePointcutReference("somePointcut").resolve(), parsePointcutReference("Object239.somePointcut").resolve());

    assertInstanceOf(assertResolves(parsePointcutReference("abc.Def.publ")), PsiMethod.class);
    assertNull(parsePointcutReference("abc.Def.priv").resolve());

    assertNull(parsePointcutReference("xa.b.c.d").resolve());
  }

  public void testHandleElementRename() throws Throwable {
    AopMemberReferenceExpression methodRef = parseMethodReference("a.b.c.d");
    PsiElement psiElement = methodRef.getReferenceExpression().handleElementRename("e");
    assertEquals("a.b.c.e", assertInstanceOf(psiElement, AopReferenceExpression.class).getText());
    assertEquals("a.b.c.e", methodRef.getText());

    methodRef = parseMethodReference("d");
    psiElement = methodRef.getReferenceExpression().handleElementRename("e");
    assertEquals("e", assertInstanceOf(psiElement, AopReferenceExpression.class).getText());
    assertEquals("e", methodRef.getText());

    methodRef = parseMethodReference("d*e");
    psiElement = methodRef.getReferenceExpression().handleElementRename("e");
    assertEquals("e", assertInstanceOf(psiElement, AopReferenceExpression.class).getText());
    assertEquals("e", methodRef.getText());
  }

  public void testParameter() throws Throwable {
    AopParameterList parameterList = ((PsiArgsExpression)parse("args(argName, nonArgName)")).getParameterList();
    PsiElement[] parameters = parameterList.getParameters();
    PsiMethod method =
      parseClass("class A {" + "@" + AopConstants.POINTCUT_ANNO + "(\"\") void foo(" + AopConstants.PROCEEDING_JOIN_POINT + " nonArgName, int argName) }").getMethods()[0];
    PsiFile file = parameterList.getContainingFile();
    LiteFixture.setContext(file, method.getModifierList().getAnnotations()[0].findAttributeValue("value"));
    ((AopPointcutExpressionFile)file).setAopModel(new LocalAopModel(null, method, new AopAdvisedElementsSearcher(getPsiManager()) {
      public boolean process(Processor<PsiClass> processor) {
        throw new UnsupportedOperationException("Method doProcess is not yet implemented in " + getClass().getName());
      }
    }));
    assertEquals(method.getParameterList().getParameters()[1], ((AopReferenceExpression)((AopReferenceHolder)parameters[0]).getTypeExpression()).resolve());
  }

  private PsiClass parseClass(String text) {
    return ((PsiJavaFile) PsiFileFactory.getInstance(getProject()).createFileFromText("a.java", text)).getClasses()[0];
  }

  public void testBindToElement() throws Throwable {
    assertBinds(getJavaFacade().findClass(CommonClassNames.JAVA_LANG_OBJECT).findMethodsByName("toString", true)[0], "toString");
    assertBinds(getJavaFacade().findClass("nonjava.nonlang.NonObject").findMethodsByName("toHell", true)[0], "nonjava.nonlang.NonObject.toHell");
    assertBinds(getJavaFacade().findClass("abc.Def").findMethodsByName("publ", false)[0], "publ");
    assertBinds(getJavaFacade().findClass("abc.Def").findMethodsByName("priv", false)[0], "priv");
    assertBinds(getJavaFacade().findClass("Object239").findMethodsByName("somePointcut", false)[0], "Object239.somePointcut");
    assertBinds(getJavaFacade().findClass("Object239").getInnerClasses()[0].findMethodsByName("somePointcut", false)[0], "Object239.Foo.somePointcut");
    assertBinds(getJavaFacade().findClass("foobar.String239"), "foobar.String239");
    assertBinds(getJavaFacade().findClass("abc.Def"), "abc.Def");
    assertBinds("a.b.c.d", getJavaFacade().findClass("abc.ABC"), "abc.ABC");
    assertBinds("ABC", getJavaFacade().findClass("abc.ABC"), "abc.ABC");
    assertBinds(JavaPsiFacade.getInstance(getProject()).findPackage("abc"), "abc");
    assertBinds(new MockXmlTag(){
      @Nullable
      public PsiMetaData getMetaData() {
        return new PsiMetaData() {

          public PsiElement getDeclaration() {
            throw new UnsupportedOperationException("Method getDeclaration is not yet implemented in " + getClass().getName());
          }

          @NonNls
          public String getName(PsiElement context) {
            return "fubar.xxx";
          }

          @NonNls
          public String getName() {
            throw new UnsupportedOperationException("Method getName is not yet implemented in " + getClass().getName());
          }

          public void init(PsiElement element) {
            throw new UnsupportedOperationException("Method init is not yet implemented in " + getClass().getName());
          }

          public Object[] getDependences() {
            throw new UnsupportedOperationException("Method getDependences is not yet implemented in " + getClass().getName());
          }
        };
      }
    }, "fubar.xxx");
  }

  private void assertBinds(PsiElement bindTo, String expectedText) throws IncorrectOperationException {
    assertBinds("a.b.c.d", bindTo, expectedText);
  }

  private void assertBinds(String refText, PsiElement bindTo, String expectedText) throws IncorrectOperationException
  {
    AopMemberReferenceExpression reference =
      assertInstanceOf(parse("execution(* " + refText + "())"), PsiExecutionExpression.class).getMethodReference();
    LiteFixture.setContext(reference.getContainingFile(), getJavaFacade().findClass("abc.Def"));
    reference.getContainingFile().setAopModel(new LocalAopModel(new AllAdvisedElementsSearcher(getPsiManager())));
    PsiElement psiElement = reference.getReferenceExpression().bindToElement(bindTo);
    assertEquals(expectedText, assertInstanceOf(psiElement, AopReferenceExpression.class).getText());
    assertEquals(expectedText, reference.getText());
  }

  private static PsiElement assertResolves(AopReferenceExpression referenceExpression) {
    PsiElement element = referenceExpression.resolve();
    assertNotNull(element);
    assertTrue(referenceExpression.isReferenceTo(element));
    return element;
  }

  public void testNoMethodNamePatternReference() throws Throwable {
    AopMemberReferenceExpression expression = parseMethodReference("java.lang..toString");
    assertNull(expression.getReference());
    assertEmpty(expression.getReferences());

    expression = parseMethodReference("java.*.lang.toString");
    assertNull(expression.getReference());
    assertEmpty(expression.getReferences());
  }

  public void testInnerClassResolve() throws Throwable {
    assertResolves(parseMethodReference("Foo.toString").getReferenceExpression().getQualifier());
    assertResolves(parseMethodReference("Object239.toString").getReferenceExpression().getQualifier());
    assertResolves(parseMethodReference("Object239.Foo.toString").getReferenceExpression().getQualifier());
  }

  public void testMethodResolve() throws Throwable {
    String prefix = "nonjava.nonlang.NonObject.";
    String methodName = "to*";
    AopMemberReferenceExpression expression = parseMethodReference(prefix + methodName);
    PsiClass psiClass = assertInstanceOf(expression.getReferenceExpression().getQualifier().resolve(), PsiClass.class);

    final AopReferenceExpression namePattern = expression.getReferenceExpression();
    assertEquals(prefix + methodName, namePattern.getCanonicalText());
    assertEquals(TextRange.from(prefix.length(), methodName.length()), namePattern.getRangeInElement());
    assertEquals(namePattern, namePattern.getElement());

    List<PsiMethod> methods = Arrays.asList(psiClass.findMethodsByName("toHell", true)[0], psiClass.findMethodsByName("toHes", true)[0]);

    assertUnorderedCollection(namePattern.multiResolve(true), ContainerUtil.map2Array(methods, Consumer.class, new Function<PsiMethod, Consumer>() {
      public Consumer fun(final PsiMethod method) {
        return new Consumer<ResolveResult>() {
          public void consume(ResolveResult resolveResult) {
            PsiElement element = resolveResult.getElement();
            assertNotNull(element);
            assertEquals(element, method);
            assertTrue(namePattern.isReferenceTo(element));
          }
        };
      }
    }));
    assertSameElements(psiClass.getAllMethods(), ContainerUtil.map2Array(namePattern.getVariants(), new Function<LookupElement, Object>() {
      public Object fun(LookupElement item) {
        return item.getObject();
      }
    }));
  }

  public void testMethodConflicts() throws Throwable {
    AopMemberReferenceExpression expression = parseMethodReference("somePointcut");
    PsiClass innerClass = getJavaFacade().findClass("Object239").getInnerClasses()[0];
    LiteFixture.setContext(expression.getContainingFile(), innerClass);
    expression.getContainingFile().setAopModel(new LocalAopModel(new AopAdvisedElementsSearcher(getPsiManager()) {
      public boolean process(Processor<PsiClass> processor) {
        throw new UnsupportedOperationException("Method doProcess is not yet implemented");
      }
    }));
    PsiElement element = expression.getReferenceExpression().resolve();
    assertEquals(innerClass, assertInstanceOf(element, PsiMethod.class).getContainingClass());
  }

  public void testInheritedMethod() throws Throwable {
    AopMemberReferenceExpression expression = parseMethodReference("foobar.String239.intfMethod");
    PsiElement element = expression.getReferenceExpression().resolve();
    assertEquals(getJavaFacade().findClass("foobar.String239"), assertInstanceOf(element, PsiMethod.class).getContainingClass());
  }

  public void testPackageResolvesToMethod() throws Throwable {
    AopMemberReferenceExpression expression = parseMethodReference("foobar.String239.intfMethod");
    LiteFixture.setContext(expression.getContainingFile(), parseClass("class A { void foobar() {}}"));
    expression.getContainingFile().setAopModel(new LocalAopModel(new AllAdvisedElementsSearcher(getPsiManager())));
    PsiElement element = expression.getReferenceExpression().resolve();
    assertEquals(getJavaFacade().findClass("foobar.String239"), assertInstanceOf(element, PsiMethod.class).getContainingClass());
  }

  public void testTypeReference() throws Throwable {
    assertTypeReference("*", "java.lang.Object", true);
    assertTypeReference("*", "boolean", true);
    assertTypeReference("*", "void", true);
    assertTypeReference("*", "int", true);
    assertTypeReference("*", "long", true);
    assertTypeReference("*", "byte", true);
    assertTypeReference("*", "char", true);
    assertTypeReference("*", "float", true);
    assertTypeReference("*", "double", true);
    assertTypeReference("*", "String", true);
    assertTypeReference("com.*", "com.String", true);
    assertTypeReference("com.*", "com.intellij.String", false);
    assertTypeReference("com.*", "String", false);
    assertTypeReference("com.*", "int", false);
    assertTypeReference("com..*", "com.intellij.String", true);
    assertTypeReference("*.com..*", "com.intellij.String", false);
    assertTypeReference("*.com..*", "org.com.intellij.String", true);
    assertTypeReference("*.com..*", "org.org.com.intellij.String", false);
    assertTypeReference("com..openapi", "com.intellij.openapi", true);
    assertTypeReference("com..openapi.*", "com.intellij.openapi.String", true);
    assertTypeReference("com..openapi.*", "com.intellij.openapi.util.String", false);
    assertTypeReference("com..openapi..*", "com.intellij.openapi.util.String", true);
    assertTypeReference("com..openapi..*", "com.intellij.util.openapi.util.String", true);
    assertTypeReference("com..openapi..*", "com.openapi.String", true);
    assertTypeReference("com..*..*", "com.intellij.util.openapi.util.String", true);
    assertTypeReference("com..*..*", "com.String", false);
    assertTypeReference("*..*..*", "com.String", false);
    assertTypeReference("*..*..*", "com.intellij.String", true);
  }

  public void testPrimitiveTypeReferences() throws Throwable {
    for (String name : AopLexer.PRIMITIVE_TYPES.keySet()) {
      assertNoReferences(name);
      assertNoReferences(name + "[]");
      assertNoReferences(name + "[][]");
    }
  }

  private void assertNoReferences(String text) {
    AopReferenceHolder aopReferenceHolder = parseTypeReference(text);
    assertEmpty(aopReferenceHolder.getReferences());
    PsiElement child = aopReferenceHolder;
    while ((child = child.getFirstChild()) != null) {
      assertEmpty(child.getReferences());
    }
  }

  private void assertTypeReference(String refText, String candidate, boolean suits) {
    assertEquals(PointcutMatchDegree.valueOf(suits), parseTypeReference(refText).accepts(new MockPsiClassType(candidate)));
  }

  private AopReferenceHolder parseTypeReference(String refText) {
    AopPointcutExpressionFile file =
      (AopPointcutExpressionFile)createLightFile(AopPointcutExpressionFileType.INSTANCE, "args(" + refText + ")");
    LiteFixture.setContext(file, createXmlFile("<a/>"));
    file.setAopModel(new LocalAopModel(new AopAdvisedElementsSearcher(getPsiManager()) {
      public boolean process(Processor<PsiClass> processor) {
        throw new UnsupportedOperationException("Method doProcess is not yet implemented");
      }
    }));
    return (AopReferenceHolder)((PsiArgsExpression)file.getPointcutExpression()).getParameterList().getParameters()[0];
  }

  private PsiFile createLightFile(FileType fileType, String s) {
    return PsiFileFactory.getInstance(getProject()).createFileFromText("a.b", fileType, s);
  }

  private PsiPointcutExpression parse(@NonNls String code) {
    return ((AopPointcutExpressionFile)createLightFile(AopPointcutExpressionFileType.INSTANCE, code)).getPointcutExpression();
  }

}
