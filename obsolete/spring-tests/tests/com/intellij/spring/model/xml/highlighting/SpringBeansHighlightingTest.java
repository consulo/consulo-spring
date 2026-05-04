/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.xml.highlighting;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.lookup.LookupValueFactory;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.highlighting.InjectionValueStyleInspection;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.LookupMethod;
import com.intellij.spring.model.xml.beans.ReplacedMethod;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.containers.ContainerUtil;
import java.util.function.Function;
import org.jetbrains.annotations.NonNls;

import java.util.Collection;
import java.util.List;

public class SpringBeansHighlightingTest extends SpringHighlightingTestCase {

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    if (getName().equals("testInclude") ||
        getName().equals("testReplacedMethod") ||
        getName().equals("testBeanScopes") ||
        getName().equals("testValueStyle")||
        getName().equals("testProxyFactoryBean")||
        getName().equals("testPlaceholderValueInspection")) {
      addSpringJar(moduleBuilder);
    }
  }

  protected boolean isWithTestSources() {
    return !getName().equals("testBeanRename");
  }

  public void testProxyFactoryBean() throws Throwable {
    myFixture.testHighlighting(true, false, true, "proxyFactoryBean.xml");
  }

  public void testInclude() throws Throwable {
    myFixture.testHighlighting("import/includeHighlightingTest.xml");

    final XmlFile xmlFile = (XmlFile)myFixture.getFile();
    final SpringModel model = SpringManager.getInstance(myProject).getSpringModelByFile(xmlFile);
    assert model != null;
    assertEquals(4, model.getConfigFiles().size());
  }

  public void testSchemas() throws Throwable {
    myFixture.testHighlighting("testSchemas.xml");
  }

  public void testDependsOnBeans() throws Throwable {
    myFixture.testHighlighting("spring-beans-depends-on.xml");
  }

  public void testDependsOnVariants() throws Throwable {
    final SpringBean springBean = getBeanFromFile("spring-beans-depends-on-variants.xml", "test5");

    final XmlAttributeValue xmlAttributeValue = springBean.getDependsOn().getXmlAttributeValue();
    assert xmlAttributeValue != null;
    final PsiReference[] references = xmlAttributeValue.getReferences();

    final Object[] variants = references[0].getVariants();
    assertEquals(3, variants.length);
  }

  public void testParentBean() throws Throwable {
    myFixture.testHighlighting(true, false, false, "spring-beans-parent.xml");
  }

  public void testPNamespaceBeanReferencesSOE() throws Throwable {
    myFixture.configureByFile(getTestName(false) + ".xml");
    SpringManager.getInstance(myProject).getLocalSpringModel((XmlFile) myFixture.getFile()).getAllDomBeans();
  }

  public void testAbstractBeanReferences() throws Throwable {
    myFixture.testHighlighting(false, false, false, "abstract-bean-references.xml");
  }

  public void testPropertiyValueInconsistency() throws Throwable {
    myFixture.disableInspections(new InjectionValueStyleInspection());
    myFixture.testHighlighting("spring-beans-property-value-inconsistency.xml");
  }

  public void testLookupMethod() throws Throwable {
    myFixture.testHighlighting("spring-beans-lookup-method.xml");

    final SpringBean springBean = getBeanFromFile("spring-beans-lookup-method.xml", "lookupTest");
    LookupMethod lookup = springBean.getLookupMethods().get(0);
    assertNotNull(lookup);

    XmlAttributeValue xmlAttributeValue = lookup.getName().getXmlAttributeValue();
    assert xmlAttributeValue != null;
    PsiReference[] references = xmlAttributeValue.getReferences();

    assertReferenceVariants(references[0], "createCommand","createCommand2","createCommand3","createCommand4", "createCommand7", "createCommandStatic");

    xmlAttributeValue = lookup.getBean().getXmlAttributeValue();
    assert xmlAttributeValue != null;
    references = xmlAttributeValue.getReferences();

    assertReferenceVariants(references[0], "fooBean","fooBean2", "fooBean3");
  }

  private static void assertReferenceVariants(PsiReference reference, String... variants) {

    assertSameElements(ContainerUtil.map2Array(reference.getVariants(), String.class, new Function<Object, String>() {
      public String fun(Object o) {
        if (o instanceof LookupElement) {
          return ((LookupElement)o).getLookupString();
        }
        return ((LookupValueFactory.LookupValueWithIcon)o).getPresentation();
      }
    }), variants);
  }

  public void testReplacedMethod() throws Throwable {
    myFixture.testHighlighting("spring-beans-replaced-method.xml");

    final SpringBean springBean = getBeanFromFile("spring-beans-replaced-method.xml", "testReplacer");
    ReplacedMethod replaced = springBean.getReplacedMethods().get(0);
    assertNotNull(replaced);

    XmlAttributeValue xmlAttributeValue = replaced.getReplacer().getXmlAttributeValue();
    assert xmlAttributeValue != null;
    PsiReference[] references = xmlAttributeValue.getReferences();

    assertReferenceVariants(references[0], "foo", "foo2", "replacer");
  }

  //todo[serega] add some assertions
  public void testDestroyAndInitMethod() throws Throwable {
    final SpringBean springBean = getBeanFromFile("spring-bean-destroy-and-init-method.xml", "foo");

    XmlAttributeValue xmlAttributeValue = springBean.getDestroyMethod().getXmlAttributeValue();
    assert xmlAttributeValue != null;
    PsiReference[] references = xmlAttributeValue.getReferences();

    xmlAttributeValue = springBean.getInitMethod().getXmlAttributeValue();
    assert xmlAttributeValue != null;
    references = xmlAttributeValue.getReferences();
  }

  public void testSpringBeansParentVariants() throws Throwable {
    final DomFileElement<Beans> fileElement = getFileElement("spring-beans-parent.xml", Beans.class, myProject);

    final String beanId = "test";

    final SpringBean springBean = findBeanById(fileElement.getRootElement(), beanId);
    assertNotNull("Cannot find bean = " + beanId  , springBean);

    final XmlAttributeValue xmlAttributeValue = springBean.getParentBean().getXmlAttributeValue();
    assertNotNull(xmlAttributeValue);

    final PsiReference[] references = xmlAttributeValue.getReferences();

    final Object[] variants = references[0].getVariants();
    assertEquals(10, variants.length);
  }

  public void testBeanAliases() throws Throwable {
    myFixture.testHighlighting("spring-beans-aliases.xml");
  }

  public void testBeanScopes() throws Throwable {
    myFixture.testHighlighting("spring-beans-scopes.xml");
  }

  public void testFactoryMethod() throws Throwable {
    myFixture.testHighlighting("spring-beans-factory-method.xml");
  }

  public void testBeanNameConventions() throws Throwable {
    myFixture.testHighlighting("spring-beans-name-convention.xml");
  }

  public void testProperties() throws Throwable {
    myFixture.testHighlighting("properties.xml");
  }

  public void testComplexProperties() throws Throwable {
    myFixture.testHighlighting("complexProperties.xml", "ComplexPropertiesInterface.java");
  }

  public void testParentArgs() throws Throwable {
    myFixture.testHighlighting(true, false, false, "parentArgs.xml");
  }

  public void testBeanRename() throws Throwable {
    myFixture.testRename("beanRename.xml", "beanRename_after.xml", "newName");
  }

  public void testBeanRenameByName() throws Throwable {
    myFixture.testRename("beanRenameByName.xml", "beanRenameByName_after.xml", "newName");
  }

  public void testValueStyle() throws Throwable {
    myFixture.testHighlighting("value-style.xml");
  }

  public void testPlaceholderValueInspection() throws Throwable {
    myFixture.testHighlighting(true, false, true, "placeholder-value-inspection.xml");
    //myFixture.testHighlighting("placeholder-value-inspection2.xml");
  }

  public void testCreateBeanQuickFix() throws Throwable {
    final Collection<IntentionAction> actions = myFixture.getAvailableIntentions("createBeanQuickFix.xml");
    for (IntentionAction action : actions) {
      if (action.getText().startsWith("Create new")) {
        myFixture.launchAction(action);
        break;
      }
    }
    myFixture.checkResultByFile("createBeanQuickFix_after.xml");
  }

  public void testCompiledProperty() throws Throwable {
    final List<IntentionAction> list = myFixture.getAvailableIntentions("compiledProperty.xml");
    for (IntentionAction intentionAction : list) {
      if (intentionAction.getFamilyName().equals(SpringBundle.message("model.bean.quickfix.family"))) {
        assertTrue(false);
      }
    }
  }

  public void testInnerBeanClass() throws Throwable {
    myFixture.testHighlighting("innerBeanClass.xml");
  }

  public void testRefArrayBean() throws Throwable {
    myFixture.testHighlighting("refArrayBean.xml");
  }


  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }

}