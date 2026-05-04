/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.intellij.spring.model.xml.custom;

import consulo.ide.impl.idea.openapi.application.PathManager;
import consulo.ide.impl.idea.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.impl.RenameableFakePsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.CustomBeanInfo;
import com.intellij.spring.CustomBeanRegistry;
import com.intellij.spring.metadata.SpringBeanMetaData;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.CustomBean;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.custom.handler.*;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import java.util.function.Consumer;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.DomManager;

import java.io.File;
import java.util.List;

/**
 * @author peter
 */
public class CustomNamespaceHandlerTest extends SpringHighlightingTestCase<JavaModuleFixtureBuilder> {

  protected void setUp() throws Exception {
    super.setUp();
    myFixture.addClass("package org.springframework.beans.factory; interface FactoryBean {" +
                       "Class getObjectType();" +
                       "}");
  }

  @Override
  protected void runTest() throws Throwable {
    if (IdeaTestUtil.COVERAGE_ENABLED_BUILD) return;

    super.runTest();
  }

  protected void configureModule(JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    if (!"testNoSpringLib".equals(getName())) {
      addSpringCoreJars(moduleBuilder);
    }

    final String homePath = PathManager.getHomePath().replace(File.separatorChar, '/');
    moduleBuilder.addLibraryJars("logging", homePath + "/lib/dev/", "commons-logging.jar");

    String path = "/" + StringUtil.getPackageName(TestNamespaceHandler.class.getName()).replace('.', '/');
    final String pathToClass = new File(TestNamespaceHandler.class.getResource(".").toURI()).getAbsolutePath().replace('\\', '/');
    assert pathToClass.endsWith(path) : path + "; " + pathToClass;
    if (!"testNoNamespaceHandler".equals(getName())) {
      moduleBuilder.addLibrary("testHandler", pathToClass.substring(0, pathToClass.length() - path.length()));
    }
    moduleBuilder.addLibrary("handlerXsd", homePath + "/svnPlugins/spring/spring-tests/tests" + path);
  }

  public void testParseStringBean() throws Throwable {
    final CommonSpringBean bean = parse("<stringBean xmlns=\"foo\" id=\"b1\"/>").getCustomBeans().get(0);
    assertEquals("b1", bean.getBeanName());
    assertEquals(CommonClassNames.JAVA_LANG_STRING, bean.getBeanClass().getQualifiedName());
  }

  public void testNoSpringLib() throws Throwable {
    final String trace =
        CustomBeanRegistry.getInstance(myProject).parseBean(createTag("<stringBean xmlns=\"foo\" id=\"b1\"/>")).getStackTrace();
    assertEquals("java.lang.NoClassDefFoundError: org/springframework/beans/factory/support/BeanDefinitionRegistry", trace);
  }

  public void testNoNamespaceHandler() throws Throwable {
    final String trace =
        CustomBeanRegistry.getInstance(myProject).parseBean(createTag("<stringBean xmlns=\"foo\" id=\"b1\"/>")).getStackTrace();
    assertNotNull(trace);
    final String expectedStackTrace =
      "java.lang.ClassNotFoundException: com.intellij.spring.model.xml.custom.handler.TestNamespaceHandler\n" +
      "\tat java.net.URLClassLoader$1.run(URLClassLoader.java:200)\n" +
      "\tat java.security.AccessController.doPrivileged(Native Method)\n" +
      "\tat java.net.URLClassLoader.findClass(URLClassLoader.java:188)\n" +
      "\tat java.lang.ClassLoader.loadClass(ClassLoader.java:306)\n" +
      "\tat java.lang.ClassLoader.loadClass(ClassLoader.java:251)\n" +
      "\tat org.springframework.util.ClassUtils.forName(ClassUtils.java:242)\n" +
      "\tat org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver.resolve(DefaultNamespaceHandlerResolver.java:123)\n" +
      "\tat org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.parseCustomElement(BeanDefinitionParserDelegate.java:1250)\n" +
      "\tat org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.parseCustomElement(BeanDefinitionParserDelegate.java:1245)\n" +
      "\tat org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.parseBeanDefinitions(DefaultBeanDefinitionDocumentReader.java:141)\n" +
      "\tat org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.registerBeanDefinitions(DefaultBeanDefinitionDocumentReader.java:92)\n" +
      "\tat org.springframework.beans.factory.xml.XmlBeanDefinitionReader.registerBeanDefinitions(XmlBeanDefinitionReader.java:507)\n" +
      "\tat org.springframework.beans.factory.xml.XmlBeanDefinitionReader.doLoadBeanDefinitions(XmlBeanDefinitionReader.java:398)\n" +
      "\tat org.springframework.beans.factory.xml.XmlBeanDefinitionReader.loadBeanDefinitions(XmlBeanDefinitionReader.java:342)\n" +
      "\tat org.springframework.beans.factory.xml.XmlBeanDefinitionReader.loadBeanDefinitions(XmlBeanDefinitionReader.java:310)";
    final String[] expectedLines = expectedStackTrace.split("\n");
    final String[] actualLines = trace.split("\n");
    for (int i = 0; i < expectedLines.length; i++) {
      String s = expectedLines[i];
      final int j = s.indexOf("(");
      String truncated = j >= 0 ? s.substring(0, j) : s;
      assertTrue("Actual:\n" + trace, actualLines[i].startsWith(truncated));
    }
  }

