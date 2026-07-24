/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import com.intellij.aop.LocalAopModel;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.testFramework.LiteFixture;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import consulo.language.psi.PsiFileFactory;

import java.io.IOException;

/**
 * @author peter
 */
public class AopPatternsTest extends JavaCodeInsightFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.addClass("class ContextClass {}");
  }

  private AopReferenceHolder parseTypeExpression(String type) throws IOException {
    AopPointcutExpressionFile file = (AopPointcutExpressionFile)PsiFileFactory.getInstance(getProject()).createFileFromText("a.b", AopPointcutExpressionFileType.INSTANCE, "execution(* *(" + type + "))");
    AopReferenceHolder type1 =
      (AopReferenceHolder)((PsiExecutionExpression)file.getPointcutExpression()).getParameterList().getParameters()[0];
    assertEquals(type, type1.getText());
    LiteFixture.setContext(file, JavaPsiFacade.getInstance(getProject()).findClass("ContextClass"));
    file.setAopModel(new LocalAopModel(new AllAdvisedElementsSearcher(getPsiManager())));
    return type1;
  }


  public void testTypeReference() throws Throwable {
    assertEquals("'_:[regex(java\\.lang\\.Object)]", parseTypeExpression(CommonClassNames.JAVA_LANG_OBJECT).getTypePattern());
    assertEquals("'_:[regex(java\\.lang\\.Object)]", parseTypeExpression("Object").getTypePattern());
    assertEquals("void", parseTypeExpression("void").getTypePattern());
    assertEquals("int[]", parseTypeExpression("int[]").getTypePattern());
    assertEquals("int...", parseTypeExpression("int...").getTypePattern());
  }

  public void testTypeReferenceLogic() throws Throwable {
    assertEquals("'_:[is(\"'_:[regex(java\\..*\\.[^\\\\.]+)]\") && is(\"'_:[regex([^\\\\.]+\\.lang\\.[^\\\\.]+)]\")]",
                 parseTypeExpression("java..* && *.lang.*").getTypePattern());
    assertEquals("'_:[is(\"'_:[regex(java\\..*\\.[^\\\\.]+)]\") || is(\"'_:[regex([^\\\\.]+\\.lang\\.[^\\\\.]+)]\")]",
                 parseTypeExpression("java..* || *.lang.*").getTypePattern());
    assertEquals("'_:[is(\"'_:[regex(java\\..*\\.[^\\\\.]+)]\") || is(\"'_:[!is(\"'_:[regex([^\\\\.]+\\.lang\\.[^\\\\.]+)]\")]\")]",
                 parseTypeExpression("java..* || !*.lang.*").getTypePattern());
    assertEquals("'_:[!is(\"int\")]", parseTypeExpression("!int").getTypePattern());
    assertEquals("'_:[is(\"'_:[!is(\"int\")]\") && is(\"'_:[is(\"'_:[is(\"void\") || is(\"boolean\")]\")]\")]",
                 parseTypeExpression("!int && (void || boolean)").getTypePattern());
  }

}
