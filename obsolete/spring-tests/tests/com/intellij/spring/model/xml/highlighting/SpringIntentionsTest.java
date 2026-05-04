/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import org.jetbrains.annotations.NonNls;

/**
 * @author Dmitry Avdeev
 */
public class SpringIntentionsTest extends SpringHighlightingTestCase {

  public void testInjectionValue() throws Throwable {
    doIntentionTest("Add \"value\" attribute");
  }

  public void testCtorArgsFix() throws Throwable {
    doIntentionTest("Add <constructor-arg>(s) for 'BeanWithConstructor(FooBean, int, int[])'");
  }

  public void testCtorArgsFixTemplate() throws Throwable {
    doIntentionTest("Add <constructor-arg>(s) for 'Boolean(String)'");
  }

  public void testCreateBeanFromList() throws Throwable {
    doIntentionTest("Create new bean 'second'");
  }

  public void testCreateBeanFromProperty() throws Throwable {
    doIntentionTest("Create new bean 'ref'");
  }

  public void testCreateBeanFromArgType() throws Throwable {
    doIntentionTest("Create new bean 'xxx'");
  }

  public void testCreateBeanFromArgIndex() throws Throwable {
    doIntentionTest("Create new bean 'xxx'");
  }

  public void testPropertyValue() throws Throwable {
    doIntentionTest("Replace <value> element with \"value\" attribute");
  }

  public void testIntPropertyValue() throws Throwable {
    doIntentionTest("Replace <value> element with \"value\" attribute");
  }

  public void testPropertyRef() throws Throwable {
    doIntentionTest("Replace <ref> element with \"ref\" attribute");
  }

  public void testCtorArgValue() throws Throwable {
    doIntentionTest("Replace <value> element with \"value\" attribute");
  }

  public void testCtorArgRef() throws Throwable {
    doIntentionTest("Replace <ref> element with \"ref\" attribute");
  }

  public void testMarkAsAbstract() throws Throwable {
    doIntentionTest("Mark bean as abstract");
  }

  public void testPropertyValueList() throws Throwable {
    doIntentionTest(SpringBundle.message("model.inspection.injection.value.add.list"));
  }

  public void testPropertyValueMap() throws Throwable {
    doIntentionTest(SpringBundle.message("model.inspection.injection.value.add.map"));
  }

  public void testPropertyValueArray() throws Throwable {
    doIntentionTest(SpringBundle.message("model.inspection.injection.value.add.list"));
  }


  private void doIntentionTest(String actionText) throws Throwable {
    String file = getTestName(true);
    CodeInsightTestUtil.doIntentionTest(myFixture, file, actionText);
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/intentions/";
  }
}