  public void testFilterOutInfrastructureBeans() throws Throwable {
    final CommonSpringBean bean = assertOneElement(parse("<withInfrastructure xmlns=\"foo\" id=\"b1\"/>").getCustomBeans());
    assertEquals("b1", bean.getBeanName());
    assertEquals(CommonClassNames.JAVA_LANG_STRING, bean.getBeanClass().getQualifiedName());
  }

  public void testOnlyInfrastructureBeans() throws Throwable {
    final XmlTag tag = createTag("<onlyInfrastructure xmlns=\"foo\" id=\"b1\"/>");
    final CustomBeanRegistry.ParseResult result = CustomBeanRegistry.getInstance(myProject).parseBean(tag);
    assertEmpty(result.getBeans());
    assertNull(result.getErrorMessage());
    assertNull(result.getStackTrace());
    assertTrue(result.hasInfrastructureBeans());
  }

  public void testParseUtilMap() throws Throwable {
    final CommonSpringBean bean = parse("<map xmlns=\"util\" id=\"mymap\"/>").getCustomBeans().get(0);
    assertEquals("mymap", bean.getBeanName());
    assertEquals("org.springframework.beans.factory.config.MapFactoryBean", bean.getBeanClass().getQualifiedName());
    assertEquals(CommonClassNames.JAVA_UTIL_MAP, assertOneElement(SpringUtils.getEffectiveBeanTypes(bean)).getQualifiedName());
  }

  public void testDontWaitTooLong() throws Throwable {
    System.setProperty(CustomBeanRegistry.CUSTOM_SPRING_BEANS_PARSING_TIMEOUT, "2000");

    try {
      final CustomBeanWrapper bean = parse("<eternity xmlns=\"foo\"/>");
      assertNull(bean.getBeanClass(true));
      assertNull(bean.getBeanClass(false));
      assertNull(bean.getBeanName());
      assertEmpty(bean.getCustomBeans());
    }
    finally {
      System.clearProperty(CustomBeanRegistry.CUSTOM_SPRING_BEANS_PARSING_TIMEOUT);
    }
  }

  public void testTagMapping() throws Throwable {
    final XmlTag tag = createTag("<outer xmlns=\"foo\"><inner/></outer>");
    final List<CustomBean> list = parse(tag).getCustomBeans();
    assertOrderedCollection(list, new Consumer<CustomBean>() {
      public void consume(final CustomBean bean) {
        assertEquals("outer", bean.getBeanName());
        assertEquals(tag, bean.getXmlTag());
      }
    }, new Consumer<CustomBean>() {
      public void consume(final CustomBean commonSpringBean) {
        assertEquals("inner", commonSpringBean.getBeanName());
        assertEquals(tag.getSubTags()[0], commonSpringBean.getXmlTag());
      }
    }, new Consumer<CustomBean>() {
      public void consume(final CustomBean commonSpringBean) {
        assertEquals("unmappedInner", commonSpringBean.getBeanName());
        assertEquals(tag, commonSpringBean.getXmlTag());
      }
    });
  }

