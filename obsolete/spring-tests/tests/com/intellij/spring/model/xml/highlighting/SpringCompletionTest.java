/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.template.TemplateManager;
import consulo.ide.impl.idea.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.completion.CompletionType;
import consulo.ide.impl.idea.codeInsight.completion.CodeCompletionHandlerBase;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.application.Result;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

import java.util.List;

public class SpringCompletionTest extends SpringHighlightingTestCase<JavaModuleFixtureBuilder> {
  protected boolean myOldSetting;

  protected void setUp() throws Exception {
    super.setUp();
    myOldSetting = CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CLASS_NAME_COMPLETION;
    CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CLASS_NAME_COMPLETION = true;    
  }

  @Override
  protected void tearDown() throws Exception {
    CodeInsightSettings.getInstance().AUTOCOMPLETE_ON_CLASS_NAME_COMPLETION = myOldSetting;
    super.tearDown();
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
    if (getTestName(false).contains("TagNameCompletion")) {
      addSpringJar(moduleBuilder);    
    }
  }

  public void testPropertyCompletion() throws Throwable {

    myFixture.testCompletion("property.xml", "property_after.xml");
    myFixture.testCompletion("18.xml", "18_after.xml");
    myFixture.testCompletion("18_2.xml", "18_2_after.xml");
    myFixture.testCompletion("18_3.xml", "18_3_after.xml");
    myFixture.testCompletion("18_4.xml", "18_4_after.xml");
  }

  public void testConstructorArgCompletion() throws Throwable {
    myFixture.testCompletion("constructorArgRef.xml", "constructorArgRef_after.xml");
    myFixture.testCompletionVariants("constructorArgIndex.xml", "0");
  }

  public void testAliasNameCompletion() throws Throwable {
    myFixture.testCompletion("aliasName.xml", "aliasName_after.xml");
  }

  public void testDependsOn() throws Throwable {
    myFixture.testCompletion("dependsOn.xml", "dependsOn_after.xml");
  }

  public void testParentProperty() throws Throwable {
    myFixture.testCompletion("parentProperty.xml", "parentProperty_after.xml");
  }

  public void testBeanClass() throws Throwable {
    myFixture.testCompletion("beanClass.xml", "beanClass_after.xml");
  }

  public void testInnerBeanClass() throws Throwable {
    myFixture.testCompletion("innerBeanClass.xml", "innerBeanClass_after.xml");
  }

  public void testQualifierCompleteion() throws Throwable {
    myFixture.testCompletion("beanClass.xml", "beanClass_after.xml");
  }

  public void testPackagePrefixRoot() throws Throwable {
    doTestPackagePrefix();
  }

  public void testPackagePrefixThird() throws Throwable {
    doTestPackagePrefix();
  }

  private void doTestPackagePrefix() throws Throwable {
    new WriteCommandAction(myProject) {
      protected void run(Result result) throws Throwable {
        final ModifiableRootModel model = ModuleRootManager.getInstance(myModule).getModifiableModel();
        model.getContentEntries()[0].getSourceFolders()[0].setPackagePrefix("xxx.yyy.zzz");
        model.commit();
      }
    }.execute();

    myFixture.addFileToProject("/foo/a.java", "package xxx.yyy.zzz.foo; class Foo {}");

    myFixture.testCompletion(getTestName(true) + ".xml", getTestName(true) + "_after.xml");
  }

