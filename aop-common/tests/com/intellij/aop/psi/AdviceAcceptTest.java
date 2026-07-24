/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.aop.AopAdvice;
import com.intellij.aop.AopAdviceUtil;
import com.intellij.aop.AopAspect;
import com.intellij.aop.AopProvider;
import com.intellij.aop.jam.AopAfterReturningAdviceImpl;
import com.intellij.aop.jam.AopConstants;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.psi.PsiClass;
import consulo.language.psi.PsiFileFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringDomFileDescription;
import com.intellij.spring.aop.SpringAopProvider;
import com.intellij.spring.model.xml.aop.AopConfig;
import com.intellij.spring.model.xml.aop.BasicAdvice;
import com.intellij.spring.model.xml.aop.SpringAspect;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.testFramework.IdeaTestUtil;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.impl.DomManagerImpl;
import consulo.module.Module;
import gnu.trove.THashSet;
import jakarta.annotation.Nonnull;

import java.util.Set;

/**
 * @author peter
 */
public class AdviceAcceptTest extends LightCodeInsightFixtureTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    myFixture.addClass("package java.lang; public class AbstractString {}");
    myFixture.addClass("package java.lang; public class String extends AbstractString {}");

    myFixture.addClass("package org.springframework.aop.aspectj.annotation; public class AnnotationAwareAspectJAutoProxyCreator {}");
  }

  public void testSpringAfterReturning() throws Throwable {
    final PsiClass beanClass = myFixture.addClass("public class BeanClass {" +
                                                                   "void foo239(String str) {}" +
                                                                   "AbstractString foo() {} " +
                                                                   "String bar() {} " +
                                                                   "}");


    XmlFile file = (XmlFile)PsiFileFactory.getInstance(getProject()).createFileFromText("a.xml", StdFileTypes.XML,
                                 "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                 "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                 "xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + "xsi:schemaLocation=\"\n" +
                                 "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n" +
                                 "http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd\">\n" +
                                 "<bean id=\"aBean\" class=\"BeanClass\"/>" +
                                 "<aop:config>\n" +
                                 "<aop:pointcut id=\"businessService\" expression=\"execution(* com.xyz.myapp.service.*.*(..))\" type=\"regex\"/>\n" +
                                 "<aop:aspect id=\"beforeExample\" ref=\"aBean\">\n" +
                                 "    <aop:after-returning pointcut=\"execution(* *(..))\" method=\"foo239\" returning=\"str\"/>\n" +
                                 "</aop:aspect>" +
                                 "</aop:config>\n" +
                                 "<aop:aspectj-autoproxy>\n" +
                                 "</beans>", 0, true);

    final DomManagerImpl domManager = DomManagerImpl.getDomManager(getProject());
    domManager.registerFileDescription(new SpringDomFileDescription(), getTestRootDisposable());
    final Beans beans = domManager.getFileElement(file, Beans.class).getRootElement();
    IdeaTestUtil.registerExtension(AopProvider.EXTENSION_POINT_NAME, new SpringAopProvider() {
      @Nonnull
      public Set<? extends AopAspect> getAdditionalAspects(@Nonnull final Module module) {
        return addAopAspects(new THashSet<AopAspect>(), beans);
      }
    }, myTestRootDisposable);
    final AopConfig aopConfig = DomUtil.getChildrenOfType(beans, AopConfig.class).get(0);
    final SpringAspect aspect = aopConfig.getAspects().get(0);
    final BasicAdvice advice = aspect.getAdvices().get(0);
    final PsiPointcutExpression expression = advice.getPointcutExpression();

    final PsiMethod foo = beanClass.getMethods()[1];
    final PsiMethod bar = beanClass.getMethods()[2];
    assertEquals(PointcutMatchDegree.TRUE, expression.acceptsSubject(new PointcutContext(expression), foo));
    assertEquals(PointcutMatchDegree.TRUE, expression.acceptsSubject(new PointcutContext(expression), bar));
    assertEquals(PointcutMatchDegree.FALSE, AopAdviceUtil.accepts(advice, foo));
    assertEquals(PointcutMatchDegree.TRUE, AopAdviceUtil.accepts(advice, bar));
  }

  public void testSpringWithParameter() throws Throwable {
    final PsiClass beanClass = myFixture.addClass("public class BeanClass {" +
                                                                   "void foo239(String str) {}" +
                                                                   "AbstractString foo() {} " +
                                                                   "String bar() {} " +
                                                                   "}");


    XmlFile file = (XmlFile) PsiFileFactory.getInstance(getProject()).createFileFromText("a.xml", StdFileTypes.XML,
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                 "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                 "xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + "xsi:schemaLocation=\"\n" +
                                 "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n" +
                                 "http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd\">\n" +
                                 "<bean id=\"aBean\" class=\"BeanClass\"/>" +
                                 "<aop:config>\n" +
                                 "<aop:pointcut id=\"pct\" expression=\"args(str)\"/>\n" +
                                 "<aop:aspect id=\"beforeExample\" ref=\"aBean\">\n" +
                                 "    <aop:around pointcut-ref=\"pct\" method=\"foo239\" returning=\"str\"/>\n" +
                                 "</aop:aspect>" +
                                 "</aop:config>\n" +
                                 "<aop:aspectj-autoproxy>\n" +
                                 "</beans>",0,true);

    final DomManagerImpl domManager = DomManagerImpl.getDomManager(getProject());
    domManager.registerFileDescription(new SpringDomFileDescription(), getTestRootDisposable());
    final Beans beans = domManager.getFileElement(file, Beans.class).getRootElement();
    IdeaTestUtil.registerExtension(AopProvider.EXTENSION_POINT_NAME, new SpringAopProvider() {
      @Nonnull
      public Set<? extends AopAspect> getAdditionalAspects(@Nonnull final consulo.module.Module module) {
        return addAopAspects(new THashSet<AopAspect>(), beans);
      }
    }, myTestRootDisposable);
    final AopConfig aopConfig = DomUtil.getChildrenOfType(beans, AopConfig.class).get(0);
    final SpringAspect aspect = aopConfig.getAspects().get(0);
    final BasicAdvice advice = aspect.getAdvices().get(0);

    assertEquals(PointcutMatchDegree.TRUE, AopAdviceUtil.accepts(advice, beanClass.getMethods()[0]));
  }

  public void testJavaAfterReturning() throws Throwable {
    final PsiClass beanClass = myFixture.addClass("public class BeanClass {" +
                                                                   "void foo239(String lst) {}" +
                                                                   "AbstractString foo() {} " +
                                                                   "String bar() {} " +
                                                                   "@" + AopConstants.AFTER_RETURNING_ANNO + "(value=\"execution(* *())\", returning=\"str\")" +
                                                                   "void advice(String str) {} " +
                                                                   "}");

    final PsiMethod adviceMethod = beanClass.getMethods()[3];
    final AopAdvice advice = new AopAfterReturningAdviceImpl() {
      @Override
      public PsiMethod getPsiElement() {
        return adviceMethod;
      }
    };
    final PsiPointcutExpression expression = advice.getPointcutExpression();

    final PsiMethod foo = beanClass.getMethods()[1];
    final PsiMethod bar = beanClass.getMethods()[2];
    assertEquals(PointcutMatchDegree.TRUE, expression.acceptsSubject(new PointcutContext(expression), foo));
    assertEquals(PointcutMatchDegree.TRUE, expression.acceptsSubject(new PointcutContext(expression), bar));
    assertEquals(PointcutMatchDegree.FALSE, AopAdviceUtil.accepts(advice, foo));
    assertEquals(PointcutMatchDegree.TRUE, AopAdviceUtil.accepts(advice, bar));
  }

}
