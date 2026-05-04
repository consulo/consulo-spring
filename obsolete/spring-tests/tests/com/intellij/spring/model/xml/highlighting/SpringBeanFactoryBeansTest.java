/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.spring.factories.ObjectTypeResolver;
import com.intellij.spring.factories.SpringFactoryBeansManager;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class SpringBeanFactoryBeansTest extends SpringHighlightingTestCase {


  protected void setUp() throws Exception {
    super.setUp();
    PsiTestUtil.addLibrary(myModuleTestFixture.getModule(), "hb3", PathManager.getHomePath() + "/lib/", "hibernate3.jar");
  }

  protected void configureModule(final JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    addSpringJar(moduleBuilder);
  }

  public void testSpringFactoryBeanInterface() throws Throwable {
    myFixture.testHighlighting("spring-factory-bean-interface.xml");
  }

  public void testHibernateSessionFactory() throws Throwable {
    myFixture.testHighlighting("spring-factory-beans.xml");
  }

  public void testHibernateSessionFactoryCompletion() throws Throwable {
    myFixture.testCompletion("spring-factory-beans_befor.xml", "spring-factory-beans_after.xml");
  }

  public void testInheritedWithBeanFactory() throws Throwable {
    SpringFactoryBeansManager.getInstance().registerFactory("FooBeanFactory", new ObjectTypeResolver() {

      @NotNull
      public Set<String> getObjectType(@NotNull final CommonSpringBean bean) {
        return Collections.singleton("FooBean2");
      }

      public boolean accept(@NotNull final String factoryClassName) {
        return factoryClassName.equals("FooBeanFactory");
      }
    });
    myFixture.testHighlighting("spring-factory-beans-inheritance.xml");
    SpringFactoryBeansManager.getInstance().unregisterFactory("FooBeanFactory");
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }
}