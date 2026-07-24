/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import consulo.language.psi.PsiElement;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import org.jetbrains.annotations.NonNls;

import java.util.function.Consumer;

/**
 * @author peter
 */
@SuppressWarnings({"ConstantConditions"})
public class AopPsiTest extends JavaCodeInsightFixtureTestCase {

  public void testReferenceExpression() throws Throwable {
    String com = "com";
    String comWithin = com + ".within";
    String comWithinSystemArchitecture = comWithin + ".SystemArchitecture";
    String fullRefText = comWithinSystemArchitecture + ".businessService";
    String fullText = fullRefText + "()";

    PsiPointcutReferenceExpression pointcutRef = assertInstanceOf(parse(fullText), PsiPointcutReferenceExpression.class);
    assertEquals(fullText, pointcutRef.getText());

    AopReferenceExpression topRef = pointcutRef.getReferenceExpression();
    assertEquals(fullRefText, topRef.getText());

    AopReferenceExpression classRef = topRef.getQualifier();
    assertEquals(comWithinSystemArchitecture, classRef.getText());

    AopReferenceExpression withinRef = classRef.getQualifier();
    assertEquals(comWithin, withinRef.getText());

    AopReferenceExpression comRef = withinRef.getQualifier();
    assertEquals(com, comRef.getText());

    assertNull(comRef.getQualifier());
  }

  public void testSmallReferenceExpression() throws Throwable {
    String fullRefText = "businessService";
    String fullText = fullRefText + "()";

    PsiPointcutReferenceExpression pointcutRef = assertInstanceOf(parse(fullText), PsiPointcutReferenceExpression.class);
    assertEquals(fullText, pointcutRef.getText());
    assertNull(pointcutRef.getReferenceExpression().getQualifier());
  }

  public void testWithin() throws Throwable {
    String refText = "com.xyz.someapp.trading..*";
    String fullText = "within(" + refText + ")";
    PsiTypedPointcutExpression expression = assertInstanceOf(parse(fullText), PsiWithinExpression.class);
    assertEquals(fullText, expression.getText());

    AopReferenceHolder typeRef = expression.getTypeReference();
    assertEquals(refText, typeRef.getText());

    AopReferenceExpression ref = assertInstanceOf(typeRef.getTypeExpression(), AopReferenceExpression.class);
    assertEquals(refText, ref.getText());
  }

  public void testAtWithin() throws Throwable {
    String refText = "com.xyz.someapp.trading.Transactional";
    String fullText = "@within(" + refText + ")";
    PsiAtWithinExpression expression = assertInstanceOf(parse(fullText), PsiAtWithinExpression.class);
    assertEquals(fullText, expression.getText());

    AopReferenceHolder typeRef = expression.getTypeReference();
    assertEquals(refText, typeRef.getText());

    AopReferenceExpression ref = assertInstanceOf(typeRef.getTypeExpression(), AopReferenceExpression.class);
    assertEquals(refText, ref.getText());
  }

  public void testThis() throws Throwable {
    String refText = "com.xyz.someapp.trading.Transactional";
    String fullText = "this(" + refText + ")";

    PsiThisExpression expression = assertInstanceOf(parse(fullText), PsiThisExpression.class);
    assertEquals(fullText, expression.getText());

    AopReferenceHolder typeRef = expression.getTypeReference();
    assertEquals(refText, typeRef.getText());

    AopReferenceExpression ref = assertInstanceOf(typeRef.getTypeExpression(), AopReferenceExpression.class);
    assertEquals(refText, ref.getText());
  }

  public void testTarget() throws Throwable {
    String refText = "com.xyz.someapp.trading.Transactional";
    String fullText = "target(" + refText + ")";

    PsiTargetExpression expression = assertInstanceOf(parse(fullText), PsiTargetExpression.class);
    assertEquals(fullText, expression.getText());

    AopReferenceHolder typeRef = expression.getTypeReference();
    assertEquals(refText, typeRef.getText());

    AopReferenceExpression ref = assertInstanceOf(typeRef.getTypeExpression(), AopReferenceExpression.class);
    assertEquals(refText, ref.getText());
  }

  public void testAtThis() throws Throwable {
    String refText = "com.xyz.someapp.trading.Transactional";
    String fullText = "@this(" + refText + ")";

    PsiAtThisExpression expression = assertInstanceOf(parse(fullText), PsiAtThisExpression.class);
    assertEquals(fullText, expression.getText());

    AopReferenceHolder typeRef = expression.getTypeReference();
    assertEquals(refText, typeRef.getText());

    AopReferenceExpression ref = assertInstanceOf(typeRef.getTypeExpression(), AopReferenceExpression.class);
    assertEquals(refText, ref.getText());
  }

