/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import org.jetbrains.annotations.NonNls;

import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class SpringFixesTest extends SpringHighlightingTestCase<JavaModuleFixtureBuilder> {

  protected boolean isWithTestSources() {
    return false;
  }

  public void testMethodReplacerFix() throws Throwable {
    final List<IntentionAction> list = myFixture.getAvailableIntentions("methodReplacerFix.xml", "MethodReplacerBean.java");
    final IntentionAction action = CodeInsightTestUtil
      .findIntentionByText(list, "Make 'MethodReplacerBean' implement 'org.springframework.beans.factory.support.MethodReplacer'");
    assertNotNull(action);
    myFixture.launchAction(action);
    myFixture.checkResultByFile("MethodReplacerBean.java", "MethodReplacerBean_after.java", false);
  }

  public void testInnerBeanClass() throws Throwable {
    myFixture.testRename("innerBeanClassRename.xml", "innerBeanClassRename_after.xml", "NewName", "InnerBean.java");
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    if (getName().equals("testMethodReplacerFix")) {
      addSpringJar(moduleBuilder);
    }
  }

  @NonNls
  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/quickfixes/";
  }
}
