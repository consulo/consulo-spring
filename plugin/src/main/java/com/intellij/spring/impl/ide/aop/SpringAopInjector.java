/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.aop;

import com.intellij.aop.IntroductionManipulator;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.aop.psi.AopPointcutExpressionLanguage;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.aop.*;
import consulo.document.util.TextRange;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.inject.MultiHostInjector;
import consulo.language.inject.MultiHostRegistrar;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.language.util.IncorrectOperationException;
import consulo.language.util.ProcessingContext;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.language.psi.util.XmlTagUtil;
import consulo.xml.language.psi.pattern.XmlAttributeValuePattern;
import consulo.xml.language.psi.pattern.XmlPatterns;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static consulo.language.pattern.StandardPatterns.*;
import static consulo.xml.dom.pattern.DomPatterns.*;

/**
 * @author peter
 */
public abstract class SpringAopInjector implements MultiHostInjector {
  private static final Key<BasicAdvice> SPRING_ADVICE_KEY = Key.create("SpringAdviceKey");
  private static final Key<SpringAspect> SPRING_ASPECT_KEY = Key.create("SpringAspectKey");
  private static final Key<DeclareParents> SPRING_INTRO_KEY = Key.create("SPRING_INTRO_KEY");
  private static final XmlAttributeValuePattern
    SPRING_AOP_INJECTION_PATTERN = XmlPatterns.xmlAttributeValue().withText(string().longerThan(1)).withParent(
    or(
      XmlPatterns.xmlAttribute("expression").and(
        withDom(
          domElement().withParent(
            domElement(SpringPointcut.class).and(
              not(domElement().withChild("type", genericDomValue(PointcutType.class).withValue(PointcutType.REGEX)))
            ).and(
              optional(domElement().inside(domElement(DomSpringBean.class)))
            ))
        )
      ),
      XmlPatterns.xmlAttribute("pointcut").and(withDom(domElement().withParent(or(
        domElement(Advisor.class),
        domElement(BasicAdvice.class).save(SPRING_ADVICE_KEY).withParent(
          domElement(SpringAspect.class).save(SPRING_ASPECT_KEY)
        )
      ))))
    )
  );
  private static final XmlAttributeValuePattern INTRO_PATTERN = XmlPatterns.xmlAttributeValue().withText(string().longerThan(1)).withParent(
    XmlPatterns.xmlAttribute("types-matching").and(
      withDom(
        domElement().withParent(
          domElement(DeclareParents.class).save(SPRING_INTRO_KEY).withParent(
            domElement(SpringAspect.class).save(SPRING_ASPECT_KEY)
          )
        )
      )
    )
  );

  public void injectLanguages(@Nonnull MultiHostRegistrar registrar, @Nonnull final PsiElement host) {
    final ProcessingContext context = new ProcessingContext();
    if (SPRING_AOP_INJECTION_PATTERN.accepts(host, context)) {
      SpringAdvisedElementsSearcher searcher = new SpringAdvisedElementsSearcher(host.getManager(), getBeansFromContext(host));
      host.putUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL, new SpringAdviceLocalAopModel(host, context.get(SPRING_ADVICE_KEY), searcher));
      registrar.startInjecting(AopPointcutExpressionLanguage.getInstance());
      registrar.addPlace(null, null, (PsiLanguageInjectionHost)host, TextRange.from(1, host.getTextLength() - 2));
      registrar.doneInjecting();
    }
    else if (INTRO_PATTERN.accepts(host, context)) {
      final SpringAdvisedElementsSearcher searcher = new SpringAdvisedElementsSearcher(host.getManager(), getBeansFromContext(host));
      host.putUserData(AopPointcutExpressionFile.LOCAL_AOP_MODEL, new SpringLocalAopModel(host, context.get(SPRING_ADVICE_KEY), searcher) {
        @Nullable
        public IntroductionManipulator getIntroductionManipulator() {
          return new IntroductionManipulator() {
            @Nonnull
            public PsiElement getCommonProblemElement() {
              return XmlTagUtil.getStartTagNameElement(getIntroduction().getXmlTag());
            }

            @Nonnull
            public DeclareParents getIntroduction() {
              return context.get(SPRING_INTRO_KEY);
            }

            public void defineDefaultImpl(Project project, ProblemDescriptor descriptor) throws IncorrectOperationException
			{
            }

            @NonNls
            public String getDefaultImplAttributeName() {
              return getIntroduction().getDefaultImpl().getXmlElementName();
            }

            @Nonnull
            public PsiElement getInterfaceElement() {
              return getIntroduction().getImplementInterface().getXmlAttributeValue();
            }

            @Nullable
            public PsiElement getDefaultImplElement() {
              return getIntroduction().getDefaultImpl().getXmlAttributeValue();
            }
          };
        }
      });
      registrar.startInjecting(AopPointcutExpressionLanguage.getInstance());
      registrar.addPlace("target(", ")", (PsiLanguageInjectionHost)host, TextRange.from(1, host.getTextLength() - 2));
      registrar.doneInjecting();
    }
  }

  private static List<SpringModel> getBeansFromContext(PsiElement host) {
    PsiFile file = host.getContainingFile();
    if (file instanceof XmlFile) {
      return SpringUtils.getNonEmptySpringModelsByFile((XmlFile)file);
    }
    return Collections.emptyList();
  }
}
