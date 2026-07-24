/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.highlighting;

import com.intellij.psi.PsiClass;
import com.intellij.spring.factories.SpringFactoryBeansManager;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.SpringHighlightingTestCase;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import consulo.ide.impl.idea.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SpringBeanFactoryCustomTypeResolversTest extends SpringHighlightingTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void configureModule(JavaModuleFixtureBuilder moduleBuilder) throws Exception {
    super.configureModule(moduleBuilder);
    addSpringJar(moduleBuilder);
  }

  public void assertFactoryProduces(@NotNull SpringBean factoryBean, @NotNull String... productClassNames) {
    PsiClass factoryBeanClass = factoryBean.getBeanClass();
    assertNotNull(factoryBeanClass);
    assertTrue(SpringFactoryBeansManager.isBeanFactory(factoryBeanClass));
    PsiClass[] classes = SpringUtils.getEffectiveBeanTypes(factoryBean);
    assertSameElements(ContainerUtil.map(classes, new NullableFunction<PsiClass, String>() {
      public String fun(PsiClass psiClass) {
        return psiClass.getQualifiedName();
      }
    }), productClassNames);
  }

  public void assertFactoryProduces(@NotNull Beans beans, @NotNull String id, @NotNull String... productClassNames) {
    SpringBean bean = findBeanById(beans, id);
    assertFactoryProduces(bean, productClassNames);
  }

  public void testBeanReferenceFactoryBean() throws Throwable {
    Beans beans = getBeans("spring-factory-beans-custom-resolvers.xml");
    assertFactoryProduces(beans, "beanReference", "TargetBean");
  }

  public void testScopedProxyFactoryBean() throws Throwable {
    Beans beans = getBeans("spring-factory-beans-custom-resolvers.xml");
    assertFactoryProduces(beans, "scopedProxy", "TargetBeanImplementingInterface");
    assertFactoryProduces(beans, "scopedProxyUsingInterface", "TargetInterfaceOne");
  }

  public void testProxyFactoryBean() throws Throwable {
    Beans beans = getBeans("spring-factory-beans-custom-resolvers.xml");
    assertFactoryProduces(beans, "proxyFromRef", "TargetInterfaceOne");
    assertFactoryProduces(beans, "proxyFromRefTwoInterfaces", "TargetInterfaceOne", "TargetInterfaceTwo");
    assertFactoryProduces(beans, "proxyFromName", "TargetInterfaceOne");
    assertFactoryProduces(beans, "proxyFromTargetClass", "TargetInterfaceOne");
    assertFactoryProduces(beans, "proxyUsingCglib", "TargetBeanImplementingInterface");
    assertFactoryProduces(beans, "proxySpecificInterface", "TargetInterfaceTwo");
    assertFactoryProduces(beans, "proxyNoAutodetectInterfaces", "TargetBeanImplementingMultipleInterfaces");
  }

  public void testTransactionProxyFactoryBean() throws Throwable {
    Beans beans = getBeans("spring-factory-beans-custom-resolvers.xml");
    assertFactoryProduces(beans, "txProxyNoInterfaces", "TargetBean");
    assertFactoryProduces(beans, "txProxyNestedTarget", "TargetBean");
    assertFactoryProduces(beans, "txProxyAllInterfaces", "TargetInterfaceOne", "TargetInterfaceTwo");
    assertFactoryProduces(beans, "txProxyUsingCglib", "TargetBeanImplementingMultipleInterfaces");
    assertFactoryProduces(beans, "txProxySpecificInterface", "TargetInterfaceOne");
  }

  // IDEA-13844
  public void testDoubleProxy() throws Throwable {
    Beans beans = getBeans("spring-factory-beans-custom-resolvers.xml");
    assertFactoryProduces(beans, "doubleProxy", "TargetInterfaceOne");
  }

  public void testCircularFactoryBeans() throws Throwable {
    Beans beans = getBeans("spring-factory-beans-custom-resolvers.xml");
    SpringBean factoryBean = findBeanById(beans, "circularOne");
    assertNotNull(factoryBean);
    PsiClass factoryBeanClass = factoryBean.getBeanClass();
    assertNotNull(factoryBeanClass);
    assertTrue(SpringFactoryBeansManager.isBeanFactory(factoryBeanClass));
    assertEmpty(SpringUtils.getEffectiveBeanTypes(factoryBean));
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/highlighting/";
  }
}
