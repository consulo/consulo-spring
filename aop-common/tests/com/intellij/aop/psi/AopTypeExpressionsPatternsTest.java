/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.LocalAopModel;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import com.intellij.testFramework.LiteFixture;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import java.util.function.Consumer;

import consulo.application.util.function.Processor;
import consulo.language.psi.PsiFileFactory;

import java.io.IOException;
import java.util.Collection;

/**
 * @author peter
 */
public class AopTypeExpressionsPatternsTest extends JavaCodeInsightFixtureTestCase {
  private PsiClass myContext;

  protected void setUp() throws Exception {
    super.setUp();
    myContext = myFixture.addClass("import foo.bar.*; class A {}");
  }

  protected void tearDown() throws Exception {
    myContext = null;
    super.tearDown();
  }

  public void testReferenceExpression() throws Throwable {
    assertEquals("foo.bar", assertInstanceOf(assertOneElement(parse("foo.bar").getPatterns()), PsiClassTypePattern.class).getText());
    assertEquals("foo.*..bar", assertInstanceOf(assertOneElement(parse("foo.*..bar").getPatterns()), PsiClassTypePattern.class).getText());
  }

  public void testParentheses() throws Throwable {
    assertEquals("foo.bar", assertInstanceOf(assertOneElement(parse("(foo.bar)").getPatterns()), PsiClassTypePattern.class).getText());
  }
  
  public void testPrimitive() throws Throwable {
    assertEquals(PsiType.BOOLEAN, assertInstanceOf(assertOneElement(parse("boolean").getPatterns()), PsiPrimitiveTypePattern.class).getType());
  }

  public void testOr() throws Throwable {
    assertUnorderedCollection(parse("foo||int").getPatterns(), new Consumer<AopPsiTypePattern>() {
      public void consume(AopPsiTypePattern aopPsiTypePattern) {
        assertEquals("foo", assertInstanceOf(aopPsiTypePattern, PsiClassTypePattern.class).getText());
      }
    }, new Consumer<AopPsiTypePattern>() {
      public void consume(AopPsiTypePattern aopPsiTypePattern) {
        assertEquals(PsiType.INT, assertInstanceOf(aopPsiTypePattern, PsiPrimitiveTypePattern.class).getType());
      }
    });
  }

  public void testAnd() throws Throwable {
    assertUnorderedCollection(assertInstanceOf(assertOneElement(parse("foo.*&&*.bar").getPatterns()), AndPsiTypePattern.class).getPatterns(), new Consumer<AopPsiTypePattern>() {
      public void consume(AopPsiTypePattern aopPsiTypePattern) {
        assertEquals("foo.*", assertInstanceOf(aopPsiTypePattern, PsiClassTypePattern.class).getText());
      }
    }, new Consumer<AopPsiTypePattern>() {
      public void consume(AopPsiTypePattern aopPsiTypePattern) {
        assertEquals("*.bar", assertInstanceOf(aopPsiTypePattern, PsiClassTypePattern.class).getText());
      }
    });
  }

  public void testSubtypePattern() throws Throwable {
    assertEquals("foo", assertInstanceOf(assertInstanceOf(assertOneElement(parse("foo+").getPatterns()), SubtypePattern.class).getBoundPattern(), PsiClassTypePattern.class).getText());
  }

  public void testGenerics() throws Throwable {
    GenericPattern pattern =
      assertInstanceOf(assertOneElement(parse("foo<bar,?,? extends foo, ? super bar>").getPatterns()), GenericPattern.class);
    assertEquals("foo", assertInstanceOf(pattern.getErasure(), PsiClassTypePattern.class).getText());
    assertOrderedCollection(pattern.getParameters(), new Consumer<AopPsiTypePattern>() {
      public void consume(AopPsiTypePattern aopPsiTypePattern) {
        assertEquals("bar", assertInstanceOf(aopPsiTypePattern, PsiClassTypePattern.class).getText());
      }
    }, new Consumer<AopPsiTypePattern>() {
      public void consume(AopPsiTypePattern aopPsiTypePattern) {
        assertNull(assertInstanceOf(aopPsiTypePattern, WildcardPattern.class).getBound());
      }
    }, new Consumer<AopPsiTypePattern>() {
      public void consume(AopPsiTypePattern aopPsiTypePattern) {
        WildcardPattern wildcardPattern = assertInstanceOf(aopPsiTypePattern, WildcardPattern.class);
        assertEquals("foo", assertInstanceOf(wildcardPattern.getBound(), PsiClassTypePattern.class).getText());
        assertFalse(wildcardPattern.isSuper());
      }
    }, new Consumer<AopPsiTypePattern>() {
      public void consume(AopPsiTypePattern aopPsiTypePattern) {
        WildcardPattern wildcardPattern = assertInstanceOf(aopPsiTypePattern, WildcardPattern.class);
        assertEquals("bar", assertInstanceOf(wildcardPattern.getBound(), PsiClassTypePattern.class).getText());
        assertTrue(wildcardPattern.isSuper());
      }
    });

  }

