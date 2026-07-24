/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.AopProvider;
import consulo.component.extension.Extensions;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.LocalInspectionTool;
import com.intellij.facet.FacetManager;
import com.intellij.testFramework.IdeaTestUtil;
import consulo.application.Result;
import com.intellij.psi.PsiClass;
import consulo.language.psi.PsiFileFactory;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.aop.SpringAopProvider;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.facet.SpringFacetConfiguration;
import com.intellij.spring.facet.SpringFacetType;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.highlighting.MissingAspectjAutoproxyInspection;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.util.xml.DomManager;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collections;

/**
 * @author peter
 */
public class AopSpringJavaHighlightingTest extends JavaCodeInsightFixtureTestCase {

  protected void setUp() throws Exception {
    super.setUp();

    myFixture.addClass("package org.aspectj.lang.annotation; public @interface Aspect {}");
    myFixture.addClass("package org.springframework.aop.aspectj.annotation; public class AnnotationAwareAspectJAutoProxyCreator {}");
  }

  protected String getBasePath() {
    return "/testData/aop/highlighting/java/";
  }

  private void doTest(LocalInspectionTool... tools) throws Throwable {
    doTest(getTestName(true) + ".java", tools);
  }

  private void doTest(String path, LocalInspectionTool... tools) throws Throwable {
    myFixture.enableInspections(tools);
    myFixture.copyFileToProject(path);
    myFixture.testHighlighting(true, false, false, path);
  }

  public void testAddAspectJAutoproxy() throws Throwable {
    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        FacetManager.getInstance(myModule).addFacet(SpringFacetType.INSTANCE, "z", null);
      }
    }.execute();

    SpringFacetConfiguration configuration = SpringFacet.getInstance(myModule).getConfiguration();
    SpringFileSet fileSet = new SpringFileSet("zz", "dd", configuration);
    configuration.getFileSets().add(fileSet);
    fileSet.addFile(myFixture.copyFileToProject("addAspectJAutoproxy.xml"));

    doTest(new MissingAspectjAutoproxyInspection());
  }

  private Beans getFileElement(String text) {
    XmlFile file = (XmlFile)PsiFileFactory.getInstance(getProject()).createFileFromText("a.xml", text);
    return DomManager.getDomManager(getProject()).getFileElement(file, Beans.class).getRootElement();
  }

  public void testAddAspectJAutoproxyDtd() throws Throwable {
    final SpringBean bean = getFileElement(
        "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">" +
        "<beans>\n" +
        "<bean id=\"bean\"/>" +
        "<bean class=\"org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator\"/>" +
        "</beans>").getBeans().get(0);

    IdeaTestUtil.registerExtension(Extensions.getRootArea(), AopProvider.EXTENSION_POINT_NAME, new SpringAopProvider() {
      @Nonnull
      public AopAdvisedElementsSearcher getAdvisedElementsSearcher(@Nonnull PsiClass aClass) {
        return new SpringAdvisedElementsSearcher(aClass.getManager(), Collections.singletonList(SpringUtils.getSpringModel(bean)));
      }
    }, myTestRootDisposable);

    doTest(new MissingAspectjAutoproxyInspection());
  }

  public void testDontAddAspectJAutoproxyXsd() throws Throwable {
    final SpringBean bean = getFileElement("<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
    "xmlns:aop=\"http://www.springframework.org/schema/aop\">\n" +
    "<bean id=\"bean\"/>" +
    "<aop:aspectj-autoproxy/>" +
    "</beans>").getBeans().get(0);

    IdeaTestUtil.registerExtension(Extensions.getRootArea(), AopProvider.EXTENSION_POINT_NAME, new SpringAopProvider() {
      @Nullable
      public AopAdvisedElementsSearcher getAdvisedElementsSearcher(@Nonnull PsiClass aClass) {
        return new SpringAdvisedElementsSearcher(aClass.getManager(), Collections.singletonList(SpringUtils.getSpringModel(bean)));
      }
    }, myTestRootDisposable);

    doTest("addAspectJAutoproxyDtd.java", new MissingAspectjAutoproxyInspection());
  }


}
