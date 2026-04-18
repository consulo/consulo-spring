/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.AopAspect;
import com.intellij.aop.AopProvider;
import com.intellij.aop.LocalAopModel;
import com.intellij.aop.jam.AopAfterReturningAdviceImpl;
import com.intellij.aop.jam.AopAfterThrowingAdviceImpl;
import com.intellij.aop.jam.AopConstants;
import com.intellij.aop.jam.AopLanguageInjector;
import com.intellij.testFramework.IdeaTestUtil;
import consulo.language.Language;
import consulo.language.inject.ConcatenationAwareInjector;
import consulo.language.inject.MultiHostInjector;
import consulo.component.extension.Extensions;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.language.psi.PsiManager;
import consulo.module.Module;
import consulo.util.lang.ref.Ref;
import consulo.document.util.TextRange;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.aop.SpringAopInjector;
import com.intellij.spring.aop.SpringAopProvider;
import com.intellij.spring.model.xml.aop.Advisor;
import com.intellij.spring.model.xml.aop.AopConfig;
import com.intellij.spring.model.xml.aop.SpringAspect;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import consulo.application.util.function.Processor;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.impl.DomManagerImpl;
import consulo.language.inject.MultiHostRegistrar;
import consulo.virtualFileSystem.VirtualFile;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.util.Set;

/**
 * @author peter
 */
public class AopInjectionTest extends JavaCodeInsightFixtureTestCase {

