/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.AopAspect;
import com.intellij.aop.AopPointcut;
import com.intellij.aop.LocalAopModel;
import com.intellij.mock.MockXmlTag;
import com.intellij.psi.PsiClass;
import consulo.language.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import consulo.application.util.function.Processor;
import com.intellij.util.xml.GenericValue;
import com.intellij.util.xml.ReadOnlyGenericValue;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author peter
*/
public abstract class MockAopModel extends LocalAopModel {

  protected MockAopModel(@Nullable PsiElement host, @Nonnull final PsiMethod pointcutMethod) {
    super(host, pointcutMethod, new AopAdvisedElementsSearcher(pointcutMethod.getManager()) {
      public boolean process(Processor<PsiClass> processor) {
        throw new UnsupportedOperationException("Method doProcess is not yet implemented");
      }
    });
  }

  protected MockAopModel(AopAdvisedElementsSearcher searcher) {
    super(null, null, searcher);
  }

  public List<? extends AopAspect> getAspects() {
    return Collections.emptyList();
  }

  protected AopPointcut createMockPointcut(@NonNls String qname) {
    return createMockPointcut(qname, null, new MockXmlTag());
  }

  protected AopPointcut createMockPointcut(@NonNls final String qname, final PsiPointcutExpression expression, final PsiElement element) {
    return new AopPointcut() {
      public GenericValue<PsiPointcutExpression> getExpression() {
        return new ReadOnlyGenericValue<PsiPointcutExpression>() {
          public PsiPointcutExpression getValue() {
            return expression;
          }
        };
      }

      public GenericValue<String> getQualifiedName() {
        return ReadOnlyGenericValue.getInstance(qname);
      }

      public PsiElement getIdentifyingPsiElement() {
        return element;
      }

      public int getParameterCount() {
        return -1;
      }
    };
  }
}
