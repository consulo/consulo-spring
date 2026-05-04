/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.custom;

import com.intellij.javaee.ExternalResourceManager;
import consulo.ide.impl.idea.openapi.application.PathManager;
import consulo.ide.impl.idea.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.HeavySpringTestCase;
import com.intellij.spring.model.xml.beans.MetadataPropertyValueConverter;
import com.intellij.spring.model.xml.beans.MetadataRefValue;
import com.intellij.spring.model.xml.beans.MetadataValue;
import com.intellij.spring.model.xml.custom.handler.TestNamespaceHandler;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.*;
import com.intellij.util.xml.reflect.DomAttributeChildDescription;
import org.jetbrains.annotations.NotNull;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URISyntaxException;

/**
 * @author peter
 */
public class CustomNamespaceMetadataTest extends HeavySpringTestCase {
  public CustomNamespaceMetadataTest() {
    super(false);
  }

  protected void configureModule(JavaModuleFixtureBuilder moduleBuilder) throws URISyntaxException {
    final String homePath = PathManager.getHomePath().replace(File.separatorChar, '/');
    moduleBuilder.addLibraryJars("spring", homePath + super.getBasePath(), "spring2.jar");

    String path = "/" + StringUtil.getPackageName(TestNamespaceHandler.class.getName()).replace('.', '/');
    moduleBuilder.addLibrary("handlers", new File(getClass().getResource(path).toURI()).getAbsoluteFile().getAbsolutePath());
    moduleBuilder.addLibrary("handlers2", homePath + "/svnPlugins/spring/spring-tests/tests" + path);
  }

  protected void setUp() throws Exception {
    super.setUp();
    final String homePath = PathManager.getHomePath().replace(File.separatorChar, '/');
    String path = "/" + StringUtil.getPackageName(TestNamespaceHandler.class.getName()).replace('.', '/');
    ExternalResourceManager.getInstance().addResource("foo", homePath + "/svnPlugins/spring/spring-tests/tests" + path + "/test.xsd");
  }

  protected void tearDown() throws Exception {
    ExternalResourceManager.getInstance().removeResource("foo");
    super.tearDown();
  }

  public void testBeanRef() throws Throwable {
    final CustomBeanWrapper wrapper = parse("<annotated xmlns=\"foo\" transaction-manager=\"239\"/>");
    final DomAttributeChildDescription<?> description = wrapper.getGenericInfo().getAttributeChildDescription("transaction-manager");
    assertNotNull(description);
    assertEquals(MetadataRefValue.class, description.getType());
    final Convert convert = description.getAnnotation(Convert.class);
    assertNotNull(convert);
    assertEquals("com.intellij.util.xml.impl.ConvertAnnotationImpl", convert.getClass().getName());
    assertFalse(convert.soft());
    final SpringBeanResolveConverter converter =
      assertInstanceOf(convert.getClass().getMethod("getConverter").invoke(convert), SpringBeanResolveConverter.class);
    assertEquals("org.springframework.transaction.PlatformTransactionManager", assertOneElement(converter.getRequiredClasses(new AbstractConvertContext() {
      @NotNull
      public DomElement getInvocationElement() {
        return description.getDomAttributeValue(wrapper);
      }
    })).getCanonicalText());
  }

  public void testAnyBeanRef() throws Throwable {
    final CustomBeanWrapper wrapper = parse("<annotated xmlns=\"foo\" any-bean=\"239\"/>");
    final DomAttributeChildDescription<?> description = wrapper.getGenericInfo().getAttributeChildDescription("any-bean");
    assertNotNull(description);
    assertEquals(MetadataRefValue.class, description.getType());
    final Convert convert = description.getAnnotation(Convert.class);
    assertNotNull(convert);
    assertEquals("com.intellij.util.xml.impl.ConvertAnnotationImpl", convert.getClass().getName());
    assertFalse(convert.soft());
    final SpringBeanResolveConverter converter =
      assertInstanceOf(convert.getClass().getMethod("getConverter").invoke(convert), SpringBeanResolveConverter.class);
    assertEquals(CommonClassNames.JAVA_LANG_OBJECT, assertOneElement(converter.getRequiredClasses(new AbstractConvertContext() {
      @NotNull
      public DomElement getInvocationElement() {
        return description.getDomAttributeValue(wrapper);
      }
    })).getCanonicalText());
  }

