/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopLiteFixture;
import com.intellij.aop.AopPointcut;
import com.intellij.aop.LocalAopModel;
import com.intellij.aop.jam.AopConstants;
import com.intellij.aop.jam.AopPointcutImpl;
import consulo.language.editor.completion.CompletionType;
import consulo.application.Result;
import consulo.language.editor.WriteCommandAction;
import consulo.language.file.FileTypeManager;
import com.intellij.testFramework.fixtures.InjectedLanguageFixtureTestCase;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nonnull;
import org.junit.AfterClass;

import java.util.Arrays;
import java.util.List;

/**
 * @author peter
 */
public class AopCompletionTest extends InjectedLanguageFixtureTestCase {
  private LocalAopModel myAopModel;

  protected void setUp() throws Exception {
    super.setUp();

    AopLiteFixture.addAopAnnotations(myFixture);

    myFixture.addClass("package java; public class String { " +
                                            "public void substring() {} " +
                                            "public void foo(int a) {}" +
                                            "public int field; " +
                                            "}");
    myFixture.addClass("package xxx; public class JustClass {}");

    myFixture.addClass("public class AnotherAspect { " +
                                            "@" + AopConstants.POINTCUT_ANNO + " " + "private void privatePointcut() {} " +
                                            "}");

    final PsiClass contextClass = myFixture.addClass(
      "public class Object239 { " +
      "public class Foo {} " +
      "@" + AopConstants.POINTCUT_ANNO + " " + "public void somePointcut(" + AopConstants.JOIN_POINT + " nonArgName, int argName) {} " +
      "@" + AopConstants.POINTCUT_ANNO + " " + "public void ObjsomePointcut() {} " +
      "@" + AopConstants.POINTCUT_ANNO + " " + "private void privatePointcut() {} " +
      "}");

    myFixture.setFileContext(contextClass);

    final PsiMethod somePointcutMethod = contextClass.findMethodsByName("somePointcut", false)[0];
    final PsiMethod ObjsomePointcutMethod = contextClass.findMethodsByName("ObjsomePointcut", false)[0];

    final AopPointcutImpl somePointcut = new AopPointcutImpl() {
      @Nonnull
      @Override
      public PsiMethod getPsiElement() {
        return somePointcutMethod;
      }
    };
    final AopPointcutImpl ObjsomePointcut = new AopPointcutImpl() {
      @Nonnull
      @Override
      public PsiMethod getPsiElement() {
        return ObjsomePointcutMethod;
      }
    };

    myAopModel = new MockAopModel(somePointcutMethod, somePointcutMethod) {
      final List<AopPointcut> pointcuts = Arrays.asList(
        somePointcut, ObjsomePointcut,
        createMockPointcut("xa.b.c.d", null, JavaPsiFacade.getInstance(myFixture.getProject()).getElementFactory().createClassFromText("@A public void foo()", null).getMethods()[0].getModifierList().getAnnotations()[0]),
        createMockPointcut("fubar"));
      public List<? extends AopPointcut> getPointcuts() {
        return pointcuts;
      }
    };

    new WriteCommandAction(getProject()) {
      @Override
      protected void run(Result result) throws Throwable {
        final FileTypeManager manager = FileTypeManager.getInstance();
        if (manager.getFileTypeByExtension(AopPointcutExpressionFileType.INSTANCE.getDefaultExtension()) != AopPointcutExpressionFileType.INSTANCE) {
          manager
          .registerFileType(AopPointcutExpressionFileType.INSTANCE, AopPointcutExpressionFileType.INSTANCE.getDefaultExtension());
        }
      }
    }.execute();
  }

  @Override
  protected void tearDown() throws Exception {
    myAopModel = null;
    super.tearDown();
  }

  @AfterClass
  public void globalTearDown() {
    new WriteCommandAction(getProject()) {
      @Override
      protected void run(Result result) throws Throwable {
        FileTypeManager.getInstance()
          .removeAssociatedExtension(AopPointcutExpressionFileType.INSTANCE, AopPointcutExpressionFileType.INSTANCE.getDefaultExtension());
      }
    }.execute();
  }

  public void testClassNameCompletionAnnotationFiltering() throws Throwable {
    myFixture.addClass("package foo; public @interface AxBxCxDx {}");
    myFixture.addClass("package bar; public interface AyByCyDy {}");
    myFixture.configureByText(AopPointcutExpressionFileType.INSTANCE, "@annotation(ABCD<caret>)");
    myFixture.complete(CompletionType.CLASS_NAME);
    assertSameElements(myFixture.getLookupElementStrings(), "AxBxCxDx");
  }

