/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import com.intellij.facet.FacetManager;
import consulo.application.Result;
import consulo.ide.impl.idea.openapi.application.PathManager;
import consulo.language.editor.WriteCommandAction;
import com.intellij.spring.facet.SpringFacetType;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import consulo.util.collection.ArrayUtil;

import java.io.File;

/**
 * @author peter
 */
public class SpringAopCompletionTest extends JavaCodeInsightFixtureTestCase {
  @Override
  protected void tuneFixture(JavaModuleFixtureBuilder moduleBuilder) {
    if ("testSpringPointcutsWithBean".equals(getName())) {
      moduleBuilder
        .addLibraryJars("spring2_5", consulo.ide.impl.idea.openapi.application.PathManager.getHomePath().replace(File.separatorChar, '/') + "/svnPlugins/spring/spring-tests/testData/", "spring2_5.jar");
    }
  }

  public void testAllBeanNames() throws Throwable {
    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        FacetManager.getInstance(myModule).addFacet(SpringFacetType.INSTANCE, SpringFacetType.INSTANCE.getPresentableName(), null);
      }
    }.execute().throwException();

    myFixture.testCompletionVariants(getTestName(false) + ".xml", "abc", "abcd", "axxx", "aspectBean");
  }

  public void testSpringPointcuts() throws Throwable {
    myFixture.testCompletionVariants(getTestName(false) + ".xml", SpringAopCompletionContributor.SPRING20_AOP_POINTCUTS);
  }

  public void testSpringPointcutsWithBean() throws Throwable {
    myFixture.testCompletionVariants("SpringPointcuts.xml", ArrayUtil.append(SpringAopCompletionContributor.SPRING20_AOP_POINTCUTS, "bean"));
  }

  public void testArgsFinishWithLParen() throws Throwable {
    myFixture.configureByFile(getTestName(false) + ".xml");
    myFixture.completeBasic();
    myFixture.type('a');
    myFixture.type('r');
    myFixture.type('(');
    myFixture.checkResultByFile(getTestName(false) + "_after.xml");
  }

  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/aop/completion/";
  }


}