  public void testListBeanRef() throws Throwable {
    final CustomBeanWrapper wrapper = parse("<annotated xmlns=\"foo\" list-bean=\"239\"/>");
    final DomAttributeChildDescription<?> description = wrapper.getGenericInfo().getAttributeChildDescription("list-bean");
    assertNotNull(description);
    assertEquals(MetadataRefValue.class, description.getType());
    final Convert convert = description.getAnnotation(Convert.class);
    assertNotNull(convert);
    assertEquals("com.intellij.util.xml.impl.ConvertAnnotationImpl", convert.getClass().getName());
    assertFalse(convert.soft());
    final SpringBeanResolveConverter converter =
      assertInstanceOf(convert.getClass().getMethod("getConverter").invoke(convert), SpringBeanResolveConverter.class);
    assertEquals(CommonClassNames.JAVA_UTIL_LIST, assertOneElement(converter.getRequiredClasses(new AbstractConvertContext() {
      @NotNull
      public DomElement getInvocationElement() {
        return description.getDomAttributeValue(wrapper);
      }
    })).getCanonicalText());
  }

  public void testBoolean() throws Throwable {
    final CustomBeanWrapper wrapper = parse("<annotated xmlns=\"foo\" bool=\"true\"/>");
    final DomAttributeChildDescription<?> description = wrapper.getGenericInfo().getAttributeChildDescription("bool");
    assertNotNull(description);
    assertEquals(MetadataValue.class, description.getType());
    final Convert convert = description.getAnnotation(Convert.class);
    assertNotNull(convert);
    assertEquals("com.intellij.util.xml.impl.ConvertAnnotationImpl", convert.getClass().getName());
    assertFalse(convert.soft());
    final MetadataPropertyValueConverter converter =
      assertInstanceOf(convert.getClass().getMethod("getConverter").invoke(convert), MetadataPropertyValueConverter.class);
    assertEquals(CommonClassNames.JAVA_LANG_BOOLEAN, converter.getRequiredType().getCanonicalText());
  }
  
  public void testClass() throws Throwable {
    final CustomBeanWrapper wrapper = parse("<annotated xmlns=\"foo\" clazz=\"a\"/>");
    final DomAttributeChildDescription<?> description = wrapper.getGenericInfo().getAttributeChildDescription("clazz");
    assertNotNull(description);
    assertEquals(ParameterizedTypeImpl.make(GenericAttributeValue.class, new Type[]{PsiClass.class}, null), description.getType());
    assertNull(description.getAnnotation(Convert.class));

    final ExtendClass extendClass = description.getAnnotation(ExtendClass.class);
    assertNotNull(extendClass);
    assertEquals(CommonClassNames.JAVA_UTIL_LIST, extendClass.value());
    assertFalse(extendClass.instantiatable());
    assertTrue(extendClass.allowAbstract());
    assertTrue(extendClass.allowInterface());
    assertFalse(extendClass.allowEmpty());
    assertFalse(extendClass.canBeDecorator());
  }

  private CustomBeanWrapper parse(final String xml) throws IncorrectOperationException {
    return parse(createTag(xml));
  }

  private CustomBeanWrapper parse(final XmlTag tag) {
    final DomManager domManager = DomManager.getDomManager(myProject);
    final XmlFile file = (XmlFile)tag.getContainingFile();
    return domManager.getFileElement(file, CustomBeanWrapper.class, tag.getLocalName()).getRootElement();
  }

  private XmlTag createTag(final String text) throws IncorrectOperationException {
    final XmlTag tag = XmlElementFactory.getInstance(myProject).createTagFromText(text);
    tag.getContainingFile().putUserData(ModuleUtil.KEY_MODULE, myModule);
    return tag;
  }
}