  public void testPackageReference1() throws Throwable {
    checkCompleted("execution(* x|.*())", "execution(* xxx.|*())");
  }

  public void testPackageReference2() throws Throwable {
    checkCompleted("execution(* x|.*", "execution(* xxx.|*");
  }

  public void testPackageReference3() throws Throwable {
    checkCompleted("execution(* x|.", "execution(* xxx.|");
  }

  public void testPackageReference4() throws Throwable {
    checkCompleted("execution(* x|", "execution(* xxx.|");
  }

  public void testClassReference1() throws Throwable {
    checkCompleted("execution(* java.S|.*())", "execution(* java.String|.*())");
  }

  public void testClassReference2() throws Throwable {
    checkCompleted("execution(* java.S|", "execution(* java.String|");
  }

  public void testNoFieldReference() throws Throwable {
    assertNoVariants("execution(* java.String.fiel|");
  }

  public void testMethodReference1() throws Throwable {
    checkCompleted("execution(* java.String.s|", "execution(* java.String.substring()|");
  }

  public void testMethodReference2() throws Throwable {
    checkCompleted("execution(* java.String.s|(", "execution(* java.String.substring()|");
  }

  public void testMethodReference3() throws Throwable {
    checkCompleted("execution(* java.String.fo|", "execution(* java.String.foo(|)");
  }

  public void testMiddleOfPointcut() throws Throwable {
    checkCompletionVariants(AopPointcutExpressionFileType.INSTANCE, "exec<caret>ution", "execution");
  }

  public void testPointcutReference1() throws Throwable {
    assertNoVariants("fu|");
  }

  public void testPointcutReference2() throws Throwable {
    checkCompleted("xa|", "xa.b.c.d()|");
  }

  public void testPointcutReference3() throws Throwable {
    checkCompleted("xa.b.c|", "xa.b.c.d()|");
  }

  public void testPointcutReference4() throws Throwable {
    assertNoVariants("jav|");
  }

  public void testPointcutSelfReference() throws Throwable {
    assertNoVariants("some|");
  }

  public void testPointcutReference6() throws Throwable {
    checkCompleted("Obj|", "ObjsomePointcut()|");
  }

  public void testPointcutReference6RightParen() throws Throwable {
    checkCompleted("Obj|()", "ObjsomePointcut()|");
  }

  public void testPointcutReference7() throws Throwable {
    //checkCompleted(AopPointcutExpressionFileType.INSTANCE, "Obj|", "ObjsomePointcut()|", getContext().getInnerClasses()[0]);
    checkCompleted(AopPointcutExpressionFileType.INSTANCE, "Obj|", "ObjsomePointcut()|");
  }

  public void testPrivatePointcutReferenceInXml() throws Throwable {
    PsiClass pointcutsClass = myFixture.addClass("import org.aspectj.lang.annotation.*;" +
                                                 "public class Pointcuts {" +
                                                                      "@Pointcut(\"\") public void pointcutPublic() {}" +
                                                                      "@Pointcut(\"\") private void pointcutPrivate() {}" +
                                                                      "@Pointcut(\"\") protected void pointcutProtected() {}" +
                                                                      "@Pointcut(\"\") void pointcutPackageLocal() {}" +
                                                                      "}");
    myFixture.setFileContext(PsiFileFactory.getInstance(getProject()).createFileFromText("a.xml", "<a/>"));
    myAopModel = new LocalAopModel(pointcutsClass, null, new AllAdvisedElementsSearcher(myFixture.getPsiManager()));
    checkCompleted("Pointcuts.poi|", "Pointcuts.pointcutPublic()|");
  }

  public void testPrivatePointcutReference() throws Throwable {
    assertNoVariants("AnotherAspect.priv|");
  }

  public void testParameterReference1() throws Throwable {
    checkCompleted("somePointcut(arg|)", "somePointcut(argName|)");
  }

  public void testNoLong() throws Throwable {
    assertNoVariants("lon|");
  }

  public void testInt() throws Throwable {
    checkCompleted("execution(i|", "execution(int |");
  }

  public void testByte() throws Throwable {
    checkCompleted("execution(by|", "execution(byte |");
  }

  public void testLong() throws Throwable {
    checkCompleted("execution(lo|", "execution(long |");
  }

