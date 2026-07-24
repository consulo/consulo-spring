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
  public Set<? extends AopAspect> getAdditionalAspects(@Nonnull consulo.module.Module module) {
    if (SpringManager.getInstance(module.getProject()) == null) return Collections.emptySet();

    if (module.getUserData(CACHED_SPRING_MODELS) == null) {
      module.putUserData(CACHED_SPRING_MODELS, CachedValuesManager.getManager(module.getProject()).createCachedValue(() -> {
        Set<AopAspect> set = new HashSet<>();
        for (SpringModel model : SpringUtils.getNonEmptySpringModels(module)) {
          for (DomFileElement<Beans> element : model.getRoots()) {
            addAopAspects(set, element.getRootElement());
          }
        }
        return new CachedValueProvider.Result<>(set, PsiModificationTracker.MODIFICATION_COUNT);
      }, false));
    }

    return module.getUserData(CACHED_SPRING_MODELS).getValue();
  }

  protected static Set<AopAspect> addAopAspects(Set<AopAspect> set, DomElement element) {
    for (DomElement child : DomUtil.getDefinedChildren(element, true, false)) {
      if (child instanceof AopAspect) {
        AopAspect aspect = (AopAspect)child;
        set.add(aspect);
      }
      else if (child instanceof AopConfig) {
        AopConfig config = (AopConfig)child;
        set.addAll(config.getAdvisors());
        set.addAll(config.getAspects());
      }
    }
    return set;
  }

  @Override
  public AopAdvisedElementsSearcher getAdvisedElementsSearcher(@Nonnull PsiClass aClass) {
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
          AopAdvisedElementsSearcher searcher =
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

        AopAdvisedElementsSearcher searcher =
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
  private static boolean hasNoSpringFacetAtAll(consulo.module.Module module) {
    return ModuleUtilCore.visitMeAndDependentModules(module, module1 -> SpringModuleExtension.getInstance(module1) == null);
  }

  @Nullable
  @Override
  public Pair<? extends ArgNamesManipulator, PsiMethod> getCustomArgNamesManipulator(@Nonnull PsiElement element) {
    if (element instanceof XmlAttributeValue &&
      element.getParent() instanceof XmlAttribute &&
      "pointcut-ref".equals(((XmlAttribute)element.getParent()).getLocalName())) {
      XmlTag tag = PsiTreeUtil.getParentOfType(element, XmlTag.class);
      if (tag != null) {
        DomElement domElement = DomManager.getDomManager(element.getProject()).getDomElement(tag);
        if (domElement instanceof BasicAdvice) {
          BasicAdvice advice = (BasicAdvice)domElement;
          PsiMethod method = advice.getMethod().getValue();
          if (advice.getPointcut().getStringValue() == null && method != null) {
            return Pair.create(new SpringArgNamesManipulator(tag), method);
          }
        }
      }
    }

    return super.getCustomArgNamesManipulator(element);
  }

  @Override
  public Integer getAdviceOrder(AopAdvice advice) {
    if (advice instanceof SpringAopAdvice) {
      return ((SpringAopAdvice)advice).getOrder().getValue();
    }
    PsiElement element = advice.getIdentifyingPsiElement();
    if (element instanceof PsiAnnotation) {
      PsiClass aClass = PsiTreeUtil.getContextOfType(element, PsiClass.class, false);
      if (aClass == null) return null;

      PsiAnnotation annotation = aClass.getModifierList().findAnnotation("org.springframework.core.annotation.Order");
      if (annotation != null) {
        PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue("value");
        if (value instanceof PsiExpression) {
          Object o = JavaConstantExpressionEvaluator.computeConstantExpression((PsiExpression)value, false);
          if (o instanceof Integer) {
            return (Integer)o;
          }
        }
        return null;
      }

      PsiClass orderedClass =
        JavaPsiFacade.getInstance(aClass.getProject()).findClass("org.springframework.core.Ordered", aClass.getResolveScope());
      if (orderedClass != null && aClass.isInheritor(orderedClass, true)) {
        PsiMethod[] methods = aClass.findMethodsByName("getOrder", true);
        for (PsiMethod method : methods) {
          PsiCodeBlock body = method.getBody();
          if (method.getParameterList().getParametersCount() == 0 && body != null && body.getStatements().length == 1) {
            PsiStatement first = body.getStatements()[0];
            if (first instanceof PsiReturnStatement) {
              PsiExpression value = ((PsiReturnStatement)first).getReturnValue();
              Object o = JavaConstantExpressionEvaluator.computeConstantExpression(value, false);
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
