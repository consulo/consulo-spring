/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.refactoring.rename.BeanPropertyRenameHandler;
import com.intellij.spring.model.highlighting.InjectionValueStyleInspection;
import com.intellij.spring.model.properties.SpringPropertiesUtil;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.util.containers.CollectionFactory;
import org.jetbrains.annotations.NonNls;

import java.util.*;

public class SpringBeanPropertiesHighLightingTest extends SpringHighlightingTestCase<JavaModuleFixtureBuilder> {
  private static final Set<String> WANT_TESTDATA = CollectionFactory.newTroveSet("testSpringPropertyRef", "testSpringPropertyInnerBean", "testSpringPropertyListSetMapProperties", "testSpringPropertyValueType", "testParentProperty");

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
  }

  protected boolean isWithTestSources() {
    return WANT_TESTDATA.contains(getName());
  }

  public void testSpringPropertyRef() throws Throwable {
    myFixture.testHighlighting("spring-bean-property-ref.xml");
  }

  public void testSpringPropertyInnerBean() throws Throwable {
    myFixture.testHighlighting("spring-bean-property-inner-bean.xml");
  }

  
  public void testSpringPropertyListSetMapProperties() throws Throwable {
    myFixture.testHighlighting("spring-bean-property-collections-maps.xml");
  }

  public void testSpringPropertyValueType() throws Throwable {
    final InjectionValueStyleInspection inspection = new InjectionValueStyleInspection();
    myFixture.disableInspections(inspection);
    myFixture.testHighlighting("spring-bean-property-value-type.xml");
  }

  public void testBeansInCollections() throws Throwable {
    myFixture.testHighlighting("spring-beans-in-collections.xml");
  }

  public void testCollectionChildren() throws Throwable {
    myFixture.testHighlighting("spring-collection-children.xml");
  }

  public void testPropertyRename() throws Throwable {
    new WriteCommandAction.Simple(myProject) {
      protected void run() throws Throwable {
        myFixture.configureByFiles("propertyRename.xml", "RenameBeanProperty.java");
        final BeanProperty beanProperty = SpringPropertiesUtil.getBeanProperty(myFixture.getEditor(), myFixture.getFile());
        BeanPropertyRenameHandler.doRename(beanProperty, "newName", false);
        myFixture.checkResultByFile("propertyRename_after.xml");
        myFixture.checkResultByFile("RenameBeanProperty.java", "RenameBeanProperty_after.java", true);
      }
    }.execute().throwException();
  }

  public void testParentProperty() throws Throwable {
    myFixture.testHighlighting(true, false, false, "parentProperty.xml");
  }

  public void testPNamespaceSetterUsages() throws Throwable {
    final PsiClass psiClass = myFixture.addClass("package foo.bar; " + "public class PTestBean {" + "    public void setFoo(String s){}" + "}");
    myFixture.configureByFile(getTestName(true) + ".xml");
    final Collection<PsiReference> usages = MethodReferencesSearch.search(psiClass.getMethods()[0]).findAll();
    assertEquals(1, usages.size());
  }

  public void testCreatePropertyQuickFix() throws Throwable {
    checkCreatePropertyFix("createPropertyQuickFix.xml", "CreatePropertyQuickFix.after", "Create setter for");
  }

  public void testCreatePropertyQuickFixPNamespaceRef() throws Throwable {
    checkCreatePropertyFix("createPropertyQuickFix2.xml", "CreatePropertyQuickFix2.after", "Create setter for");
  }

  public void testCreatePropertyQuickFixPNamespaceValue() throws Throwable {
    checkCreatePropertyFix("createPropertyQuickFix3.xml", "CreatePropertyQuickFix3.after", "Create setter for");
  }

  private void checkCreatePropertyFix(final String path, final String afterFile, final String prefix) throws Throwable {
    final List<IntentionAction> actions = myFixture.getAvailableIntentions(path, "CreatePropertyQuickFix.java", "FooBean.java");
    Collections.sort(actions, new Comparator<IntentionAction>() {

      public int compare(final IntentionAction o1, final IntentionAction o2) {
        return o1.getText().compareTo(o2.getText());
      }
    });

    for (IntentionAction action: actions) {
      if (action.getText().startsWith(prefix)) {
        myFixture.launchAction(action);
      }
    }
    myFixture.checkResultByFile("CreatePropertyQuickFix.java", afterFile, true);
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }
}