  public void testNoPrimitiveInFunctionName1() throws Throwable {
    assertNoVariants("execution(long lo|");
  }
  
  public void testNoPrimitiveInTarget() throws Throwable {
    assertNoVariants("target(boole|");
  }

  public void testNoPrimitiveInFunctionName2() throws Throwable {
    assertNoVariants("execution(long vo|");
  }

  public void testVoid1() throws Throwable {
    checkCompleted("execution(vo|", "execution(void |");
  }

  public void testVoid2() throws Throwable {
    checkCompleted("execution(private vo|", "execution(private void |");
  }

  public void testNoVoid() throws Throwable {
    checkCompleted("execution(* vo|", "execution(* vo|");
  }

  public void testExecution1() throws Throwable {
    checkCompleted("ex|", "execution(|)");
  }

  public void testExecution2() throws Throwable {
    checkCompleted("(ex|", "(execution(|)");
  }

  public void testExecution3() throws Throwable {
    checkCompleted("(aaa()) && ex|", "(aaa()) && execution(|)");
  }

  public void testAtWithin1() throws Throwable {
    checkCompleted("@w|", "@within(|)");
  }

  public void testAtWithin2() throws Throwable {
    checkCompleted("@within(xx|)", "@within(xxx.|)");
  }

  public void testAtWithin3() throws Throwable {
    assertNoVariants("@within(bool|)");
  }
  
  public void testAtWithin3RightParen() throws Throwable {
    checkCompleted("@with|()", "@within(|)");
  }

  public void testArgs() throws Throwable {
    checkCompleted("ar|", "args(|)");
  }

  public void testAtArgs() throws Throwable {
    checkCompleted("@ar|", "@args(|)");
  }

  public void testThis() throws Throwable {
    checkCompleted("th|", "this(|)");
  }

  public void testAtThis() throws Throwable {
    checkCompleted("@th|", "@this(|)");
  }

  public void testTarget() throws Throwable {
    checkCompleted("ta|", "target(|)");
  }

  public void testAtTarget() throws Throwable {
    checkCompleted("@ta|", "@target(|)");
  }

  public void testAtAnnotation() throws Throwable {
    checkCompleted("@an|", "@annotation(|)");
  }

  public void testPublic1() throws Throwable {
    checkCompleted("execution(pu|", "execution(public |");
  }

  public void testPublic2() throws Throwable {
    checkCompletionVariants(AopPointcutExpressionFileType.INSTANCE, "execution(pu|a", "public");
  }

  private PsiClass getContext() {
    return JavaPsiFacade.getInstance(myFixture.getProject()).findClass("Object239");
  }

  public void testPublic3() throws Throwable {
    assertNoVariants("execution(private pu|");
  }

  public void testPublic4() throws Throwable {
    assertNoVariants("execution(private !final pu|");
  }

  public void testPrivate() throws Throwable {
    checkCompleted("execution(pri|", "execution(private |");
  }

  public void testProtected() throws Throwable {
    checkCompleted("execution(pro|", "execution(protected |");
  }

  public void testSynchronized1() throws Throwable {
    checkCompleted("execution(syn|", "execution(synchronized |");
  }

  public void testSynchronized2() throws Throwable {
    checkCompleted("execution(private syn|", "execution(private synchronized |");
  }

  public void testSynchronized3() throws Throwable {
    assertNoVariants("execution(static syn|");
  }

  public void testSynchronized4() throws Throwable {
    assertNoVariants("execution(static private syn|");
  }

  public void testStatic1() throws Throwable {
    checkCompleted("execution(sta|", "execution(static |");
  }

  public void testStatic2() throws Throwable {
    assertNoVariants("execution(synchronized sta|");
  }

  public void testFinal() throws Throwable {
    checkCompleted("execution(fi|", "execution(final |");
  }

  public void testThrows() throws Throwable {
    checkCompleted("execution(* *() thr|", "execution(* *() throws |");
  }

  private void assertNoVariants(@NonNls final String text) throws Throwable {
    assertNoVariants(AopPointcutExpressionFileType.INSTANCE, text);
  }

  private void checkCompleted(@NonNls final String text, @NonNls final String resultText) throws Throwable {
    checkCompleted(AopPointcutExpressionFileType.INSTANCE, text, resultText);
  }

  protected void tuneCompletionFile(final PsiFile file) {
    ((AopPointcutExpressionFile) file).setAopModel(myAopModel);
  }

}
