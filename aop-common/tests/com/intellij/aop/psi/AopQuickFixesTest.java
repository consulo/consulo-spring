/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopLiteFixture;
import com.intellij.aop.ArgNamesManipulator;
import com.intellij.aop.JavaArgNamesManipulator;
import com.intellij.aop.jam.AopAdviceImpl;
import static com.intellij.aop.jam.AopConstants.*;
import com.intellij.aop.jam.SetArgNamesQuickFix;
import com.intellij.facet.FacetManager;
import com.intellij.jam.JamService;
import com.intellij.lang.StdLanguages;
import consulo.application.Result;
import consulo.document.util.TextRange;
import consulo.language.editor.WriteCommandAction;
import consulo.language.inject.impl.internal.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringApplicationComponent;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.facet.SpringFacetType;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.testFramework.MockProblemDescriptor;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.xml.util.XmlTagUtil;
import consulo.language.psi.EmptyResolveMessageProvider;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.PsiReference;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nonnull;

import java.util.Arrays;

/**
 * @author peter
 */
public class AopQuickFixesTest extends JavaCodeInsightFixtureTestCase {

  protected void setUp() throws Exception {
    super.setUp();

    AopLiteFixture.addAopAnnotations(myFixture);

    myFixture.addClass("package org.aspectj.lang; public interface JoinPoint  {}");
    myFixture.addClass("package org.aspectj.lang; public interface ProceedingJoinPoint  {}");
  }

  public void testReturningReference() throws Throwable {
    PsiClass psiClass =
      parseClass("@" + ASPECT_ANNO +
                 " class A { @" + AFTER_RETURNING_ANNO + "(value=\"\", returning=\"argName\") " +
                 "public void a(" + PROCEEDING_JOIN_POINT + " pjp, int argName, boolean b, short s) {}");

    PsiMethod method = psiClass.getMethods()[0];
    PsiAnnotation annotation = method.getModifierList().getAnnotations()[0];
    JavaArgNamesManipulator manipulator = new JavaArgNamesManipulator(getAdvice(method));
    PsiReference reference = manipulator.getReturningReference();
    assertInstanceOf(reference, EmptyResolveMessageProvider.class);
    assertEquals("argName", reference.getCanonicalText());
    assertEquals(annotation.findAttributeValue(RETURNING_PARAM), reference.getElement());
    assertEquals(TextRange.from(1, "argName".length()), reference.getRangeInElement());
    assertSameElements(reference.getVariants(), Arrays.asList(method.getParameterList().getParameters()).subList(1, 4).toArray());
  }

  private AopAdviceImpl getAdvice(PsiMethod method) {
    return JamService.getJamService(getProject()).getJamElement(
      AopAdviceImpl.class, method);
  }

  public void testDefineArgNamesQuickFix() throws Throwable {
    checkDefineFix("@" + BEFORE_ANNO + "(value=\"\") public void a(int i) {}",
                   "@" + BEFORE_ANNO + "(value=\"\", argNames = \"i\")");

    checkDefineFix("@" + BEFORE_ANNO + "(value=\"\") public void a(int i, int j) {}",
                   "@" + BEFORE_ANNO + "(value=\"\", argNames = \"i,j\")");

    checkDefineFix("@" + BEFORE_ANNO + "(value=\"\") public void a(" + PROCEEDING_JOIN_POINT + " p, int i) {}",
                   "@" + BEFORE_ANNO + "(value=\"\", argNames = \"p,i\")");

    checkDefineFix("@" + BEFORE_ANNO + "(\"\") public void a(" + PROCEEDING_JOIN_POINT + " p, int i) {}",
                   "@" + BEFORE_ANNO + "(value = \"\", argNames = \"p,i\")");
  }

  public void testRemoveArgNamesQuickFix() throws Throwable {
    checkRemoveFix("@" + BEFORE_ANNO + "(value=\"\", argNames=\"argName\") public void a(int i) {}",
                   "@" + BEFORE_ANNO + "(value=\"\" )");

    checkRemoveFix("@" + BEFORE_ANNO + "(value=\"\", argNames=\"argName\") public void a(int i, int j) {}",
                   "@" + BEFORE_ANNO + "(value=\"\" )");

    checkRemoveFix("@" + BEFORE_ANNO + "(value=\"\", argNames=\"arg\" + \"Name\") public void a(" + PROCEEDING_JOIN_POINT + " p, int i) {}",
                   "@" + BEFORE_ANNO + "(value=\"\" )");
  }