  public void testPointcutAnnotationInjection() throws Throwable {
    registerTrueAopProvider();
    final PsiClass psiClass = myFixture.addClass("package java.lang; class Object { @" + AopConstants.POINTCUT_ANNO +
                                         "(\"execution(* *())\") " +
                                         "public void foo();" +//0
                                         "}");
    final PsiAnnotationMemberValue attrValue = psiClass.getMethods()[0].getModifierList().getAnnotations()[0].findAttributeValue("value");
    checkInjection(attrValue, new AopLanguageInjector());
    final LocalAopModel model = attrValue.getUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL);
  }

  private void registerTrueAopProvider() {
    IdeaTestUtil.registerExtension(Extensions.getRootArea(), AopProvider.EXTENSION_POINT_NAME, new TrueAopProvider(), myTestRootDisposable);
  }

  public void testDeclareParentsAnnotationInjection() throws Throwable {
    registerTrueAopProvider();
    final PsiClass psiClass = myFixture.addClass("package java.lang; class Object { @" + AopConstants.DECLARE_PARENTS_ANNO +
                                         "(\"a.b.c*+\") " +
                                         "public int foo;" +//0
                                         "}");
    final PsiAnnotationMemberValue attrValue = psiClass.getFields()[0].getModifierList().getAnnotations()[0].findAttributeValue("value");
    checkIntoInjection(new AopLanguageInjector(), attrValue);
  }

  private static void checkIntoInjection(final ConcatenationAwareInjector injector, final PsiElement attrValue) {
    final Ref<Boolean> visited = Ref.create(false);
    injector.getLanguagesToInject(new MultiHostRegistrar() {
      @Nonnull
      public /*this*/ MultiHostRegistrar startInjecting(@Nonnull Language language) {
        assertEquals(AopPointcutExpressionLanguage.getInstance(), language);
        return this;
      }

      @Nonnull
      public /*this*/ MultiHostRegistrar addPlace(@NonNls @Nullable String prefix, @NonNls @Nullable String suffix,
																		  @Nonnull PsiLanguageInjectionHost host, @Nonnull TextRange rangeInsideHost) {
        assertFalse(visited.get());
        visited.set(true);
        assertEquals(TextRange.from(1, attrValue.getTextLength() - 2), rangeInsideHost);
        assertEquals("target(", prefix);
        assertEquals(")", suffix);
        return this;
      }

      public void doneInjecting() {

      }
    }, (PsiLanguageInjectionHost)attrValue);
    assertTrue(visited.get());
  }
  private static void checkIntoInjection(final MultiHostInjector injector, final PsiElement attrValue) {
    final Ref<Boolean> visited = Ref.create(false);
    injector.getLanguagesToInject(new MultiHostRegistrar() {
      @Nonnull
      public /*this*/ MultiHostRegistrar startInjecting(@Nonnull Language language) {
        assertEquals(AopPointcutExpressionLanguage.getInstance(), language);
        return this;
      }

      @Nonnull
      public /*this*/ MultiHostRegistrar addPlace(@NonNls @Nullable String prefix, @NonNls @Nullable String suffix,
																		  @Nonnull PsiLanguageInjectionHost host, @Nonnull TextRange rangeInsideHost) {
        assertFalse(visited.get());
        visited.set(true);
        assertEquals(TextRange.from(1, attrValue.getTextLength() - 2), rangeInsideHost);
        assertEquals("target(", prefix);
        assertEquals(")", suffix);
        return this;
      }

      public void doneInjecting() {

      }
    }, (PsiLanguageInjectionHost)attrValue);
    assertTrue(visited.get());
  }


  public void testAdviceAnnotationInjectionBefore() throws Throwable {
    registerTrueAopProvider();
    checkAdvice(AopConstants.BEFORE_ANNO);
  }

  public void testAdviceAnnotationInjectionAfter() throws Throwable {
    registerTrueAopProvider();
    checkAdvice(AopConstants.AFTER_ANNO);
  }

  public void testAdviceAnnotationInjectionAfterThrowing() throws Throwable {
    registerTrueAopProvider();
    checkAdvice(AopConstants.AFTER_THROWING_ANNO);
  }

  public void testAdviceAnnotationInjectionAround() throws Throwable {
    registerTrueAopProvider();
    checkAdvice(AopConstants.AROUND_ANNO);
  }

  private PsiAnnotationMemberValue checkAdvice(final String anno) throws IOException {
    final PsiClass psiClass = myFixture.addClass("package java.lang; " +
                                         "@" + AopConstants.ASPECT_ANNO + " " +
                                         "class Object { @" + anno + "(\"execution(* *())\") public void foo(org.aspectj.lang.ProceedingJoinPoint yyy);" +
                                         "}");
    final PsiAnnotationMemberValue attrValue = psiClass.getMethods()[0].getModifierList().getAnnotations()[0].findAttributeValue("value");
    checkInjection(attrValue, new AopLanguageInjector());
    return attrValue;
  }

  public void testAfterReturningAdviceInJava() throws Throwable {
    registerTrueAopProvider();
    final PsiClass psiClass = myFixture.addClass("package java.lang; " +
                                         "@" + AopConstants.ASPECT_ANNO + " " +
                                         "class Object { @" + AopConstants.AFTER_RETURNING_ANNO + "(value=\"execution(* *())\", returning=\"zzz\") " +
                                         "public void foo(org.aspectj.lang.ProceedingJoinPoint yyy, int zzz);" +
                                         "}");
    final PsiMethod method = psiClass.getMethods()[0];
    final PsiAnnotation annotation = method.getModifierList().getAnnotations()[0];
    final PsiAnnotationMemberValue attrValue = annotation.findAttributeValue("value");
    final AopAfterReturningAdviceImpl advice = new AopAfterReturningAdviceImpl() {
      public PsiMethod getPsiElement() {
        return method;
      }
    };
    checkInjection(attrValue, new AopLanguageInjector());

    assertEquals(method.getParameterList().getParameters()[1], advice.getReturning().getValue());
  }

  public void testAfterThrowingAdviceInJava() throws Throwable {
    registerTrueAopProvider();
    final PsiClass psiClass = myFixture.addClass("package java.lang; " +
                                         "@" + AopConstants.ASPECT_ANNO + " " +
                                         "class Object { @" + AopConstants.AFTER_THROWING_ANNO + "(value=\"execution(* *())\", throwing=\"zzz\") " +
                                         "public void foo(org.aspectj.lang.ProceedingJoinPoint yyy, int zzz);" +
                                         "}");
    final PsiMethod method = psiClass.getMethods()[0];
    final PsiAnnotation annotation = method.getModifierList().getAnnotations()[0];
    final PsiAnnotationMemberValue attrValue = annotation.findAttributeValue("value");
    final AopAfterThrowingAdviceImpl advice = new AopAfterThrowingAdviceImpl() {
      public PsiMethod getPsiElement() {
        return method;
      }
    };
    checkInjection(attrValue, new AopLanguageInjector());
    final LocalAopModel model = attrValue.getUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL);

    assertEquals(method.getParameterList().getParameters()[1], advice.getThrowing().getValue());
  }

  public void testAfterThrowingAndReturningPointcutParameterAdviceInJava() throws Throwable {
    registerTrueAopProvider();
    final PsiClass psiClass = myFixture.addClass("package java.lang; " +
                                         "@" + AopConstants.ASPECT_ANNO + " " +
                                         "class Object { " +
                                         "@" + AopConstants.AFTER_THROWING_ANNO + "(pointcut=\"execution(* *())\") public void foo(org.aspectj.lang.ProceedingJoinPoint yyy); " +
                                         "@" + AopConstants.AFTER_RETURNING_ANNO + "(pointcut=\"execution(* *())\") public void bar(org.aspectj.lang.ProceedingJoinPoint yyy); " +
                                         "}");
    PsiMethod method = psiClass.getMethods()[0];
    PsiAnnotation annotation = method.getModifierList().getAnnotations()[0];
    checkInjection(annotation.findAttributeValue("pointcut"), new AopLanguageInjector());

    method = psiClass.getMethods()[1];
    annotation = method.getModifierList().getAnnotations()[0];
    checkInjection(annotation.findAttributeValue("pointcut"), new AopLanguageInjector());

  }

  private static void checkInjection(final PsiElement attrValue, final ConcatenationAwareInjector injector) {
    final Ref<Boolean> visited = Ref.create(false);
    injector.getLanguagesToInject(new MultiHostRegistrar() {
      @Nonnull
      public /*this*/ MultiHostRegistrar startInjecting(@Nonnull Language language) {
        assertEquals(AopPointcutExpressionLanguage.getInstance(), language);
        return this;
      }

      @Nonnull
      public /*this*/ MultiHostRegistrar addPlace(@NonNls @Nullable String prefix, @NonNls @Nullable String suffix,
																		  @Nonnull PsiLanguageInjectionHost host, @Nonnull TextRange rangeInsideHost) {
        assertFalse(visited.get());
        visited.set(true);
        assertEquals(TextRange.from(1, attrValue.getTextLength() - 2), rangeInsideHost);
        assertNull(prefix);
        assertNull(suffix);
        return this;
      }

      public void doneInjecting() {

      }
    }, (PsiLanguageInjectionHost)attrValue);
    assertTrue(visited.get());
  }
  private static void checkInjection(final PsiElement attrValue, final MultiHostInjector injector) {
    final Ref<Boolean> visited = Ref.create(false);
    injector.getLanguagesToInject(new MultiHostRegistrar() {
      @Nonnull
      public /*this*/ MultiHostRegistrar startInjecting(@Nonnull Language language) {
        assertEquals(AopPointcutExpressionLanguage.getInstance(), language);
        return this;
      }

      @Nonnull
      public /*this*/ MultiHostRegistrar addPlace(@NonNls @Nullable String prefix, @NonNls @Nullable String suffix,
																		  @Nonnull PsiLanguageInjectionHost host, @Nonnull TextRange rangeInsideHost) {
        assertFalse(visited.get());
        visited.set(true);
        assertEquals(TextRange.from(1, attrValue.getTextLength() - 2), rangeInsideHost);
        assertNull(prefix);
        assertNull(suffix);
        return this;
      }

      public void doneInjecting() {

      }
    }, (PsiLanguageInjectionHost)attrValue);
    assertTrue(visited.get());
  }

  private XmlFile createXmlFile(final String text) throws IOException {
    final VirtualFile file = myFixture.getTempDirFixture().createFile("a.xml");
    VirtualFileUtil.saveText(file, text);
    return (XmlFile)PsiManager.getInstance(getProject()).findFile(file);
  }

  public void testSpringAopRegexPointcut() throws Throwable {
    XmlFile file = createXmlFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                 "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                 "xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + "xsi:schemaLocation=\"\n" +
                                 "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n" +
                                 "http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd\">\n" +
                                 "<aop:config>\n" +
                                 "<aop:pointcut id=\"businessService\" expression=\"execution(* com.xyz.myapp.service.*.*(..))\" type=\"regex\"/>\n" +
                                 "</aop:config>\n" +
                                 "</beans>");

    final DomManagerImpl domManager = DomManagerImpl.getDomManager(getProject());
    Beans beans = domManager.getFileElement(file, Beans.class).getRootElement();
    final AopConfig aopConfig = DomUtil.getChildrenOfType(beans, AopConfig.class).get(0);
    final GenericAttributeValue<PsiPointcutExpression> expression = aopConfig.getPointcuts().get(0).getExpression();
    final XmlAttributeValue attrValue = expression.getXmlAttributeValue();
    new SpringAopInjector().getLanguagesToInject(new MultiHostRegistrar() {
      @Nonnull
      public /*this*/ MultiHostRegistrar startInjecting(@Nonnull Language language) {
        fail();
        return null;
      }

      @Nonnull
      public /*this*/ MultiHostRegistrar addPlace(@NonNls @Nullable String prefix, @NonNls @Nullable String suffix,
																		  @Nonnull PsiLanguageInjectionHost host, @Nonnull TextRange rangeInsideHost) {

        return null;
      }

      public void doneInjecting() {

      }
    }, (PsiLanguageInjectionHost)attrValue);
  }
  public void testSpringDeclareParents() throws Throwable {
    XmlFile file = createXmlFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                 "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                 "xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + "xsi:schemaLocation=\"\n" +
                                 "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n" +
                                 "http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd\">\n" +
                                 "<aop:config>\n" +
                                 "<aop:aspect>\n" +
                                 "<aop:declare-parents types-matching=\"a..*+\"/>" +
                                 "</aop:aspect>\n" +
                                 "</aop:config>\n" +
                                 "</beans>");

    final DomManagerImpl domManager = DomManagerImpl.getDomManager(getProject());
    Beans beans = domManager.getFileElement(file, Beans.class).getRootElement();
    final AopConfig aopConfig = DomUtil.getChildrenOfType(beans, AopConfig.class).get(0);
    final SpringAspect aspect = aopConfig.getAspects().get(0);
    final GenericAttributeValue<AopReferenceHolder> expression = aspect.getIntroductions().get(0).getTypesMatching();
    checkIntoInjection(new SpringAopInjector(), expression.getXmlAttributeValue());
  }

  public void testSpringAdvice() throws Throwable {
    final PsiClass psiClass = myFixture.addClass("package foo; public class Bar { public void foo239() {} }");

    XmlFile file = createXmlFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                 "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                 "xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + "xsi:schemaLocation=\"\n" +
                                 "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n" +
                                 "http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd\">\n" +
                                 "<bean id=\"aBean\" class=\"foo.Bar\"/>" +
                                 "<aop:config>\n" +
                                 "<aop:aspect id=\"beforeExample\" ref=\"aBean\">\n" +
                                 "    <aop:before pointcut=\"execution(* com.xyz.myapp.dao.*.*(..))\" \n" +
                                 "      method=\"foo239\"/>\n" +
                                 "</aop:aspect>" +
                                 "</aop:config>\n" +
                                 "</beans>");

    final DomManagerImpl domManager = DomManagerImpl.getDomManager(getProject());
    Beans beans = domManager.getFileElement(file, Beans.class).getRootElement();
    final AopConfig aopConfig = DomUtil.getChildrenOfType(beans, AopConfig.class).get(0);
    final GenericAttributeValue<PsiPointcutExpression> expression = aopConfig.getAspects().get(0).getBefores().get(0).getPointcut();
    final XmlAttributeValue attrValue = expression.getXmlAttributeValue();
    checkInjection(attrValue, new SpringAopInjector());
    LocalAopModel model = attrValue.getUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL);
    assertEquals(model.getPointcutMethod(), psiClass.getMethods()[0]);
  }

  public void testSpringAfterReturningAdvice() throws Throwable {
    final PsiClass psiClass = myFixture.addClass("package foo; public class Bar { " +
                                                                      "public void foo239(org.aspectj.lang.ProceedingJoinPoint yyy) {} " +
                                                                      "}");

    XmlFile file = createXmlFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                 "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                 "xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + "xsi:schemaLocation=\"\n" +
                                 "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n" +
                                 "http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd\">\n" +
                                 "<bean id=\"aBean\" class=\"foo.Bar\"/>" +
                                 "<aop:config>\n" +
                                 "<aop:aspect id=\"beforeExample\" ref=\"aBean\">\n" +
                                 "    <aop:after-returning pointcut=\"execution(* com.xyz.myapp.dao.*.*(..))\" returning=\"xxx\"  method=\"foo239\"/>\n" +
                                 "</aop:aspect>" +
                                 "</aop:config>\n" +
                                 "</beans>");

    final DomManagerImpl domManager = DomManagerImpl.getDomManager(getProject());
    Beans beans = domManager.getFileElement(file, Beans.class).getRootElement();
    final AopConfig aopConfig = DomUtil.getChildrenOfType(beans, AopConfig.class).get(0);
    final GenericAttributeValue<PsiPointcutExpression> expression = aopConfig.getAspects().get(0).getAdvices().get(0).getPointcut();
    final XmlAttributeValue attrValue = expression.getXmlAttributeValue();
    checkInjection(attrValue, new SpringAopInjector());
    LocalAopModel model = attrValue.getUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL);
    assertEquals(model.getPointcutMethod(), psiClass.getMethods()[0]);
  }

  public void testSpringAfterThrowingAdvice() throws Throwable {
    final PsiClass psiClass = myFixture.addClass("package foo; public class Bar { " +
                                                                      "public void foo239(org.aspectj.lang.ProceedingJoinPoint yyy) {} " +
                                                                      "}");

    XmlFile file = createXmlFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                 "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                 "xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + "xsi:schemaLocation=\"\n" +
                                 "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n" +
                                 "http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd\">\n" +
                                 "<bean id=\"aBean\" class=\"foo.Bar\"/>" +
                                 "<aop:config>\n" +
                                 "<aop:aspect id=\"beforeExample\" ref=\"aBean\">\n" +
                                 "    <aop:after-throwing pointcut=\"execution(* com.xyz.myapp.dao.*.*(..))\" throwing=\"xxx\"  method=\"foo239\"/>\n" +
                                 "</aop:aspect>" +
                                 "</aop:config>\n" +
                                 "</beans>");

    final DomManagerImpl domManager = DomManagerImpl.getDomManager(getProject());
    Beans beans = domManager.getFileElement(file, Beans.class).getRootElement();
    final AopConfig aopConfig = DomUtil.getChildrenOfType(beans, AopConfig.class).get(0);
    final GenericAttributeValue<PsiPointcutExpression> expression = aopConfig.getAspects().get(0).getAdvices().get(0).getPointcut();
    final XmlAttributeValue attrValue = expression.getXmlAttributeValue();
    checkInjection(attrValue, new SpringAopInjector());
    LocalAopModel model = attrValue.getUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL);
    assertEquals(model.getPointcutMethod(), psiClass.getMethods()[0]);
  }

  public void testSpringAdvisor() throws Throwable {
    XmlFile file = createXmlFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                 "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                 "xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + "xsi:schemaLocation=\"\n" +
                                 "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n" +
                                 "http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd\">\n" +
                                 "<bean id=\"aBean\" class=\"foo.Bar\"/>" +
                                 "<aop:config>\n" +
                                 "<aop:pointcut id=\"businessService\" expression=\"execution(* com.xyz.myapp.service.*.*(..))\" type=\"regex\"/>\n" +
                                 "<aop:advisor pointcut-ref=\"businessService\" pointcut=\"xxx()\"/>\n" +
                                 "</aop:aspect>" +
                                 "</aop:config>\n" +
                                 "</beans>");

    final DomManagerImpl domManager = DomManagerImpl.getDomManager(getProject());
    Beans beans = domManager.getFileElement(file, Beans.class).getRootElement();
    final AopConfig aopConfig = DomUtil.getChildrenOfType(beans, AopConfig.class).get(0);
    final Advisor advisor = aopConfig.getAdvisors().get(0);
    final GenericAttributeValue<PsiPointcutExpression> expression = advisor.getPointcut();
    final XmlAttributeValue attrValue = expression.getXmlAttributeValue();
    checkInjection(attrValue, new SpringAopInjector());
    LocalAopModel model = attrValue.getUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL);
    assertNull(model.getPointcutMethod());

    assertEquals(aopConfig.getPointcuts().get(0), advisor.getPointcutRef().getValue());
  }

  public void testPointcutRefInAdvice() throws Throwable {
    IdeaTestUtil.registerExtension(Extensions.getRootArea(), AopProvider.EXTENSION_POINT_NAME, new AopProvider() {
      @Nullable
      public AopAdvisedElementsSearcher getAdvisedElementsSearcher(@Nonnull final PsiClass aClass) {
        return new AllAdvisedElementsSearcher(getPsiManager());
      }
    }, myTestRootDisposable);


    XmlFile file = createXmlFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                 "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                 "xmlns:aop=\"http://www.springframework.org/schema/aop\"\n" + "xsi:schemaLocation=\"\n" +
                                 "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n" +
                                 "http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-2.0.xsd\">\n" +
                                 "<aop:config>\n" +
                                 "<aop:pointcut id=\"businessService\" expression=\"execution(* com.xyz.myapp.service.*.*(..))\" type=\"regex\"/>\n" +
                                 "<aop:aspect id=\"beforeExample\" ref=\"aBean\">\n" +
                                 "    <aop:before pointcut-ref=\"businessService\" method=\"foo239\"/>\n" +
                                 "    <aop:before pointcut=\"execution(* com.xyz.myapp.service.*.*(..))\" method=\"foo239\"/>\n" +
                                 "</aop:aspect>" +
                                 "</aop:config>\n" +
                                 "</beans>");

    final DomManagerImpl domManager = DomManagerImpl.getDomManager(getProject());
    final Beans beans = domManager.getFileElement(file, Beans.class).getRootElement();
    IdeaTestUtil.registerExtension(AopProvider.EXTENSION_POINT_NAME, new SpringAopProvider() {
      @Nonnull
      public Set<? extends AopAspect> getAdditionalAspects(@Nonnull final Module module) {
        return addAopAspects(new THashSet<AopAspect>(), beans);
      }
    }, myTestRootDisposable);
    final AopConfig aopConfig = DomUtil.getChildrenOfType(beans, AopConfig.class).get(0);
    final SpringAspect aspect = aopConfig.getAspects().get(0);
    assertEquals(aopConfig.getPointcuts().get(0), aspect.getBefores().get(0).getPointcutRef().getValue());
    assertEquals(aopConfig.getPointcuts().get(0).getExpression().getValue(), aspect.getBefores().get(0).getPointcutExpression());
    assertEquals(aspect.getBefores().get(1).getPointcut().getValue(), aspect.getBefores().get(1).getPointcutExpression());
  }

  private class TrueAopProvider extends AopProvider {
    @Nullable
    public AopAdvisedElementsSearcher getAdvisedElementsSearcher(@Nonnull final PsiClass aClass) {
      return new AopAdvisedElementsSearcher(getPsiManager()) {
        public boolean process(final Processor<PsiClass> processor) {
          throw new UnsupportedOperationException("Method doProcess is not yet implemented in " + getClass().getName());
        }
      };
    }
  }
}