  public void testContextNamespaces() throws Throwable {
    final XmlTag tag = createTag("<beans xmlns=\"http://www.springframework.org/schema/beans\" xmlns:foo=\"foo\"><foo:stringBean id=\"x\"/></beans>");
    final CommonSpringBean bean = parse(tag.getSubTags()[0]).getCustomBeans().get(0);
    assertEquals("x", bean.getBeanName());
    assertEquals(CommonClassNames.JAVA_LANG_STRING, bean.getBeanClass().getQualifiedName());
  }

  public void testContextNamespaces2() throws Throwable {
    final XmlTag tag = createTag("<beans:beans xmlns:beans=\"http://www.springframework.org/schema/beans\" xmlns=\"foo\"><stringBean id=\"x\"/></beans:beans>");
    final CommonSpringBean bean = parse(tag.getSubTags()[0]).getCustomBeans().get(0);
    assertEquals("x", bean.getBeanName());
    assertEquals(CommonClassNames.JAVA_LANG_STRING, bean.getBeanClass().getQualifiedName());
  }

  public void testWithoutId() throws Throwable {
    final CommonSpringBean bean = parse("<stringBean xmlns=\"foo\"/>").getCustomBeans().get(0);
    assertNull(bean.getBeanName());
    assertEquals(CommonClassNames.JAVA_LANG_STRING, bean.getBeanClass().getQualifiedName());
  }

  public void testFactoryBean15() throws Throwable {
    final CommonSpringBean bean = parse("<factoryBean15 xmlns=\"foo\" id=\"foo\"/>").getCustomBeans().get(0);
    assertEquals(TestFactoryBean.class.getName(), bean.getBeanClass(true).getQualifiedName());
    assertEquals(CommonClassNames.JAVA_LANG_STRING,  assertOneElement(SpringUtils.getEffectiveBeanTypes(bean)).getQualifiedName());
  }

  public void testFactoryBeanGenericMethod() throws Throwable {
    final CommonSpringBean bean = parse("<genericMethod xmlns=\"foo\" id=\"foo\"/>").getCustomBeans().get(0);
    assertEquals(GenericFactoryBean.class.getName(), bean.getBeanClass(true).getQualifiedName());
    assertEquals(CommonClassNames.JAVA_LANG_STRING,  assertOneElement(SpringUtils.getEffectiveBeanTypes(bean)).getQualifiedName());
  }

  public void testAbstractFactoryBean() throws Throwable {
    final CommonSpringBean bean = parse("<concreteFactoryBean xmlns=\"foo\" id=\"foo\"/>").getCustomBeans().get(0);
    assertEquals(ConcreteFactoryBean.class.getName(), bean.getBeanClass(true).getQualifiedName());
    assertEquals(CommonClassNames.JAVA_LANG_STRING,  assertOneElement(SpringUtils.getEffectiveBeanTypes(bean)).getQualifiedName());
  }

  public void testSeveralFactoryMethods() throws Throwable {
    CommonSpringBean bean = parse("<factoryMethodFoo xmlns=\"foo\" id=\"foo\"/>").getCustomBeans().get(0);
    assertEquals(CommonClassNames.JAVA_LANG_STRING, bean.getBeanClass().getQualifiedName());
    assertEquals(TestBeanWithFactoryMethod.class.getName(), bean.getBeanClass(false).getQualifiedName());

    bean = parse("<factoryMethodBar xmlns=\"foo\" id=\"foo\"/>").getCustomBeans().get(0);
    assertEquals(TestBeanWithFactoryMethod.class.getName(), bean.getBeanClass(false).getQualifiedName());
    assertEquals(TestBeanWithFactoryMethod.class.getName(), bean.getBeanClass().getQualifiedName());
  }

  public void testFactoryMethodInAnotherBean() throws Throwable {
    CommonSpringBean bean = parse("<factoryMethodInAnotherBean xmlns=\"foo\" id=\"foo\"/>").getCustomBeans().get(0);
    assertEquals(TestBeanWithFactoryMethod.class.getName(), bean.getBeanClass(false).getQualifiedName());
    assertEquals(TestBeanWithFactoryMethod.class.getName(), bean.getBeanClass().getQualifiedName());
    assertInstanceOf(bean.getIdentifyingPsiElement(), RenameableFakePsiElement.class);
  }

