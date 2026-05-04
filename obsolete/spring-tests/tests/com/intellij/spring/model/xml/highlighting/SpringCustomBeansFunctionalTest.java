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
package com.intellij.spring.model.xml.highlighting;

import com.intellij.codeInsight.intention.IntentionAction;
import consulo.ide.impl.idea.codeInsight.navigation.actions.GotoTypeDeclarationAction;
import com.intellij.javaee.ExternalResourceManager;
import com.intellij.javaee.ExternalResourceManagerEx;
import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.CustomBeanRegistry;
import com.intellij.spring.model.highlighting.InjectionValueStyleInspection;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.custom.handler.TestNamespaceHandler;
import com.intellij.testFramework.ExpectedHighlightingData;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementVisitor;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NonNls;

import java.io.File;

/**
 * @author peter
 */
public class SpringCustomBeansFunctionalTest extends SpringHighlightingTestCase<JavaModuleFixtureBuilder> {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    final String homePath = PathManager.getHomePath().replace(File.separatorChar, '/');
    String path = "/" + StringUtil.getPackageName(TestNamespaceHandler.class.getName()).replace('.', '/');
    ExternalResourceManager.getInstance().addResource("foo", homePath + "/svnPlugins/spring/spring-tests/tests" + path + "/test.xsd");
  }

  @Override
  protected void tearDown() throws Exception {
    ExternalResourceManagerEx.getInstanceEx().removeResource("foo");
    super.tearDown();
  }

  @Override
  protected void runTest() throws Throwable {
    if (IdeaTestUtil.COVERAGE_ENABLED_BUILD) return;

    super.runTest();
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);

    final String homePath = PathManager.getHomePath().replace(File.separatorChar, '/');
    moduleBuilder.addLibraryJars("spring", homePath + super.getBasePath(), "spring-2.0.6.jar");
    moduleBuilder.addLibraryJars("logging", homePath + "/lib/dev/", "commons-logging.jar");

    String path = "/" + StringUtil.getPackageName(TestNamespaceHandler.class.getName()).replace('.', '/');
    final String pathToClass = new File(TestNamespaceHandler.class.getResource(".").toURI()).getAbsolutePath().replace('\\', '/');
    assert pathToClass.endsWith(path) : path + "; " + pathToClass;
    moduleBuilder.addLibrary("testHandler", pathToClass.substring(0, pathToClass.length() - path.length()));
    //moduleBuilder.addLibrary("handlerXsd", homePath + "/svnPlugins/spring/spring-tests/tests" + path);

    moduleBuilder.addContentRoot(homePath + "/svnPlugins/spring/spring-tests/tests" + path);
    moduleBuilder.addSourceRoot(homePath + "/svnPlugins/spring/spring-tests/tests" + path);
  }

  @Override
  protected boolean isWithTestSources() {
    return false;
  }

  public void testCustomBeansHighlighting() throws Throwable {
    final InjectionValueStyleInspection inspection = new InjectionValueStyleInspection();
    myFixture.disableInspections(inspection);
    myFixture.copyFileToProject("FooBean2.java");

    configureAndParseBeans("spring-custom-handlers.xml");
    myFixture.checkHighlighting(true, false, false);
  }

  private String configureAndParseBeans(final String filePath) throws Throwable {
    myFixture.copyFileToProject(filePath, "aa.xml");
    myFixture.configureByFile("aa.xml");

    final Document document = myFixture.getEditor().getDocument();
    final PsiFile file = myFixture.getFile();

    new WriteCommandAction(myProject) {
      protected void run(Result result) throws Throwable {
        new ExpectedHighlightingData(document, false, false, false, file);
      }
    }.execute();
    PsiDocumentManager.getInstance(myProject).commitDocument(document);

    final Beans beans = DomManager.getDomManager(myProject).getFileElement((XmlFile)file, Beans.class).getRootElement();
    beans.acceptChildren(new DomElementVisitor() {
      public void visitDomElement(final DomElement element) {
        final XmlTag tag = element.getXmlTag();
        if (tag == null) return;
        if (element instanceof CustomBeanWrapper) {
          CustomBeanRegistry.getInstance(myProject).parseBean(tag);
        }
        element.acceptChildren(this);
      }
    });
    myFixture.configureByFile(filePath);
    return filePath;
  }

  public void testToolHighlighting() throws Throwable {
    final InjectionValueStyleInspection inspection = new InjectionValueStyleInspection();
    myFixture.disableInspections(inspection);
    myFixture.copyFileToProject("FooBean2.java");

    configureAndParseBeans(getTestName(true) + ".xml");
    myFixture.checkHighlighting();
  }

  public void testRenameCustomBeanWithId() throws Throwable {
    configureAndParseBeans(getTestName(true) + ".xml");
    myFixture.testRename(getTestName(true) + "_after.xml", "newName");
  }

  public void testRenameCustomBeanWithIdFromUsage() throws Throwable {
    configureAndParseBeans(getTestName(true) + ".xml");
    myFixture.testRename(getTestName(true) + "_after.xml", "newName");
  }

  public void testRenameInnerCustomBeanWithNonDefaultIdAttribute() throws Throwable {
    configureAndParseBeans(getTestName(true) + ".xml");
    myFixture.testRename(getTestName(true) + "_after.xml", "newName");
  }

  public void testRenameInnerCustomBeanWithNonDefaultIdAttributeFromUsage() throws Throwable {
    configureAndParseBeans(getTestName(true) + ".xml");
    myFixture.testRename(getTestName(true) + "_after.xml", "newName");
  }

  public void testGotoTypeDeclaration() throws Throwable {
    myFixture.configureByFile(getTestName(false) + ".xml");
    final Editor editor = myFixture.getEditor();
    final PsiElement element = GotoTypeDeclarationAction.findSymbolType(editor, editor.getCaretModel().getOffset());
    assertNotNull(element);
    assertEquals("java.util.List", ((PsiClass)element).getQualifiedName());
  }

  public void testParseCustomBeanQuickFixOnUnresolvedReference() throws Throwable {
    addFileToSet(configureFileSet("xx", myModule), getTestName(false) + ".xml");
    myFixture.configureByFile(getTestName(false) + ".xml");
    final IntentionAction intention = myFixture.findSingleIntention("Try parsing custom beans");
    myFixture.launchAction(intention);
    assertEmpty(myFixture.filterAvailableIntentions("Try parsing custom beans"));
  }

  public void testResolveWorksForUnparsedBeans() throws Throwable {
    myFixture.addClass("public class A { public void setAaa(String aaa) {}}");
    myFixture.testHighlighting(getTestName(false) + ".xml");
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }

}
