/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import consulo.language.util.IncorrectOperationException;
import consulo.application.util.function.Processor;
import gnu.trove.THashSet;

import java.util.Arrays;
import java.util.Set;

/**
 * @author peter
 */
public class PsiTypePatternsTest extends JavaCodeInsightFixtureTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    myFixture.addClass("package java.lang; public @interface FooAnno {}");
    myFixture.addClass("package java.lang.java.lang; public class Object {}");
    myFixture.addClass("package java.bar; @FooAnno public class MyList<T> {}");
    myFixture.addClass("package java.bar; public class MyArrayList<T> extends MyList<T> {}");
    myFixture.addClass("package foo; public class Bar { public class Zip {} }");
  }

  public void testPrimitiveTypes() throws Throwable {
    AopPsiTypePattern voidPattern = new PsiPrimitiveTypePattern(PsiType.VOID);
    assertTrue(voidPattern.accepts(PsiType.VOID));
    assertFalse(voidPattern.accepts(PsiType.INT));
    assertEquals(PointcutMatchDegree.FALSE, voidPattern.canBeAssignableFrom(PsiType.INT));

    assertSame(AopPsiTypePattern.FALSE, AopPsiTypePatternsUtil.conjunctPatterns(voidPattern, new PsiPrimitiveTypePattern(PsiType.INT)));
    assertSame(voidPattern, AopPsiTypePatternsUtil.conjunctPatterns(voidPattern, new PsiPrimitiveTypePattern(PsiType.VOID)));
  }

  public void testClasses() throws Throwable {
    PsiType javaLangObject = createPsiType("java.lang.Object");
    PsiType javaLangJavaLangObject = createPsiType("java.lang.java.lang.Object");
    PsiType javaUtilList = createPsiType("java.bar.MyList");
    PsiType fooBar = createPsiType("foo.Bar");
    PsiType fooBarZip = createPsiType("foo.Bar.Zip");

    PsiClassTypePattern pattern = new PsiClassTypePattern("java.*");
    assertFalse(pattern.accepts(javaLangObject));
    assertFalse(pattern.accepts(javaLangJavaLangObject));
    assertFalse(pattern.accepts(javaUtilList));
    assertFalse(pattern.accepts(fooBar));
    assertFalse(pattern.accepts(fooBarZip));

    pattern = new PsiClassTypePattern("java.lang.*");
    assertTrue(pattern.accepts(javaLangObject));
    assertFalse(pattern.accepts(javaLangJavaLangObject));
    assertFalse(pattern.accepts(javaUtilList));
    assertFalse(pattern.accepts(fooBar));
    assertFalse(pattern.accepts(fooBarZip));

    pattern = new PsiClassTypePattern("java..*");
    assertTrue(pattern.accepts(javaLangObject));
    assertTrue(pattern.accepts(javaLangJavaLangObject));
    assertTrue(pattern.accepts(javaUtilList));
    assertFalse(pattern.accepts(fooBar));
    assertFalse(pattern.accepts(fooBarZip));

    pattern = new PsiClassTypePattern("foo..*");
    assertFalse(pattern.accepts(javaLangObject));
    assertFalse(pattern.accepts(javaLangJavaLangObject));
    assertFalse(pattern.accepts(javaUtilList));
    assertTrue(pattern.accepts(fooBar));
    assertTrue(pattern.accepts(fooBarZip));

    pattern = new PsiClassTypePattern("*.Bar.*");
    assertFalse(pattern.accepts(javaLangObject));
    assertFalse(pattern.accepts(javaLangJavaLangObject));
    assertFalse(pattern.accepts(javaUtilList));
    assertFalse(pattern.accepts(fooBar));
    assertTrue(pattern.accepts(fooBarZip));

    pattern = new PsiClassTypePattern("foo.*");
    assertFalse(pattern.accepts(javaLangObject));
    assertFalse(pattern.accepts(javaLangJavaLangObject));
    assertFalse(pattern.accepts(javaUtilList));
    assertTrue(pattern.accepts(fooBar));
    assertFalse(pattern.accepts(fooBarZip));

    pattern = new PsiClassTypePattern("...$%@#*^*)^*X# %^*^R^E&%^$%^$%#^$@*&^ #T)&(*ytwrndfjkbyf4 y2w qenruoift0q 8976r123789t6789%&^%(*OUHBJT(V%RFGNBjv >, d.ak.sd f.as lfli709r4377oil....");
    assertFalse(pattern.accepts(javaLangObject));
    assertFalse(pattern.accepts(javaLangJavaLangObject));
    assertFalse(pattern.accepts(javaUtilList));
    assertFalse(pattern.accepts(fooBar));
    assertFalse(pattern.accepts(fooBarZip));
  }

  private PsiType createPsiType(String qname) throws IncorrectOperationException {
    return JavaPsiFacade.getInstance(getProject()).getElementFactory().createTypeFromText(qname, null);
  }

  private PsiClassType createPsiType(PsiClass aClass) {
    return JavaPsiFacade.getInstance(getProject()).getElementFactory().createType(aClass);
  }

  public void testSubtype() throws Throwable {
    SubtypePattern pattern = new SubtypePattern(AopPsiTypePattern.TRUE);
    assertTrue(pattern.accepts(PsiType.VOID));
    assertTrue(pattern.accepts(PsiType.INT));
    assertTrue(pattern.accepts(createPsiType("java.lang.Object")));
    assertTrue(pattern.accepts(createPsiType("java.bar.MyList")));

    pattern = new SubtypePattern(new PsiClassTypePattern("java..*"));
    assertFalse(pattern.accepts(PsiType.VOID));
    assertFalse(pattern.accepts(PsiType.INT));
    assertTrue(pattern.accepts(createPsiType("java.lang.Object")));
    assertTrue(pattern.accepts(createPsiType("java.bar.MyList")));

    pattern = new SubtypePattern(new PsiClassTypePattern("foo..*"));
    assertFalse(pattern.accepts(PsiType.VOID));
    assertFalse(pattern.accepts(PsiType.INT));
    assertFalse(pattern.accepts(createPsiType("java.lang.Object")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList")));
    assertTrue(pattern.accepts(createPsiType(myFixture.addClass("package ggg; public class A extends foo.Bar {}"))));
  }

  public void testArray() throws Throwable {
    ArrayPattern pattern = new ArrayPattern(AopPsiTypePattern.TRUE, false);
    assertFalse(pattern.accepts(PsiType.VOID));
    assertFalse(pattern.accepts(PsiType.INT));
    assertTrue(pattern.accepts(new PsiArrayType(PsiType.INT)));
    assertFalse(pattern.accepts(new PsiEllipsisType(PsiType.INT)));
    assertTrue(pattern.accepts(new PsiArrayType(createPsiType("java.lang.Object"))));

    pattern = new ArrayPattern(new PsiClassTypePattern("*.lang.*"), false);
    assertFalse(pattern.accepts(PsiType.VOID));
    assertFalse(pattern.accepts(new PsiArrayType(PsiType.INT)));
    assertFalse(pattern.accepts(new PsiEllipsisType(PsiType.INT)));
    assertTrue(pattern.accepts(new PsiArrayType(createPsiType("java.lang.Object"))));
    assertFalse(pattern.accepts(new PsiArrayType(createPsiType("java.bar.MyList"))));
    assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(new PsiArrayType(createPsiType("java.bar.MyList"))));
    assertEquals(PointcutMatchDegree.FALSE, pattern.canBeAssignableFrom(new PsiArrayType(PsiType.INT)));

    pattern = new ArrayPattern(AopPsiTypePattern.TRUE, true);
    assertFalse(pattern.accepts(PsiType.VOID));
    assertFalse(pattern.accepts(PsiType.INT));
    assertFalse(pattern.accepts(new PsiArrayType(PsiType.INT)));
    assertTrue(pattern.accepts(new PsiEllipsisType(PsiType.INT)));
  }

  public void testSimpleGenerics() throws Throwable {
    GenericPattern pattern = new GenericPattern(new PsiClassTypePattern("java.bar.MyList"), new PsiClassTypePattern("java.lang.Object"));
    assertFalse(pattern.accepts(createPsiType("java.lang.Object")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyArrayList")));
    assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyArrayList")));
    assertEquals(PointcutMatchDegree.FALSE, pattern.canBeAssignableFrom(createPsiType("int")));

    assertTrue(pattern.accepts(createPsiType("java.bar.MyList<java.lang.Object>")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyArrayList<java.lang.Object>")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyArrayList<foo.Bar>")));

    assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyArrayList<java.lang.Object>")));
    assertEquals(PointcutMatchDegree.FALSE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyArrayList<foo.Bar>")));
  }

  public void testUnboundedWildcard() throws Throwable {
    GenericPattern pattern = new GenericPattern(new PsiClassTypePattern("java.bar.MyList"), new WildcardPattern(null, true));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<java.lang.Object>")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<? extends java.lang.Object>")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<? super java.lang.Object>")));
    assertTrue(pattern.accepts(createPsiType("java.bar.MyList<?>")));

    assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyList<java.lang.Object>")));
    assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyArrayList<java.lang.Object>")));
    assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyArrayList<foo.Bar>")));
  }

  public void testExtendsWildcard() throws Throwable {
    GenericPattern pattern = new GenericPattern(new PsiClassTypePattern("java.bar.MyList"), new WildcardPattern(new PsiClassTypePattern("java.bar.MyList"), false));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<java.lang.Object>")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<? extends java.lang.Object>")));
    assertTrue(pattern.accepts(createPsiType("java.bar.MyList<? extends java.bar.MyList>")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<? super java.lang.Object>")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<?>")));

    assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyList<java.bar.MyList>")));
    assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyArrayList<java.bar.MyList>")));
    assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyArrayList<? extends java.bar.MyList>")));
    assertEquals(PointcutMatchDegree.FALSE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyArrayList<foo.Bar>")));
  }
  
  public void testSuperWildcard() throws Throwable {
    GenericPattern pattern = new GenericPattern(new PsiClassTypePattern("java.bar.MyList"), new WildcardPattern(new PsiClassTypePattern("java.bar.MyList"), true));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<java.lang.Object>")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<? extends java.lang.Object>")));
    assertTrue(pattern.accepts(createPsiType("java.bar.MyList<? super java.bar.MyList>")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<? extends java.bar.MyList>")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<? super java.lang.Object>")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<?>")));

    assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyList<java.bar.MyList>")));
    assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyList<? super java.bar.MyList>")));
    //assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyList<java.lang.Object>")));
    //assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyList<? super java.lang.Object>")));
    assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyArrayList<java.bar.MyList>")));
    assertEquals(PointcutMatchDegree.TRUE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyArrayList<? super java.bar.MyList>")));
    assertEquals(PointcutMatchDegree.FALSE, pattern.canBeAssignableFrom(createPsiType("java.bar.MyArrayList<foo.Bar>")));
  }

  public void testSubtypeAsGenericParameter() throws Throwable {
    GenericPattern pattern = new GenericPattern(new PsiClassTypePattern("java.bar.MyList"), new SubtypePattern(new PsiClassTypePattern("java.lang.Object")));
    assertTrue(pattern.accepts(createPsiType("java.bar.MyList<java.lang.Object>")));
    assertTrue(pattern.accepts(createPsiType("java.bar.MyList<java.bar.MyList>")));
    assertTrue(pattern.accepts(createPsiType("java.bar.MyList<java.bar.MyArrayList>")));
    assertTrue(pattern.accepts(createPsiType("java.bar.MyList<? extends java.bar.MyList>")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<?>")));

    pattern = new GenericPattern(new PsiClassTypePattern("java.bar.MyList"), new SubtypePattern(new PsiClassTypePattern("java.bar.MyList")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<java.lang.Object>")));
    assertTrue(pattern.accepts(createPsiType("java.bar.MyList<java.bar.MyList>")));
    assertTrue(pattern.accepts(createPsiType("java.bar.MyList<java.bar.MyArrayList>")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<? extends java.bar.MyList>")));
    assertFalse(pattern.accepts(createPsiType("java.bar.MyList<?>")));
  }

  public void testConcatenation() throws Throwable {
    ConcatenationPattern pattern = new ConcatenationPattern(new PsiClassTypePattern("foo"), new PsiClassTypePattern("Bar"), false);
    assertTrue(pattern.accepts("foo.Bar"));
    assertFalse(pattern.accepts("foo.Bar.Zip"));
    assertFalse(pattern.accepts("foo.xxx.Zip"));

    pattern = new ConcatenationPattern(new PsiClassTypePattern("foo"), new PsiClassTypePattern("Bar"), true);
    assertTrue(pattern.accepts("foo.Bar"));
    assertFalse(pattern.accepts("foo.Bar.Zip"));
    assertTrue(pattern.accepts("foo.xxx.Bar"));
    assertTrue(pattern.accepts("foo.xxx.yyy.Bar"));

    pattern = new ConcatenationPattern(new PsiClassTypePattern("foo"), AopPsiTypePattern.TRUE, true);
    assertTrue(pattern.accepts("foo.Bar"));
    assertTrue(pattern.accepts("foo.Bar.Zip"));
    assertTrue(pattern.accepts("foo.xxx.Bar"));
    assertTrue(pattern.accepts("foo.xxx.yyy.Bar"));
    assertFalse(pattern.accepts("java.lang.Object"));

    pattern = new ConcatenationPattern(new PsiClassTypePattern("java.lang"), AopPsiTypePattern.TRUE, true);
    assertFalse(pattern.accepts("foo.Bar"));
    assertTrue(pattern.accepts("java.lang.Object"));
    assertTrue(pattern.accepts("java.lang.java.lang.Object"));
    assertFalse(pattern.accepts("java.bar.MyList"));
  }

  public void testNot() throws Throwable {
    NotPattern pattern = new NotPattern(new PsiClassTypePattern("foo"));
    assertTrue(pattern.accepts(createPsiType("foo.Bar")));
    assertFalse(pattern.accepts("foo"));
    assertTrue(pattern.accepts("foo1"));
    assertTrue(pattern.accepts(PsiType.INT));

    assertFalse(new NotPattern(new PsiClassTypePattern("foo.Bar")).accepts(createPsiType("foo.Bar")));
  }

  public void testProcessPackages() throws Throwable {
    assertPackages(AopPsiTypePattern.FALSE);
    assertPackages(new PsiPrimitiveTypePattern(PsiType.INT));
    assertPackages(new ArrayPattern(AopPsiTypePattern.TRUE, true));
    assertPackages(AopPsiTypePattern.TRUE, "", "java", "java.lang", "java.lang.java", "java.lang.java.lang", "java.bar", "foo");
    assertPackages(new SubtypePattern(new PsiPrimitiveTypePattern(PsiType.INT)), "", "java", "java.lang", "java.lang.java", "java.lang.java.lang", "java.bar", "foo");
    assertPackages(new NotPattern(new PsiPrimitiveTypePattern(PsiType.INT)), "", "java", "java.lang", "java.lang.java", "java.lang.java.lang", "java.bar", "foo");
    assertPackages(new PsiClassTypePattern("java..*"), "", "java", "java.lang", "java.lang.java", "java.lang.java.lang", "java.bar");
    assertPackages(new PsiClassTypePattern("java..lang"), "", "java", "java.lang", "java.lang.java", "java.lang.java.lang", "java.bar");
    assertPackages(new PsiClassTypePattern("java.bar"), "", "java", "java.bar");
    assertPackages(new PsiClassTypePattern("java.*"), "", "java");
    assertPackages(new PsiClassTypePattern("*.lang"), "", "java", "java.lang", "java.lang.java", "java.lang.java.lang", "java.bar", "foo");
    assertPackages(AopPsiTypePatternsUtil.conjunctPatterns(new PsiClassTypePattern("*.lang"), new PsiClassTypePattern("foo")), "", "foo");
  }

  private void assertPackages(AopPsiTypePattern pattern, String... packages) {
    final Set<String> actual = new THashSet<String>();
    assertTrue(pattern.processPackages(getPsiManager(), new Processor<PsiPackage>() {
      public boolean process(PsiPackage psiPackage) {
        actual.add(psiPackage.getQualifiedName());
        return true;
      }
    }));
    assertTrue(actual.containsAll(Arrays.asList(packages)));
  }

  public void testAnnotatedPattern() throws Throwable {
    PsiAnnotatedTypePattern pattern = new PsiAnnotatedTypePattern(new PsiClassTypePattern("*..FooAnno"));
    assertTrue(pattern.accepts(createPsiType("java.bar.MyList")));
    assertFalse(pattern.accepts(createPsiType("java.lang.Object")));
    assertFalse(pattern.accepts(createPsiType("FooAnno")));
  }

}
