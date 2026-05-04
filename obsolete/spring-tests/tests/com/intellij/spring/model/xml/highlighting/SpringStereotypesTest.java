package com.intellij.spring.model.xml.highlighting;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.spring.model.xml.beans.JamSpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import java.util.function.Consumer;
import java.util.function.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

/**
 * User: Sergey.Vasiliev
 */
public class SpringStereotypesTest extends SpringHighlightingTestCase<JavaModuleFixtureBuilder> {

  protected boolean isWithTestSources() {
    return false;
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);

    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
    addSpring_2_5_Library(moduleBuilder);
    moduleBuilder
        .addLibraryJars("javaee", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(), "javaee.jar");
    moduleBuilder
        .addLibraryJars("javaee", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(), "spring-context-2.5.5.jar");
  }

  public void testStereotypesModel() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "stereotipes.xml");

    myFixture.copyFileToProject("components/FooClass.java");
    myFixture.copyFileToProject("components/Genre.java");
    myFixture.copyFileToProject("components/ChildGenre.java");

    final String[] strings = {"fooService1", "fooService2", "fooServiceGenre", "fooService4", "fooService5", "fooService6",
        "fooComponent1", "fooComponent2", "fooRepositoryGenre", "fooRepository2", "fooController1", "fooController2"};

    assertUnorderedCollection(getNames(), getConsumers(strings));
  }

  public void testCustomComponentsModel() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "stereotipes.xml");

    myFixture.copyFileToProject("components/CustomAnnotationComponents.java");
    myFixture.copyFileToProject("components/Genre.java");
    myFixture.copyFileToProject("components/ChildGenre.java");
    myFixture.copyFileToProject("components/CustomComponentAnnotation.java");
    myFixture.copyFileToProject("components/CustomComponentAnnotationChild.java");

    final String[] strings = {"fooComponent1",
        "fooComponent_2",
        "fooComponent3",
        "fooComponent4",
        "fooComponent5",
        "fooComponent6",
        "fooComponent_7"};

    assertUnorderedCollection(getNames(), getConsumers(strings));
  }

  public void testBasePackage() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "basePackageTest.xml");
    myFixture.copyFileToProject("components/FooClass.java");
    myFixture.copyFileToProject("foo/bar/MyComponent.java");
    myFixture.testHighlighting("basePackageTest.xml");
    assertEquals(13, getNames().size());
  }

  public void testBasePackage2() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "basePackageTest2.xml");
    myFixture.copyFileToProject("components/FooClass.java");
    myFixture.copyFileToProject("foo/bar/MyComponent.java");
    myFixture.copyFileToProject("foo/bars/MyComponent2.java");
    myFixture.testHighlighting("basePackageTest2.xml");
    assertEquals(1, getNames().size());
  }

  public void testBasePackage3() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "basePackageTest3.xml");
    myFixture.copyFileToProject("components/FooClass.java");
    myFixture.copyFileToProject("foo/bar/MyComponent.java");
    myFixture.testHighlighting("basePackageTest3.xml");
    assertEquals(0, getNames().size());
  }

  public void testBasePackage4() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "basePackageTest4.xml");
    myFixture.copyFileToProject("components/FooClass.java");
    myFixture.copyFileToProject("foo/bar/MyComponent.java");
    myFixture.copyFileToProject("foo/bars/MyComponent2.java");
    myFixture.testHighlighting("basePackageTest4.xml");
    assertEquals(2, getNames().size());
  }

  public void testBasePackageWildcard() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "basePackageWildcard.xml");
    myFixture.copyFileToProject("components/FooClass.java");
    myFixture.copyFileToProject("foo/bar/MyComponent.java");
    myFixture.copyFileToProject("foo/bars/MyComponent2.java");
    myFixture.testHighlighting("basePackageWildcard.xml");
    assertEquals(2, getNames().size());
  }

  public void testParentRef() throws Throwable {
    final SpringFileSet parent = configureFileSet("parent", myModule);
    addFileToSet(parent, "parent.xml");

    final SpringFileSet child = configureFileSet("child", myModule);
    addFileToSet(child, "child.xml");
    child.addDependency("parent");

    myFixture.copyFileToProject("foo/bar/MyComponent.java");
    myFixture.testCompletion("child.xml", "child_after.xml");
  }

  public void testAutowiredMethod() throws Throwable {
    final SpringFileSet set = configureFileSet();
    addFileToSet(set, "autowired.xml");
    myFixture.testHighlighting(true, false, true, "components/AutowiredMethod.java");
  }

  public void testAutowiredMethodGutter() throws Throwable {
    final SpringFileSet set = configureFileSet();
    addFileToSet(set, "autowired.xml");
    final GutterIconRenderer iconRenderer = myFixture.findGutter("components/AutowiredMethod.java");
    assertNotNull(iconRenderer);
    final AnAction anAction = iconRenderer.getClickAction();
    assertNotNull(anAction);
  }

  public void testAutowiredField() throws Throwable {
    final SpringFileSet set = configureFileSet();
    addFileToSet(set, "autowired.xml");
    myFixture.testHighlighting(true, false, true, "components/AutowiredField.java");
  }

  public void testAutowiredFieldGutter() throws Throwable {
    final SpringFileSet set = configureFileSet();
    addFileToSet(set, "autowired.xml");
    final GutterIconRenderer iconRenderer = myFixture.findGutter("components/AutowiredField.java");
    assertNotNull(iconRenderer);
    final AnAction anAction = iconRenderer.getClickAction();
    assertNotNull(anAction);
  }

  public void testResourceAutowiredField() throws Throwable {
    final SpringFileSet set = configureFileSet();
    addFileToSet(set, "autowired.xml");
    myFixture.testHighlighting(true, false, true, "components/ResourceAutowiredField.java");
  }

  public void testResourceAutowiredMethod() throws Throwable {
    final SpringFileSet set = configureFileSet();
    addFileToSet(set, "autowired.xml");
    myFixture.testHighlighting(true, false, true, "components/ResourceAutowiredMethod.java");
  }


  public void testUnusedSymbols() throws Throwable {
    final SpringFileSet fileSet = configureFileSet();
    addFileToSet(fileSet, "unused-symbols.xml");
    myFixture.testHighlighting(true, false, true, "UnusedSymbols.java");
  }

  private Consumer<String>[] getConsumers(final String[] strings) {
    final List<Consumer<String>> list = ContainerUtil.map2List(strings, new Function<String, Consumer<String>>() {
      public Consumer<String> fun(final String str) {
        return new Consumer<String>() {
          public void consume(final String s) {
            assertEquals(str, s);
          }
        };
      }
    });

    return list.toArray(new Consumer[list.size()]);
  }

  private List<String> getNames() {
    final SpringModel model = SpringManager.getInstance(myProject).getAllModels(myModule).get(0);
    List<String> names = new ArrayList<String>();
    for (SpringBaseBeanPointer pointer : model.getAllCommonBeans()) {
      if (pointer instanceof JamSpringBeanPointer) {
        names.add(pointer.getName());
      }
    }
    return names;
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/stereotypes";
  }
}
