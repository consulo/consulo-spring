/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopAdviceType;
import com.intellij.aop.AopAdviceUtil;
import com.intellij.aop.jam.AopConstants;
import com.intellij.facet.FacetManager;
import consulo.application.Result;
import consulo.application.util.function.CommonProcessors;
import consulo.application.util.function.Processor;
import consulo.language.editor.WriteCommandAction;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringDomFileDescription;
import com.intellij.spring.SpringModel;
import com.intellij.spring.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.facet.SpringFacetType;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.highlighting.MissingAspectjAutoproxyInspection;
import com.intellij.spring.model.xml.aop.AfterReturningAdvice;
import com.intellij.spring.model.xml.aop.AopConfig;
import com.intellij.spring.model.xml.aop.BasicAdvice;
import com.intellij.spring.model.xml.aop.SpringAspect;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;

import java.util.function.Consumer;
import consulo.language.util.IncorrectOperationException;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.impl.DomFileElementImpl;
import com.intellij.util.xml.impl.DomManagerImpl;
import consulo.language.psi.PsiFileFactory;
import org.jetbrains.annotations.NonNls;

import java.util.Collections;
import java.util.List;

/**
 * @author peter
 */
public class SpringAopTest extends JavaCodeInsightFixtureTestCase {

  protected void setUp() throws Exception {
    super.setUp();

    new WriteCommandAction(getProject()) {
      protected void run(Result result) throws Throwable {
        FacetManager.getInstance(myModule).addFacet(SpringFacetType.INSTANCE, "ab", null);
      }
    }.execute();

    myFixture.addClass("package org.springframework.aop.aspectj.annotation; public class AnnotationAwareAspectJAutoProxyCreator {}");
  }

  public void testAdvices() throws Throwable {
    XmlFile file = createXmlFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                 "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                 "xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + "xsi:schemaLocation=\"\n" +
                                 "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n" +
                                 "http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd\">\n" +
                                 "<bean id=\"aBean\" class=\"foo.Bar\"/>" +
                                 "<aop:config>\n" +
                                 "<aop:aspect id=\"beforeExample\" ref=\"aBean\">\n" +
                                 "    <aop:before pointcut=\"execution(* com.xyz.myapp.dao.*.*(..))\"/>\n" +
                                 "    <aop:after pointcut=\"execution(* com.xyz.myapp.dao.*.*(..))\"/>\n" +
                                 "    <aop:after-returning pointcut=\"execution(* com.xyz.myapp.dao.*.*(..))\"/>\n" +
                                 "    <aop:after-throwing pointcut=\"execution(* com.xyz.myapp.dao.*.*(..))\"/>\n" +
                                 "    <aop:around pointcut=\"execution(* com.xyz.myapp.dao.*.*(..))\"/>\n" +
                                 "</aop:aspect>" +
                                 "</aop:config>\n" +
                                 "</beans>");

