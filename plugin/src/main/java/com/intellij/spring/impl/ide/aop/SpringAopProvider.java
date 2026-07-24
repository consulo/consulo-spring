/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.aop;

import com.intellij.aop.*;
import com.intellij.aop.psi.AllAdvisedElementsSearcher;
import com.intellij.java.language.impl.psi.impl.JavaConstantExpressionEvaluator;
import com.intellij.java.language.psi.*;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.aop.AopConfig;
import com.intellij.spring.impl.ide.model.xml.aop.BasicAdvice;
import com.intellij.spring.impl.ide.model.xml.aop.SpringAopAdvice;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.ProjectRootManager;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import consulo.util.dataholder.Key;
import consulo.util.lang.Pair;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlAttributeValue;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomFileElement;
import consulo.xml.dom.DomManager;
import consulo.xml.dom.DomUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author peter
 */
@ExtensionImpl
public class SpringAopProvider extends AopProvider {
  private static final Key<CachedValue<Set<? extends AopAspect>>> CACHED_SPRING_MODELS = Key.create("CachedSpringModels");
  private static final Key<CachedValue<AopAdvisedElementsSearcher>> CACHED_SEARCHER = Key.create("CACHED_SEARCHER");

  @Nonnull
  @Override
  public Set<? extends AopAspect> getAdditionalAspects(@Nonnull final consulo.module.Module module) {
    if (SpringManager.getInstance(module.getProject()) == null) return Collections.emptySet();

    if (module.getUserData(CACHED_SPRING_MODELS) == null) {
      module.putUserData(CACHED_SPRING_MODELS, CachedValuesManager.getManager(module.getProject()).createCachedValue(() -> {
        final Set<AopAspect> set = new HashSet<>();
        for (final SpringModel model : SpringUtils.getNonEmptySpringModels(module)) {
          for (final DomFileElement<Beans> element : model.getRoots()) {
            addAopAspects(set, element.getRootElement());
          }
        }
        return new CachedValueProvider.Result<>(set, PsiModificationTracker.MODIFICATION_COUNT);
      }, false));
    }

    return module.getUserData(CACHED_SPRING_MODELS).getValue();
  }

  protected static Set<AopAspect> addAopAspects(final Set<AopAspect> set, final DomElement element) {
    for (final DomElement child : DomUtil.getDefinedChildren(element, true, false)) {
      if (child instanceof AopAspect) {
        final AopAspect aspect = (AopAspect)child;
        set.add(aspect);
      }
      else if (child instanceof AopConfig) {
        final AopConfig config = (AopConfig)child;
        set.addAll(config.getAdvisors());
        set.addAll(config.getAspects());
      }
    }
    return set;
  }

  @Override
  public AopAdvisedElementsSearcher getAdvisedElementsSearcher(@Nonnull final PsiClass aClass) {
    return getSearcher(aClass);
  }

  public static AopAdvisedElementsSearcher getSearcher(final PsiClass aClass) {
    CachedValue<AopAdvisedElementsSearcher> value = aClass.getUserData(CACHED_SEARCHER);
    if (value == null) {
      aClass.putUserData(CACHED_SEARCHER, value = CachedValuesManager.getManager(aClass.getProject()).createCachedValue(() -> {
        Module module = aClass.getModule();
        if (module == null || hasNoSpringFacetAtAll(module)) {
          final GlobalSearchScope scope =
            module == null ? GlobalSearchScope.EMPTY_SCOPE : GlobalSearchScope.moduleWithDependenciesScope(
              module);
          final AopAdvisedElementsSearcher searcher =
            new AllAdvisedElementsSearcher(aClass.getManager(), scope) {
              @Override
              public boolean shouldSuppressErrors() {
                return true;
              }
            };
          return CachedValueProvider.Result.create(searcher,
                                                   PsiModificationTracker.MODIFICATION_COUNT,
                                                   ProjectRootManager.getInstance(aClass.getProject()));
        }

        final AopAdvisedElementsSearcher searcher =
          new SpringAdvisedElementsSearcher(aClass.getManager(),
                                            SpringUtils.getNonEmptySpringModels(module));
        return CachedValueProvider.Result.create(searcher,
                                                 PsiModificationTracker.MODIFICATION_COUNT,
                                                 ProjectRootManager.getInstance(aClass.getProject()));
      }, false));
    }
    return value.getValue();
  }

  @RequiredReadAction
  private static boolean hasNoSpringFacetAtAll(final consulo.module.Module module) {
    return ModuleUtilCore.visitMeAndDependentModules(module, module1 -> SpringModuleExtension.getInstance(module1) == null);
  }

  @Nullable
  @Override
  public Pair<? extends ArgNamesManipulator, PsiMethod> getCustomArgNamesManipulator(@Nonnull final PsiElement element) {
    if (element instanceof XmlAttributeValue &&
      element.getParent() instanceof XmlAttribute &&
      "pointcut-ref".equals(((XmlAttribute)element.getParent()).getLocalName())) {
      final XmlTag tag = PsiTreeUtil.getParentOfType(element, XmlTag.class);
      if (tag != null) {
        final DomElement domElement = DomManager.getDomManager(element.getProject()).getDomElement(tag);
        if (domElement instanceof BasicAdvice) {
          final BasicAdvice advice = (BasicAdvice)domElement;
          final PsiMethod method = advice.getMethod().getValue();
          if (advice.getPointcut().getStringValue() == null && method != null) {
            return Pair.create(new SpringArgNamesManipulator(tag), method);
          }
        }
      }
    }

    return super.getCustomArgNamesManipulator(element);
  }

  @Override
  public Integer getAdviceOrder(final AopAdvice advice) {
    if (advice instanceof SpringAopAdvice) {
      return ((SpringAopAdvice)advice).getOrder().getValue();
    }
    final PsiElement element = advice.getIdentifyingPsiElement();
    if (element instanceof PsiAnnotation) {
      final PsiClass aClass = PsiTreeUtil.getContextOfType(element, PsiClass.class, false);
      if (aClass == null) return null;

      final PsiAnnotation annotation = aClass.getModifierList().findAnnotation("org.springframework.core.annotation.Order");
      if (annotation != null) {
        final PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue("value");
        if (value instanceof PsiExpression) {
          final Object o = JavaConstantExpressionEvaluator.computeConstantExpression((PsiExpression)value, false);
          if (o instanceof Integer) {
            return (Integer)o;
          }
        }
        return null;
      }

      final PsiClass orderedClass =
        JavaPsiFacade.getInstance(aClass.getProject()).findClass("org.springframework.core.Ordered", aClass.getResolveScope());
      if (orderedClass != null && aClass.isInheritor(orderedClass, true)) {
        final PsiMethod[] methods = aClass.findMethodsByName("getOrder", true);
        for (final PsiMethod method : methods) {
          final PsiCodeBlock body = method.getBody();
          if (method.getParameterList().getParametersCount() == 0 && body != null && body.getStatements().length == 1) {
            final PsiStatement first = body.getStatements()[0];
            if (first instanceof PsiReturnStatement) {
              final PsiExpression value = ((PsiReturnStatement)first).getReturnValue();
              final Object o = JavaConstantExpressionEvaluator.computeConstantExpression(value, false);
              if (o instanceof Integer) {
                return (Integer)o;
              }
            }
          }
        }
      }
    }

    return super.getAdviceOrder(advice);
  }
}
