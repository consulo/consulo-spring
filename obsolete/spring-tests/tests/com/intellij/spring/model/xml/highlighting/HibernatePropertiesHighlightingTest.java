package com.intellij.spring.model.xml.highlighting;

import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.spring.model.xml.beans.Prop;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringProperty;
import com.intellij.spring.model.xml.beans.SpringPropertyDefinition;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class HibernatePropertiesHighlightingTest extends SpringHighlightingTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    PsiTestUtil.addLibrary(myModuleTestFixture.getModule(), "hb3", PathManager.getHomePath() + "/lib/", "hibernate3.jar");
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    addSpringJar(moduleBuilder);
  }

  public void testPropertyValues() throws Throwable {
    myFixture.testHighlighting("hibernate-properties-resolving.xml");

  }

  public void testPropertyValueCompletion() throws Throwable {
    myFixture.testCompletion("hibernate-properties.xml", "hibernate-properties-after.xml");

  }
  public void testPropertyNameCompletion() throws Throwable {
    myFixture.testCompletion("hibernate-properties-names.xml", "hibernate-properties-names-after.xml");
  }

  public void testPropertiesNames() throws Throwable {
    final SpringBean springBean = getBeanFromFile("hibernate-properties-resolving.xml", "sessionFactory");
    final SpringPropertyDefinition propertyByName = SpringUtils.findPropertyByName(springBean, "hibernateProperties");
    assertNotNull(propertyByName);

    final Prop prop = ((SpringProperty)propertyByName).getProps().getProps().get(0);

    XmlAttributeValue xmlAttributeValue = prop.getKey().getXmlAttributeValue();
    assert xmlAttributeValue != null;
    PsiReference[] references = xmlAttributeValue.getReferences();

    assertTrue(references[0].getVariants().length > 0);
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }

}