  public void testBeanPolicies() throws Throwable {
    final CustomBeanInfo info = new CustomBeanInfo();
    info.idAttribute = "idd";
    info.beanClassName = CommonClassNames.JAVA_LANG_INTEGER;
    CustomBeanRegistry.getInstance(myProject).addBeanPolicy("foo", "policyBean", info);

    final XmlTag tag = createTag("<policyBean idd=\"239\" xmlns=\"foo\"/>");
    final CustomBean bean = getWrapper(tag).getCustomBeans().get(0);
    assertEquals("239", bean.getBeanName());
    assertEquals(tag.getAttribute("idd"), bean.getIdAttribute());
    assertEquals(CommonClassNames.JAVA_LANG_INTEGER, bean.getBeanClass().getQualifiedName());
  }

  public void testGetExceptionStackTrace() throws Throwable {
    final XmlTag tag = createTag("<exception xmlns=\"foo\"/>");
    final String stackTrace = CustomBeanRegistry.getInstance(myProject).parseBean(tag).getStackTrace();
    assertEquals("java.lang.UnsupportedOperationException: Method parse is not yet implemented in com.intellij.spring.model.xml.custom.handler.TestNamespaceHandler$3\n" +
                 "\tat com.intellij.spring.model.xml.custom.handler.TestNamespaceHandler$3.parse(TestNamespaceHandler.java:47)\n" +
                 "\tat org.springframework.beans.factory.xml.NamespaceHandlerSupport.parse(NamespaceHandlerSupport.java:69)\n" +
                 "\tat org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.parseCustomElement(BeanDefinitionParserDelegate.java:1255)\n" +
                 "\tat org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.parseCustomElement(BeanDefinitionParserDelegate.java:1245)\n" +
                 "\tat org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.parseBeanDefinitions(DefaultBeanDefinitionDocumentReader.java:141)\n" +
                 "\tat org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader.registerBeanDefinitions(DefaultBeanDefinitionDocumentReader.java:92)\n" +
                 "\tat org.springframework.beans.factory.xml.XmlBeanDefinitionReader.registerBeanDefinitions(XmlBeanDefinitionReader.java:507)\n" +
                 "\tat org.springframework.beans.factory.xml.XmlBeanDefinitionReader.doLoadBeanDefinitions(XmlBeanDefinitionReader.java:398)\n" +
                 "\tat org.springframework.beans.factory.xml.XmlBeanDefinitionReader.loadBeanDefinitions(XmlBeanDefinitionReader.java:342)\n" +
                 "\tat org.springframework.beans.factory.xml.XmlBeanDefinitionReader.loadBeanDefinitions(XmlBeanDefinitionReader.java:310)",
                 stackTrace);
  }

  public void testHardcodedName() throws Throwable {
    CommonSpringBean bean = parse("<hardCoded xmlns=\"foo\"/>").getCustomBeans().get(0);
    assertEquals("hardCoded", bean.getBeanName());
    assertFalse(bean.getXmlTag().getMetaData() instanceof SpringBeanMetaData);
  }

  private CustomBeanWrapper parse(final String xml) throws IncorrectOperationException {
    return parse(createTag(xml));
  }

  private CustomBeanWrapper parse(final XmlTag tag) {
    CustomBeanRegistry.getInstance(myProject).parseBean(tag);
    return getWrapper(tag);
  }

  private CustomBeanWrapper getWrapper(final XmlTag tag) {
    final DomManager domManager = DomManager.getDomManager(myProject);
    final XmlFile file = (XmlFile)tag.getContainingFile();
    if (tag.getParentTag() != null) {
      return domManager.getFileElement(file, Beans.class, tag.getLocalName()).getRootElement().getCustomBeans().get(0);
    }
    return domManager.getFileElement(file, CustomBeanWrapper.class, tag.getLocalName()).getRootElement();
  }

  private XmlTag createTag(final String text) throws IncorrectOperationException {
    final XmlTag tag = XmlElementFactory.getInstance(myProject).createTagFromText(text);
    tag.getContainingFile().putUserData(ModuleUtil.KEY_MODULE, myModule);
    return tag;
  }
}
