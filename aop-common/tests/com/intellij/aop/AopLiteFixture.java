/*
 * Copyright (c) 2007 Your Corporation. All Rights Reserved.
 */
package com.intellij.aop;

import com.intellij.aop.psi.AopAnnotator;
import com.intellij.aop.psi.AopPointcutExpressionLanguage;
import com.intellij.aop.psi.AopPointcutExpressionParserDefinition;
import com.intellij.lang.LanguageAnnotators;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.spring.SpringDefaultDomExtender;
import com.intellij.spring.aop.SpringAopDomExtender;
import com.intellij.spring.aop.SpringTxDomExtender;
import com.intellij.spring.model.xml.SpringModelElement;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.ListOrSet;
import com.intellij.testFramework.LiteFixture;
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture;

import java.io.IOException;

public abstract class AopLiteFixture extends LiteFixture {
  protected void setUp() throws Exception {
    super.setUp();
    registerAopParserDefinition();
  }

  public static void registerAopParserDefinition() {
    LanguageParserDefinitions.INSTANCE.addExplicitExtension(AopPointcutExpressionLanguage.getInstance(),
                                                           new AopPointcutExpressionParserDefinition());
    LanguageAnnotators.INSTANCE.addExplicitExtension(AopPointcutExpressionLanguage.getInstance(), new AopAnnotator());
  }

  public static void addAopAnnotations(JavaCodeInsightTestFixture fixture) throws IOException {
    fixture.addClass("package org.aspectj.lang.annotation; public @interface Pointcut {" +
                                        "java.lang.String value();" +
                                        "java.lang.String argNames() default \"\";" +
                                        "}");
    fixture.addClass("package org.aspectj.lang.annotation; public @interface Before {" +
                                        "java.lang.String value();" +
                                        "java.lang.String argNames() default \"\";" +
                                        "}");
    fixture.addClass("package org.aspectj.lang.annotation; public @interface After {" +
                                        "java.lang.String value();" +
                                        "java.lang.String argNames() default \"\";" +
                                        "}");
    fixture.addClass("package org.aspectj.lang.annotation; public @interface Around {" +
                                        "java.lang.String value();" +
                                        "java.lang.String argNames() default \"\";" +
                                        "}");
    fixture.addClass("package org.aspectj.lang.annotation; public @interface AfterReturning {" +
                                        "java.lang.String value() default \"\";" +
                                        "java.lang.String argNames() default \"\";" +
                                        "java.lang.String returning() default \"\";" +
                                        "}");
    fixture.addClass("package org.aspectj.lang.annotation; public @interface AfterThrowing {" +
                                        "java.lang.String value() default \"\";" +
                                        "java.lang.String argNames() default \"\";" +
                                        "java.lang.String throwing() default \"\";" +
                                        "}");
    fixture.addClass("package org.aspectj.lang.annotation; public @interface Aspect {" +
                                        "java.lang.String value() default \"\"" +
                                        "}");
    fixture.addClass("package org.aspectj.lang.annotation; public @interface DeclareParents {" +
                                        "java.lang.String value();" +
                                        "java.lang.Class defaultImpl() default org.aspectj.lang.annotation.DeclareParents.class;" +
                                        "}");
  }

  public static void registerSpringDomExtenders(LiteFixture fixture) {
    fixture.registerDomExtender(Beans.class, SpringDefaultDomExtender.BeansExtender.class);
    fixture.registerDomExtender(ListOrSet.class, SpringDefaultDomExtender.ListOrSetExtender.class);
    fixture.registerDomExtender(SpringModelElement.class, SpringAopDomExtender.class);
    fixture.registerDomExtender(Beans.class, SpringTxDomExtender.class);
  }
}