    DomManagerImpl domManager = DomManagerImpl.getDomManager(getProject());
    Beans beans = domManager.getFileElement(file, Beans.class).getRootElement();
    SpringAspect aspect = DomUtil.getChildrenOfType(beans, AopConfig.class).get(0).getAspects().get(0);
    List<BasicAdvice> list = aspect.getAdvices();
    assertEquals(5, list.size());
    for (int i = 0; i < list.size(); i++) {
      assertEquals(AopAdviceType.values()[i], list.get(i).getAdviceType());
    }
  }

  private XmlFile createXmlFile(String text) {
    return (XmlFile)PsiFileFactory.getInstance(getProject()).createFileFromText("a.xml", text);
  }

  public void testAfterReturning() throws Throwable {
    PsiClass psiClass = myFixture.addClass("package foo; class Bar { " +
                                        "Object foo1() {} " +
                                        "String foo2() {} " +
                                        "void advice1() {}" +
                                        "void advice2(Object ret) {}" +
                                        "void advice3(String ret) {}" +
                                        "}");

    final PsiMethod objMethod = psiClass.getMethods()[0];
    final PsiMethod strMethod = psiClass.getMethods()[1];
    final PsiMethod advice1 = psiClass.getMethods()[2];
    final PsiMethod advice2 = psiClass.getMethods()[3];
    final PsiMethod advice3 = psiClass.getMethods()[4];

    XmlFile file = (XmlFile)myFixture.addFileToProject("spring.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                 "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                 "xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + "xsi:schemaLocation=\"\n" +
                                 "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n" +
                                 "http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd\">\n" +
                                 "<bean id=\"aBean\" class=\"foo.Bar\"/>" +
                                 "<aop:config>\n" +
                                 "<aop:aspect id=\"beforeExample\" ref=\"aBean\">\n" +
                                 "    <aop:after-returning method=\"xxx\" pointcut=\"execution(* foo*(..))\" returning=\"ret\"/>\n" +
                                 "    <aop:after-returning method=\"advice1\" pointcut=\"execution(* foo*(..))\" returning=\"ret\"/>\n" +
                                 "    <aop:after-returning method=\"advice2\" pointcut=\"execution(* foo*(..))\" returning=\"ret\"/>\n" +
                                 "    <aop:after-returning method=\"advice3\" pointcut=\"execution(* foo*(..))\" returning=\"ret\"/>\n" +
                                 "</aop:aspect>" +
                                 "</aop:config>\n" +
                                                   "<aop:aspectj-autoproxy>" +
                                                   "</beans>");

    DomManagerImpl domManager = DomManagerImpl.getDomManager(getProject());
    Beans beans = domManager.getFileElement(file, Beans.class).getRootElement();
    SpringAspect aspect = DomUtil.getChildrenOfType(beans, AopConfig.class).get(0).getAspects().get(0);
    List<AfterReturningAdvice> list = aspect.getAfterReturnings();
    assertOrderedCollection(list, new Consumer<AfterReturningAdvice>() {
      public void consume(AfterReturningAdvice advice) {
        assertNull(advice.getMethod().getValue());
        assertNull(advice.getReturning().getValue());
        assertEquals(PointcutMatchDegree.TRUE, AopAdviceUtil.accepts(advice, objMethod));
        assertEquals(PointcutMatchDegree.TRUE, AopAdviceUtil.accepts(advice, strMethod));
      }
    }, new Consumer<AfterReturningAdvice>() {
      public void consume(AfterReturningAdvice advice) {
        assertEquals(advice1, advice.getMethod().getValue());
        assertNull(advice.getReturning().getValue());
        assertEquals(PointcutMatchDegree.TRUE, AopAdviceUtil.accepts(advice, objMethod));
        assertEquals(PointcutMatchDegree.TRUE, AopAdviceUtil.accepts(advice, strMethod));
      }
    }, new Consumer<AfterReturningAdvice>() {
      public void consume(AfterReturningAdvice advice) {
        assertEquals(advice2, advice.getMethod().getValue());
        assertEquals(advice2.getParameterList().getParameters()[0], advice.getReturning().getValue());
        assertEquals(PointcutMatchDegree.TRUE, AopAdviceUtil.accepts(advice, objMethod));
        assertEquals(PointcutMatchDegree.TRUE, AopAdviceUtil.accepts(advice, strMethod));
      }
    }, new Consumer<AfterReturningAdvice>() {
      public void consume(AfterReturningAdvice advice) {
        assertEquals(advice3, advice.getMethod().getValue());
        assertEquals(advice3.getParameterList().getParameters()[0], advice.getReturning().getValue());
        assertEquals(PointcutMatchDegree.FALSE, AopAdviceUtil.accepts(advice, objMethod));
        assertEquals(PointcutMatchDegree.TRUE, AopAdviceUtil.accepts(advice, strMethod));
      }
    });

  }

  public void testSearcherMethodFiltering() throws Throwable {
    SpringAdvisedElementsSearcher searcher = new SpringAdvisedElementsSearcher(getPsiManager(), Collections.<SpringModel>emptyList()) {
      public boolean isAcceptable(PsiClass psiClass) {
        return true;
      }
    };
    assertTrue(searcher.acceptsBoundMethod(parseMethod("public void foo() {}")));
    assertTrue(searcher.acceptsBoundMethod(parseMethod("void foo() {}")));
    assertTrue(searcher.acceptsBoundMethod(parseMethod("protected synchronized void foo() {}")));
    assertFalse(searcher.acceptsBoundMethod(parseMethod("private void foo() {}")));
    assertFalse(searcher.acceptsBoundMethod(parseMethod("static void foo() {}")));
    assertFalse(searcher.acceptsBoundMethod(parseMethod("final void foo() {}")));
    assertFalse(searcher.acceptsBoundMethod(parseMethod("final void foo() {}")));
  }

  public PsiMethod parseMethod(@NonNls String text) throws IncorrectOperationException {
    return JavaPsiFacade.getInstance(getProject()).getElementFactory().createClassFromText(text, null).getMethods()[0];
  }

  public void testAtAspectJEnableConditions() throws Throwable {
    DomManagerImpl domManager = DomManagerImpl.getDomManager(getProject());
    domManager.registerFileDescription(new SpringDomFileDescription(), getTestRootDisposable());
    DomFileElementImpl<Beans> fileElement = domManager.getFileElement(createXmlFile(
        "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
        "xmlns:aop=\"http://www.springframework.org/schema/aop\">\n" +
        "<bean id=\"bean\"/>" +
        "</beans>"), Beans.class);

    assertFalse(MissingAspectjAutoproxyInspection.isAspectJSupportEnabled(SpringUtils.getNonEmptySpringModelsByFile(fileElement.getFile())));

    fileElement = domManager.getFileElement(createXmlFile("<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                                 "xmlns:aop=\"http://www.springframework.org/schema/aop\">\n" +
                                 "<aop:aspectj-autoproxy/>" +
                                 "<bean id=\"bean\"/>" +
                                 "</beans>"), Beans.class);
    assertTrue(MissingAspectjAutoproxyInspection.isAspectJSupportEnabled(SpringUtils.getNonEmptySpringModelsByFile(fileElement.getFile())));
  }

  public void testSearcherProcessClasses() throws Throwable {
    SpringAdvisedElementsSearcher searcher = new SpringAdvisedElementsSearcher(getPsiManager(), Collections.<SpringModel>emptyList());
    assertTrue(searcher.process(new Processor<PsiClass>() {
      public boolean process(PsiClass psiClass) {
        throw new UnsupportedOperationException("Method process is not yet implemented in " + getClass().getName());
      }
    }));

    myFixture.addClass("package org.aspectj.lang.annotation; public @interface Advisor {}");
    myFixture.addClass("package org.springframework.aop; public class Advice {}");
    myFixture.addClass("package org.springframework.aop.framework; public class AopInfrastructureBean {}");

    PsiClass a = myFixture.addClass("package a; public class Bean {}");
    PsiClass b = myFixture.addClass("package b; public final class Bean {}");
    PsiClass c = myFixture.addClass("package c; public class Bean {}");
    PsiClass d = myFixture.addClass("package d; public class Bean {}");
    PsiClass e = myFixture.addClass("package e; @" + AopConstants.ASPECT_ANNO + " public class Bean {}");
    PsiClass f = myFixture.addClass("package f; public class Bean {}");
    PsiClass with_pointcut = myFixture.addClass("public class Foo { @" + AopConstants.POINTCUT_ANNO + " void foo(); }");

    /*registerExtension(ExtensionPointName.create("com.intellij.annotatedMembersSearch"), new QueryExecutor<PsiMember, AnnotatedMembersSearch.Parameters>(){
      public boolean execute(final AnnotatedMembersSearch.Parameters queryParameters, final Processor<PsiMember> consumer) {
        return consumer.process(with_pointcut.getMethods()[0]);
      }
    });*/

    XmlFile file = (XmlFile) myFixture.addFileToProject("spring.xml",
                                                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                 "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                 "xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + "xsi:schemaLocation=\"\n" +
                                 "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n" +
                                 "http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd\">\n" +
                                 "<bean id=\"aBean\" class=\"a.Bean\"/>" +
                                 "<bean id=\"bBean\" class=\"b.Bean\"/>" +
                                 "<bean id=\"cBean\" class=\"c.Bean\"/>" +
                                 "<bean id=\"eBean\" class=\"e.Bean\"/>" +
                                 "<bean id=\"advisorBean\" class=\"org.springframework.aop.Advisor\"/>" +
                                 "<bean id=\"adviceBean\" class=\"org.aopalliance.aop.Advice\"/>" +
                                 "<bean id=\"infrastructureBean\" class=\"org.springframework.aop.framework.AopInfrastructureBean\"/>" +
                                 "</beans>");


    DomManagerImpl domManager = DomManagerImpl.getDomManager(getProject());
    DomFileElement<Beans> fileElement = domManager.getFileElement(file, Beans.class);
    Beans beans = fileElement.getRootElement();
    /*getProject().registerService(SpringManager.class, new SpringManagerImpl(domManager){
      @Nullable
      public SpringModel getSpringModelByFile(@NotNull final XmlFile file) {
        return new SpringModelImpl(fileElement, new THashSet<XmlFile>(Arrays.asList(file)), module, null);
      }
    });
    */

    searcher = new SpringAdvisedElementsSearcher(getPsiManager(), SpringUtils.getNonEmptySpringModelsByFile(fileElement.getFile()));
    CommonProcessors.CollectProcessor<PsiClass> processor = new CommonProcessors.CollectProcessor<PsiClass>();
    searcher.process(processor);
    assertSameElements(processor.getResults(), a, c, a.getSuperClass(), c.getSuperClass());

  }

}
