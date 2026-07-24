/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopLiteFixture;
import com.intellij.facet.FacetManager;
import consulo.application.Result;
import consulo.language.psi.PsiElement;
import com.intellij.psi.PsiParameter;
import com.intellij.spring.facet.SpringFacetType;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.lang.java.JavaRefactoringSupportProvider;
import consulo.language.editor.WriteCommandAction;

/**
 * @author peter
 */
public class SpringAopRenameTest extends JavaCodeInsightFixtureTestCase {
  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/refactoring/aop/";
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        FacetManager.getInstance(myModule).addFacet(SpringFacetType.INSTANCE, "xx", null);
      }
    }.execute();
  }

  public void testBeanPointcutRename() throws Throwable {
    myFixture.testRename(getTestName(false) + ".xml", getTestName(false) + "_after.xml", "newName");
  }

  public void testReturningParameter() throws Throwable {
    AopLiteFixture.addAopAnnotations(myFixture);

    myFixture.testRename(getTestName(false) + ".java", getTestName(false) + "_after.java", "newName");

    PsiElement atCaret = myFixture.getFile().findElementAt(myFixture.getEditor().getCaretModel().getOffset()).getParent();
    assertInstanceOf(atCaret, PsiParameter.class);
    assertTrue(JavaRefactoringSupportProvider.mayRenameInplace(atCaret, atCaret));
  }

}
