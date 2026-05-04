/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

public class SpringBeanValueEditorsTest extends SpringHighlightingTestCase {
  private static final String[] springJarTestNames = new String[]{"testResources", "testPlaceholderProperties", "testPlaceholderMapKeyCompletion",
    "testTypedValues", "testPlaceholderUtilsProperties", "testPlaceholderProperties_IDEADEV_20991", "testContextPlaceholder", "testPlaceholderSimpleLocations"};

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);

    for (String springJarTestName : springJarTestNames) {
      if (springJarTestName.equals(getName())) {
        addSpringJar(moduleBuilder);
        break;
      }
    }
  }

  public void testBooleanValueConverter() throws Throwable {
    myFixture.testHighlighting("value-converters-boolean.xml");
  }

  public void testEmptyNumbers() throws Throwable {
    myFixture.testHighlighting("value-converters-empty-numbers.xml");
  }

  public void testNumbers() throws Throwable {
    myFixture.testHighlighting("value-converters-numbers.xml");
  }

  public void testClass() throws Throwable {
    myFixture.testHighlighting("testClass.xml");
  }

  public void testResources() throws Throwable {
    myFixture.testHighlighting("foo/testResources.xml");
  }

  public void testPlaceholderProperties() throws Throwable {
    myFixture.testHighlighting(false, false, false, "placeholders.xml");
  }

  public void testPlaceholderProperties_IDEADEV_20991() throws Throwable {
    SpringFileSet parentFileset = configureFileSet("parent", myModule);
    SpringFileSet childFileset = configureFileSet("child", myModule);

    childFileset.addDependency(parentFileset.getId());

    addFileToSet(parentFileset, "placeholders_parent.xml");
    addFileToSet(childFileset, "placeholders_child.xml");

    myFixture.testHighlighting(false, false, false, "placeholders_child.xml");
  }

  public void testPlaceholderUtilsProperties() throws Throwable {
    myFixture.testHighlighting(false, false, false, "placeholders_util_shema.xml");
    myFixture.testHighlighting(false, false, false, "placeholders_util_shema2.xml");
    myFixture.testHighlighting(false, false, false, "placeholders_util_shema3.xml");
  }

  public void testContextPlaceholder() throws Throwable {
    myFixture.testHighlighting(false, false, false, "placeholders_context_schema.xml");
  }

  public void testPlaceholderSimpleLocations() throws Throwable {
    myFixture.testHighlighting(false, false, false, "placeholders_simple_locations.xml");
  }

  public void testPlaceholderMapKeyCompletion() throws Throwable {
    myFixture.testCompletion("placeholder_key_map.xml", "placeholder_key_map_after.xml");
  }

  public void testConstructorValues() throws Throwable {
    myFixture.testHighlighting("constructorValues.xml");
  }

  public void testEnumCompletion() throws Throwable {
    myFixture.testCompletion("enum_properties.xml", "enum_properties_after.xml");
    myFixture.testCompletion("enum_properties_map.xml", "enum_properties_map_after.xml");
    myFixture.testCompletion("enum_properties_list.xml", "enum_properties_list_after.xml");
    myFixture.testCompletion("enum_properties_class.xml", "enum_properties_class_after.xml");
  }

  public void testEnumHighlighting() throws Throwable {
    myFixture.testHighlighting("enum_properties_highlighting.xml");
  }

  public void testTypedValues() throws Throwable {
    myFixture.testHighlighting("typed-values.xml");
  }

  public void testClassArray() throws Throwable {
    myFixture.testHighlighting("class-array.xml");
  }

  public void testBooleanCompletion() throws Throwable {
    myFixture.testCompletion("boolean-completion.xml", "boolean-completion_after.xml");
  }

  public void testOverloadedSetters() throws Throwable {
    myFixture.testHighlighting("overloaded-setters.xml");
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/values/";
  }
}