  private void checkDefineFix(String textBefore, String textAfter) {
    String classText = "@" + ASPECT_ANNO + " class A{" + textBefore + "}";
    PsiFile file = PsiFileFactory.getInstance(myFixture.getProject()).createFileFromText("a.java", StdLanguages.JAVA, classText, true, false);
    final PsiMethod method = ((PsiJavaFile)file).getClasses()[0].getMethods()[0];
    PsiAnnotation annotation = method.getModifierList().getAnnotations()[0];
    final ArgNamesManipulator manipulator = new JavaArgNamesManipulator(getAdvice(method));
    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        new SetArgNamesQuickFix("", true, manipulator, method)
          .applyFix(myFixture.getProject(), new MockProblemDescriptor(manipulator.getArgNamesProblemElement(), "", null));
      }
    }.execute();
    assertEquals(textAfter, annotation.getText());
  }

  private void checkRemoveFix(String textBefore, String textAfter) {
    PsiMethod method = parseClass("@" + ASPECT_ANNO + " class A{" + textBefore + "}").getMethods()[0];
    PsiAnnotation annotation = method.getModifierList().getAnnotations()[0];
    ArgNamesManipulator manipulator = new JavaArgNamesManipulator(getAdvice(method));
    new SetArgNamesQuickFix("", false, manipulator, method).applyFix(myFixture.getProject(), new MockProblemDescriptor(manipulator.getArgNamesProblemElement(), "", null));
    assertEquals(textAfter, annotation.getText());
  }

  protected PsiClass parseClass(@NonNls String text) {
    return ((PsiJavaFile) PsiFileFactory.getInstance(myFixture.getProject()).createFileFromText("a.java", text)).getClasses()[0];
  }

  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/aop/fixes/";
  }

  public void testSpringArgNamesManipulator() throws Throwable {
    myFixture.configureByFile(getTestName(false) + ".xml");
    XmlFile file = (XmlFile)myFixture.getFile();
    final ArgNamesManipulator manipulator = getManipulator(file);

    XmlTag pointcutTag = file.getDocument().getRootTag().getSubTags()[0].getSubTags()[0];
    assertEquals(pointcutTag.getAttribute("arg-names").getValueElement(), manipulator.getArgNamesProblemElement());

    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        manipulator.setArgNames("a,b,c");
      }
    }.execute();
    assertEquals("a,b,c", pointcutTag.getAttributeValue("arg-names"));
    assertEquals(pointcutTag.getAttribute("arg-names").getValueElement(), manipulator.getArgNamesProblemElement());

    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        manipulator.setArgNames(null);
      }
    }.execute();
    assertNull(pointcutTag.getAttributeValue("arg-names"));
    assertEquals(XmlTagUtil.getStartTagNameElement(pointcutTag), manipulator.getArgNamesProblemElement());
  }

  @Nonnull
  private ArgNamesManipulator getManipulator(XmlFile file) {
    AopPointcutExpressionFile aopFile = (AopPointcutExpressionFile) consulo.language.inject.impl.internal.InjectedLanguageUtil.findElementAtNoCommit(file, myFixture.getEditor().getCaretModel().getOffset()).getContainingFile();
    return aopFile.getAopModel().getArgNamesManipulator();
  }

  private XmlFile createXmlFile(String text) {
    return (XmlFile) PsiFileFactory.getInstance(myFixture.getProject()).createFileFromText("a.xml", text);
  }

  public void testSpringArgNamesManipulator_Returning() throws Throwable {
    myFixture.configureByFile(getTestName(false) + ".xml");
    XmlFile file = (XmlFile) myFixture.getFile();
    ArgNamesManipulator manipulator = getManipulator(file);

    XmlTag adviceTag = file.getDocument().getRootTag().getSubTags()[0].getSubTags()[0].getSubTags()[0];
    assertEquals(adviceTag.getAttribute(RETURNING_PARAM).getValueElement().getReferences()[0], manipulator.getReturningReference());
  }

  public void testSpringArgNamesManipulator_Throwing() throws Throwable {
    myFixture.configureByFile(getTestName(false) + ".xml");
    XmlFile file = (XmlFile) myFixture.getFile();
    ArgNamesManipulator manipulator = getManipulator(file);

    XmlTag adviceTag = file.getDocument().getRootTag().getSubTags()[0].getSubTags()[0].getSubTags()[0];
    assertEquals(adviceTag.getAttribute(THROWING_PARAM).getValueElement().getReferences()[0], manipulator.getThrowingReference());
  }

  public void testAddAspectjAutoproxyQuickFix() throws Throwable {
    checkAddAspectjAutoproxy();
  }

  private void checkAddAspectjAutoproxy() throws Throwable {
    myFixture.enableInspections(new SpringApplicationComponent());
    myFixture.configureByFiles("AddAspectjAutoproxyQuickFix.java", getTestName(false) + ".xml");
    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        SpringFacet facet = FacetManager.getInstance(myModule).addFacet(SpringFacetType.INSTANCE, "s", null);
        SpringFileSet fileSet = new SpringFileSet("a", "a", facet.getConfiguration());
        facet.getConfiguration().getFileSets().add(fileSet);
        fileSet.addFile(myFixture.getTempDirFixture().getFile(getTestName(false) + ".xml"));
      }
    }.execute();
    myFixture.launchAction(assertOneElement(myFixture.filterAvailableIntentions("Add <aop:aspectj-autoproxy")));
    myFixture.checkResultByFile(getTestName(false) + ".xml", getTestName(false) + "_after.xml", true);
  }

  public void testAddAspectjAutoproxyQuickFixAndDefineNamespace() throws Throwable {
    checkAddAspectjAutoproxy();
  }

  public void testAddAspectjAutoproxyQuickFixDtd() throws Throwable {
    checkAddAspectjAutoproxy();
 }

  public void testDeclareParentsImplementInterface() throws Throwable {
    myFixture.enableInspections(SpringApplicationComponent.getSpringInspectionClasses());

    myFixture.configureByFile(getTestName(false) + ".xml");
    myFixture.launchAction(myFixture.findSingleIntention("Create Interface foo.Intf"));
    myFixture.checkResultByFile("foo/Intf.java", getTestName(false) + "_after.java", true);
  }

}