  public void testAtTarget() throws Throwable {
    String refText = "com.xyz.someapp.trading.Transactional";
    String fullText = "@target(" + refText + ")";

    PsiAtTargetExpression expression = assertInstanceOf(parse(fullText), PsiAtTargetExpression.class);
    assertEquals(fullText, expression.getText());

    AopReferenceHolder typeRef = expression.getTypeReference();
    assertEquals(refText, typeRef.getText());

    AopReferenceExpression ref = assertInstanceOf(typeRef.getTypeExpression(), AopReferenceExpression.class);
    assertEquals(refText, ref.getText());
  }

  public void testAtAnnotation() throws Throwable {
    String refText = "com.xyz.someapp.trading.Transactional";
    String fullText = "@annotation(" + refText + ")";

    PsiAtAnnotationExpression expression = assertInstanceOf(parse(fullText), PsiAtAnnotationExpression.class);
    assertEquals(fullText, expression.getText());

    AopReferenceHolder typeRef = expression.getTypeReference();
    assertEquals(refText, typeRef.getText());

    AopReferenceExpression ref = assertInstanceOf(typeRef.getTypeExpression(), AopReferenceExpression.class);
    assertEquals(refText, ref.getText());
  }

  public void testArgs() throws Throwable {
    final String ref = "java.io..*";
    String fullText = "args(..,*," + ref + ")";
    PsiArgsExpression expression = assertInstanceOf(parse(fullText), PsiArgsExpression.class);
    assertEquals(fullText, expression.getText());

    AopAbstractList list = expression.getParameterList();

    assertOrderedCollection(list.getParameters(), new Consumer<PsiElement>() {
      public void consume(PsiElement element) {
        assertEquals("..", element.getText());
      }
    }, new Consumer<PsiElement>() {
      public void consume(PsiElement pattern) {
        assertEquals("*", pattern.getText());
        assertEquals("*", assertInstanceOf(((AopReferenceHolder)pattern).getTypeExpression(), AopReferenceExpression.class).getText());
      }
    }, new Consumer<PsiElement>() {
      public void consume(PsiElement pattern) {
        assertEquals(ref, pattern.getText());
        assertEquals(ref, assertInstanceOf(((AopReferenceHolder)pattern).getTypeExpression(), AopReferenceExpression.class).getText());
      }
    });
  }
  
  public void testAtArgs() throws Throwable {
    final String ref = "java.io.Foo";
    String fullText = "@args(" + ref + ")";
    PsiAtArgsExpression expression = assertInstanceOf(parse(fullText), PsiAtArgsExpression.class);
    assertEquals(fullText, expression.getText());

    AopAbstractList list = expression.getParameterList();

    assertOrderedCollection(list.getParameters(), new Consumer<PsiElement>() {
      public void consume(PsiElement pattern) {
        assertEquals(ref, pattern.getText());
        assertEquals(ref, assertInstanceOf(((AopReferenceHolder)pattern).getTypeExpression(), AopReferenceExpression.class).getText());
      }
    });
  }

  public void testParenthesesNotBinary() throws Throwable {
    String exprText = "foo()";
    String secondText = "bar()";
    String notText = "not " + secondText;
    String firstText = "(" + exprText + ")";
    String fullText = firstText + " or " + notText + "";

    AopBinaryExpression binaryExpression = assertInstanceOf(parse(fullText), AopBinaryExpression.class);
    assertEquals(fullText, binaryExpression.getText());

    AopParenthesizedExpression parExpr = assertInstanceOf(binaryExpression.getOperands().first, AopParenthesizedExpression.class);
    assertEquals(firstText, parExpr.getText());

    assertEquals(exprText, assertInstanceOf(parExpr.getInnerPointcutExpression(), PsiPointcutReferenceExpression.class).getText());

    AopNotExpression notExpression = assertInstanceOf(binaryExpression.getOperands().second, AopNotExpression.class);
    assertEquals(notText, notExpression.getText());
    assertEquals(secondText, notExpression.getInnerExpression().getText());
  }

  public void testBinaryOps() throws Throwable {
    assertBinaryOp("foo() and bar()", AopBinaryExpression.AopOperation.AND);
    assertBinaryOp("foo() && bar()", AopBinaryExpression.AopOperation.AND);
    assertBinaryOp("foo() || bar()", AopBinaryExpression.AopOperation.OR);
    assertBinaryOp("foo() or bar()", AopBinaryExpression.AopOperation.OR);
  }

  private void assertBinaryOp(String text, AopBinaryExpression.AopOperation expected) {
    assertEquals(expected, assertInstanceOf(parse(text), AopBinaryExpression.class).getOperation());
  }