  public void testEnumInGenerics() throws Throwable {
    Collection<AopPsiTypePattern> patterns = parse("foo<foo||bar,int||long>").getPatterns();
    assertUnorderedCollection(patterns, new Consumer<AopPsiTypePattern>() {
      public void consume(AopPsiTypePattern aopPsiTypePattern) {
        AopPsiTypePattern[] parameters = assertInstanceOf(aopPsiTypePattern, GenericPattern.class).getParameters();
        assertEquals("foo", ((PsiClassTypePattern)parameters[0]).getText());
        assertEquals(PsiType.INT, ((PsiPrimitiveTypePattern)parameters[1]).getType());
      }
    }, new Consumer<AopPsiTypePattern>() {
      public void consume(AopPsiTypePattern aopPsiTypePattern) {
        AopPsiTypePattern[] parameters = assertInstanceOf(aopPsiTypePattern, GenericPattern.class).getParameters();
        assertEquals("foo", ((PsiClassTypePattern)parameters[0]).getText());
        assertEquals(PsiType.LONG, ((PsiPrimitiveTypePattern)parameters[1]).getType());
      }
    }, new Consumer<AopPsiTypePattern>() {
      public void consume(AopPsiTypePattern aopPsiTypePattern) {
        AopPsiTypePattern[] parameters = assertInstanceOf(aopPsiTypePattern, GenericPattern.class).getParameters();
        assertEquals("bar", ((PsiClassTypePattern)parameters[0]).getText());
        assertEquals(PsiType.INT, ((PsiPrimitiveTypePattern)parameters[1]).getType());
      }
    }, new Consumer<AopPsiTypePattern>() {
      public void consume(AopPsiTypePattern aopPsiTypePattern) {
        AopPsiTypePattern[] parameters = assertInstanceOf(aopPsiTypePattern, GenericPattern.class).getParameters();
        assertEquals("bar", ((PsiClassTypePattern)parameters[0]).getText());
        assertEquals(PsiType.LONG, ((PsiPrimitiveTypePattern)parameters[1]).getType());
      }
    });
  }

  public void testArray() throws Throwable {
    assertTrue(assertInstanceOf(assertOneElement(parse("foo...").getPatterns()), ArrayPattern.class).isVarargs());
    assertFalse(assertInstanceOf(assertOneElement(parse("foo[]").getPatterns()), ArrayPattern.class).isVarargs());
  }

  public void testNot() throws Throwable {
    assertInstanceOf(assertOneElement(parse("!foo").getPatterns()), NotPattern.class);
  }

  public void testAsterisk() throws Throwable {
    assertEquals(AopPsiTypePattern.TRUE, assertOneElement(parse("*").getPatterns()));
  }

  public void testNonReferenceQualifiers() throws Throwable {
    ConcatenationPattern pattern = assertInstanceOf(assertOneElement(parse("(int).bar").getPatterns()), ConcatenationPattern.class);
    assertEquals(PsiType.INT, assertInstanceOf(pattern.getLeft(), PsiPrimitiveTypePattern.class).getType());
    assertEquals("bar", assertInstanceOf(pattern.getRight(), PsiClassTypePattern.class).getText());
    assertFalse(pattern.isDoubleDot());
  }

  public void testDontHonorImports() throws Throwable {
    myFixture.addClass("package foo.bar; public class XObject {}");
    assertEquals("XObject.*", assertInstanceOf(assertOneElement(parse("XObject.*").getPatterns()), PsiClassTypePattern.class).getText());
    assertEquals("java.lang.Object.*", assertInstanceOf(assertOneElement(parse("Object.*").getPatterns()), PsiClassTypePattern.class).getText());
  }

  private AopTypeExpression parse(String text) throws IOException {
    AopPointcutExpressionFile file = (AopPointcutExpressionFile) PsiFileFactory.getInstance(getProject()).createFileFromText("a.b", AopPointcutExpressionFileType.INSTANCE, "args(" + text + ")");
    LiteFixture.setContext(file, myContext);
    file.setAopModel(new LocalAopModel(new AopAdvisedElementsSearcher(getPsiManager()) {
      public boolean process(Processor<PsiClass> processor) {
        throw new UnsupportedOperationException("Method doProcess is not yet implemented");
      }
    }));
    return ((AopReferenceHolder)((PsiArgsExpression)file.getPointcutExpression()).getParameterList().getParameters()[0]).getTypeExpression();
  }
}