  public void testListOrSetReferences() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "list_or_set_references.xml");
    addFileToSet(fileSet, "list_or_set_references2.xml");

    final SpringBean springBean = getBeanFromFile("list_or_set_references.xml", "listBean");

    final SpringPropertyDefinition listProperty = SpringUtils.findPropertyByName(springBean, "list");
    final SpringPropertyDefinition genericsListProperty = SpringUtils.findPropertyByName(springBean, "genericsList");

    assertListOrSetVariants(((SpringProperty)listProperty).getList(), 7, 2);
    assertListOrSetVariants(((SpringProperty)genericsListProperty).getList(), 7, 2);

    assertListOrSetVariants(springBean.getConstructorArgs().get(0).getList(), 7, 2);
  }

  public void testCollectionReferences() throws Throwable {
    //	 IDEADEV-18943
    myFixture.testCompletion("collectionReferenceBefore.xml", "collectionReferenceAfter.xml");
  }

  private void assertListOrSetVariants(ListOrSet listOrSet, final int beanVariantsCount, final int localVariantsCount) {
    final List<SpringRef> list = listOrSet.getRefs();
    final SpringRef beanRef = list.get(0);
    final SpringRef localRef = list.get(1);
    final Object[] beanVariants = beanRef.getBean().getXmlAttributeValue().getReferences()[0].getVariants();
    final Object[] localVariants = localRef.getLocal().getXmlAttributeValue().getReferences()[0].getVariants();

    assertEquals(beanVariantsCount, beanVariants.length);
    assertEquals(localVariantsCount, localVariants.length);
  }

  public void testPAttributes() throws Throwable {
    myFixture.testCompletionVariants("pAttributes.xml", "id", "name", "parent", "abstract", "lazy-init", "autowire", "autowire-candidate",
                                     "dependency-check", "depends-on", "init-method", "destroy-method", "factory-method", "factory-bean",
                                     "scope", "xml:base","xml:id", "xml:lang", "xml:space", "xsi:nill", "xsi:noNamespaceSchemaLocation",
                                     "xsi:schemaLocation", "xsi:type", "p:contextClassLoader", "p:contextClassLoader-ref", "p:daemon",
                                     "p:name", "p:name-ref", "p:priority");
  }

  public void testBeanId() throws Throwable {
    myFixture.testCompletionVariants("spring-beans-id-completion.xml", "unknown_StringBean", "s");
    myFixture.testCompletionVariants("spring-beans-id-completion_2.xml", "unknown_ListBean", "unknown_StringBean");
  }

  public void testDomOverridesSchema() throws Throwable {
    myFixture.testCompletionVariants("spring-beans-abstract-property.xml", "false", "true");
    myFixture.addClass("package org.springframework.transaction; public interface PlatformTransactionManager {}");
    myFixture.testCompletionVariants("spring-beans-transaction-manager.xml", "transx");
  }

  protected void doTagNameCompletion(final String file) throws Throwable {
    new WriteCommandAction.Simple(myProject) {
      protected void run() throws Throwable {
        myFixture.configureByFile(file + ".xml");
        new CodeCompletionHandlerBase(CompletionType.CLASS_NAME).invoke(myProject, myFixture.getEditor(), myFixture.getFile());
        myFixture.checkResultByFile(file + "_after.xml");
      }
    }.execute().throwException();
  }

  public void testTagNameCompletion() throws Throwable {
    doTagNameCompletion("tagNameCompletion");
  }

  public void testUnqualifiedTagNameCompletion() throws Throwable {
    doTagNameCompletion("unqualifiedTagNameCompletion");
  }

  public void testInnerTagNameCompletion() throws Throwable {
    doTagNameCompletion("innerTagNameCompletion");
  }

  public void testInnerTagNameCompletion2() throws Throwable {
    doTagNameCompletion("innerTagNameCompletion2");
  }

  public void testAttributeLiveTemplateStaysInTheTabOnPackageCompletion() throws Throwable {
    ((TemplateManagerImpl)TemplateManager.getInstance(myProject)).setTemplateTesting(true);
    try {
      myFixture.configureByFile(getTestName(false) + ".xml");
      myFixture.completeBasic();
      myFixture.type('j');
      myFixture.type('a');
      myFixture.type('v');
      myFixture.type('a');
      myFixture.type('.');
      myFixture.checkResultByFile(getTestName(false) + "_after.xml");
    }
    finally {
      ((TemplateManagerImpl)TemplateManager.getInstance(myProject)).setTemplateTesting(false);
    }
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/completion/";
  }
}