  public void testExecution() throws Throwable {
    String modifiersText = "public not static";
    String qualifier = "com";
    String methodNamePattern = "set*A*b";
    String methodRefText = qualifier + ".." + methodNamePattern;
    final String thr1Text = "com..*";
    final String thr2Text = "java.lang.*";
    String throwsListText = "throws " + thr1Text + ", " + thr2Text;
    String paramsText = "..,*";
    String fullText = "execution(" + modifiersText + " * " + methodRefText + "(" + paramsText + ") " + throwsListText + ")";

    PsiExecutionExpression expression = assertInstanceOf(parse(fullText), PsiExecutionExpression.class);
    assertEquals(fullText, expression.getText());

    assertEquals(modifiersText, expression.getModifierList().getText());
    assertEquals(paramsText, expression.getParameterList().getText());
    assertEquals("*", expression.getReturnType().getText());

    AopMemberReferenceExpression methodRef = expression.getMethodReference();
    assertEquals(methodRefText, methodRef.getReferenceExpression().getText());
    assertEquals(qualifier, methodRef.getReferenceExpression().getQualifier().getText());

    AopThrowsList throwsList = expression.getThrowsList();
    assertEquals(throwsListText, throwsList.getText());
    assertOrderedCollection(throwsList.getExceptionPatterns(), new Consumer<AopReferenceHolder>() {
      public void consume(AopReferenceHolder item) {
        assertEquals(thr1Text, item.getText());
      }
    }, new Consumer<AopReferenceHolder>() {
      public void consume(AopReferenceHolder item) {
        assertEquals(thr2Text, item.getText());
      }
    });
  }

  public void testReferenceExpressionResolvability() throws Throwable {
    assertResolvable("com.intellij.rulez", AopReferenceExpression.Resolvability.PLAIN);
    assertResolvable("com.intellij..rulez", AopReferenceExpression.Resolvability.NONE);
    assertResolvable("com.intellij.*", AopReferenceExpression.Resolvability.POLYVARIANT);
    assertResolvable("com.*.rulez", AopReferenceExpression.Resolvability.NONE);
    assertResolvable("com..rulez", AopReferenceExpression.Resolvability.NONE);
    assertResolvable("com", AopReferenceExpression.Resolvability.PLAIN);
    assertResolvable("com.Object+.z", AopReferenceExpression.Resolvability.NONE);
    assertResolvable("com.Object+.*", AopReferenceExpression.Resolvability.NONE);
    assertResolvable("*", AopReferenceExpression.Resolvability.NONE);
  }

  private void assertResolvable(String refText, AopReferenceExpression.Resolvability resolvable) {
    PsiExecutionExpression expression= assertInstanceOf(parse("execution(* " + refText + ".foo())"), PsiExecutionExpression.class);
    AopReferenceQualifier refExpr = expression.getMethodReference().getReferenceExpression().getGeneralizedQualifier();
    assertNotNull(refExpr);
    assertEquals(resolvable, refExpr.getResolvability());
  }

  private PsiPointcutExpression parse(@NonNls String code) {
    PsiFile file = PsiFileFactory.getInstance(getProject()).createFileFromText("a.b", AopPointcutExpressionFileType.INSTANCE, code);
    return ((AopPointcutExpressionFile)file).getPointcutExpression();
  }

  public void testWildcards() throws Throwable {
    PsiElement[] parameters = ((PsiArgsExpression)parse("args(List<? extends T>, List<? super T>, List<?>)")).getParameterList().getParameters();
    assertOrderedCollection(parameters, createWildcardChecker(true, false), createWildcardChecker(false, true), createWildcardChecker(false, false));
  }

  private static Consumer<PsiElement> createWildcardChecker(final boolean anExtends, final boolean aSuper) {
    return new Consumer<PsiElement>() {
      public void consume(PsiElement element) {
        AopTypeExpression expression = assertInstanceOf(element, AopReferenceHolder.class).getTypeExpression();
        AopTypeParameterList list = assertInstanceOf(expression, AopGenericTypeExpression.class).getTypeParameterList();
        AopReferenceHolder holder = assertInstanceOf(assertOneElement(list.getParameters()), AopReferenceHolder.class);
        AopWildcardExpression wildcard = assertInstanceOf(holder.getTypeExpression(), AopWildcardExpression.class);
        assertEquals(anExtends, wildcard.isExtends());
        assertEquals(aSuper, wildcard.isSuper());
        if (anExtends || aSuper) {
          assertEquals("T", wildcard.getBound().getText());
        } else {
          assertNull(wildcard.getBound());
        }
      }
    };
  }

  public void testArrays() throws Throwable {
    PsiArgsExpression expression = (PsiArgsExpression)parse("args(int[], int...)");
    PsiElement[] parameters = expression.getParameterList().getParameters();
    AopArrayExpression array = assertInstanceOf(((AopReferenceHolder)parameters[0]).getTypeExpression(), AopArrayExpression.class);
    AopArrayExpression varargs = assertInstanceOf(((AopReferenceHolder)parameters[1]).getTypeExpression(), AopArrayExpression.class);
    assertEquals("int", array.getTypeReference().getText());
    assertEquals("int", varargs.getTypeReference().getText());
    assertFalse(array.isVarargs());
    assertTrue(varargs.isVarargs());
  }

}
