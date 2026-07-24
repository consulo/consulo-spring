/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.xml.beans.*;

import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class HeavySpringModelTest extends HeavySpringTestCase {

  public HeavySpringModelTest() {
    super(true);
  }

  public void testModelCache() throws Throwable {
    VirtualFile file = myTempDirTestFixture.getFile("testModelCache.xml");
    assert file != null;
    PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
    assert psiFile != null;
    SpringManager springManager = SpringManager.getInstance(myProject);
    SpringModel model = springManager.getSpringModelByFile((XmlFile)psiFile);
    assert model != null;
    assertEquals(2, model.getAllDomBeans().size());

    PsiClass psiClass = model.getAllDomBeans().iterator().next().getBeanClass();
    assert psiClass != null;
    List<SpringBaseBeanPointer> beansByPsiClass = model.findBeansByPsiClass(psiClass);
    assertEquals(2, beansByPsiClass.size());

    // delete import...
    final SpringImport springImport = model.getMergedModel().getImports().get(0);
    new WriteCommandAction.Simple(myProject) {

      protected void run() throws Throwable {
        springImport.undefine();
      }
    }.execute().throwException();

    final SpringModel newModel = springManager.getSpringModelByFile((XmlFile)psiFile);
    assert newModel != null;
    assert newModel != model;

    assertEquals(1, newModel.getAllDomBeans().size());
    List<SpringBaseBeanPointer> byPsiClass = newModel.findBeansByPsiClass(psiClass);
    assertEquals(1, byPsiClass.size());

    new WriteCommandAction.Simple(myProject) {

      protected void run() throws Throwable {
        SpringImport anImport = newModel.getMergedModel().addImport();
        anImport.getResource().setStringValue("import.xml");
      }
    }.execute().throwException();

    SpringModel anotherModel = springManager.getSpringModelByFile((XmlFile)psiFile);
    assert anotherModel != null;
    List<SpringBaseBeanPointer> byPsiClassBeans = anotherModel.findBeansByPsiClass(psiClass);
    assertEquals(2, byPsiClassBeans.size());

    List<SpringBaseBeanPointer> withInheritance = anotherModel.findBeansByEffectivePsiClassWithInheritance(psiClass);
    assertEquals(2, withInheritance.size());
  }

 public void testFileSetCache() throws Throwable {

    SpringFileSet fileSet = configureFileSet();

    VirtualFile file = addFile(fileSet, "testFileSet.xml");

    PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
    assert psiFile != null;
    SpringManager springManager = SpringManager.getInstance(myProject);
    SpringModel model = springManager.getSpringModelByFile((XmlFile)psiFile);

    assert model != null;
    assertEquals(1, model.getAllDomBeans().size());

    final SpringBean springBean = (SpringBean)model.findBean("name").getSpringBean();
    new WriteCommandAction.Simple(myProject) {

      protected void run() throws Throwable {
        springBean.undefine();
      }
    }.execute().throwException();

    SpringModel newModel = springManager.getSpringModelByFile((XmlFile)psiFile);
    assert newModel != null;
    assert newModel != model;
    assertEquals(0, newModel.getAllDomBeans().size());
  }

  protected String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/modelTest";
  }
}
