/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.aop;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.jam.AopConstants;
import com.intellij.java.indexing.impl.search.MethodSuperSearcher;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.PsiModifierList;
import com.intellij.java.language.psi.search.searches.SuperMethodsSearch;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.java.language.psi.util.MethodSignatureBackedByPsiMethod;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringModelVisitor;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.aop.AopConfig;
import com.intellij.spring.impl.ide.model.xml.aop.AspectjAutoproxy;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.DomSpringBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.impl.ide.model.xml.tx.AnnotationDriven;
import consulo.application.ApplicationManager;
import consulo.application.progress.ProgressManager;
import consulo.application.util.AtomicNotNullLazyValue;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.application.util.function.Computable;
import consulo.application.util.function.Processor;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiModificationTracker;
import consulo.util.dataholder.Key;
import consulo.xml.util.xml.DomElement;
import consulo.xml.util.xml.DomFileElement;
import consulo.xml.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author peter
 */
public class SpringAdvisedElementsSearcher extends AopAdvisedElementsSearcher {
  private static final Key<CachedValue<Boolean>> CGLIB_PROXYING = Key.create("CGLIB_PROXYING");
  private final List<SpringModel> myModels;
  private final AtomicNotNullLazyValue<Boolean> myCglibProxyType = new AtomicNotNullLazyValue<Boolean>() {
    @Nonnull
    @Override
    protected Boolean compute() {
      for (final SpringModel model : myModels) {
        for (final DomFileElement<Beans> root : model.getRoots()) {
          CachedValue<Boolean> value = root.getUserData(CGLIB_PROXYING);
          if (value == null) {
            root.putUserData(CGLIB_PROXYING, value = CachedValuesManager.getManager(getManager().getProject()).createCachedValue(new CachedValueProvider<Boolean>() {
              public Result<Boolean> compute() {
                return Result.create(isCglib(root), root);
              }
            }, false));
          }
          if (value.getValue().booleanValue()) {
            return true;
          }
        }

      }
      return false;
    }
  };
  private static final Key<CachedValue<Boolean>> INHERITANCE_CACHE_KEY = Key.create("INHERITANCE_CACHE");

  public SpringAdvisedElementsSearcher(@Nonnull final PsiManager manager, final List<SpringModel> models) {
    super(manager);
    myModels = models;
  }

  private static boolean isCglib(DomFileElement<Beans> root) {
    final DomElement element = root.getRootElement();
        for (final AopConfig config : DomUtil.getDefinedChildrenOfType(element, AopConfig.class)) {
          if (Boolean.TRUE.equals(config.getProxyTargetClass().getValue())) {
            return true;
          }
        }

        for (final CommonSpringBean springBean : SpringUtils.getChildBeans(element, false)) {
          if (springBean instanceof AnnotationDriven && Boolean.TRUE.equals(((AnnotationDriven)springBean).getProxyTargetClass().getValue()) ||
              springBean instanceof AspectjAutoproxy && Boolean.TRUE.equals(((AspectjAutoproxy)springBean).getProxyTargetClass().getValue())) {
            return true;
          }
        }
    return false;
  }

  public boolean isAcceptable(final PsiClass psiClass) {
    return _isAcceptable(psiClass) && isSpringBeanClass(psiClass);

  }

  protected boolean isSpringBeanClass(PsiClass psiClass) {
    for (final SpringModel model : myModels) {
      if (!model.findBeansByPsiClassWithInheritance(psiClass).isEmpty()) return true;
    }
    return false;
  }

  private static boolean _isAcceptable(final PsiClass psiClass) {
    if (psiClass == null || psiClass.isInterface() || psiClass.hasModifierProperty(PsiModifier.FINAL)) return false;

    if (isAopClass(psiClass)) return false;

    final PsiModifierList modifierList = psiClass.getModifierList();
    if ((modifierList != null && modifierList.findAnnotation(AopConstants.ASPECT_ANNO) != null)) return false;
    return true;
  }

  public boolean process(final Processor<PsiClass> processor) {
    final MyBeanVisitor visitor = new MyBeanVisitor(processor);
    final Set<SpringModel> visited = new HashSet<SpringModel>();
    for (final SpringModel model : myModels) {
      ProgressManager.getInstance().checkCanceled();
      if (!visited.add(model)) continue;

      final Collection<? extends SpringBaseBeanPointer> beans =
        ApplicationManager.getApplication().runReadAction(new Computable<Collection<? extends SpringBaseBeanPointer>>() {
          public Collection<? extends SpringBaseBeanPointer> compute() {
            return model.getAllCommonBeans(true);
          }
        });

      for (final SpringBaseBeanPointer pointer : beans) {
        ProgressManager.getInstance().checkCanceled();

        final boolean[] stop = new boolean[]{false};
        ApplicationManager.getApplication().runReadAction(new Runnable() {
          public void run() {
            if (!pointer.isValid()) {
              return;
            }

            if (pointer instanceof DomSpringBeanPointer) {
              stop[0] = !SpringModelVisitor.visitBean(visitor, ((DomSpringBeanPointer)pointer).getSpringBean());
            }
            else {
              stop[0] = !visitor.processBeanClass(pointer.getBeanClass());
            }
          }
        });
        if (stop[0]) {
          return false;
        }
      }
    }

    return true;
  }

  private static class MyBeanVisitor extends SpringModelVisitor {
    private final Processor<PsiClass> myProcessor;

    private MyBeanVisitor(final Processor<PsiClass> processor) {
      myProcessor = processor;
    }

    protected boolean visitBean(final CommonSpringBean bean) {
      ProgressManager.getInstance().checkCanceled();
      return processBeanClass(bean.getBeanClass()) && super.visitBean(bean);
    }

    final boolean processBeanClass(@Nullable final PsiClass beanClass) {
      return !_isAcceptable(beanClass) || InheritanceUtil.processSupers(beanClass, true, myProcessor);
    }

  }

  private static boolean isAopClass(@Nonnull final PsiClass psiClass) {
    CachedValue<Boolean> value = psiClass.getUserData(INHERITANCE_CACHE_KEY);
    if (value == null) {
      value = CachedValuesManager.getManager(psiClass.getProject()).createCachedValue(new CachedValueProvider<Boolean>() {
        public Result<Boolean> compute() {
          final boolean result = !InheritanceUtil.processSupers(psiClass, true, new Processor<PsiClass>() {
            public boolean process(final PsiClass psiClass) {
              @NonNls final String qname = psiClass.getQualifiedName();
              return !"org.springframework.aop.Advisor".equals(qname) &&
                     !"org.aopalliance.aop.Advice".equals(qname) &&
                     !"org.springframework.aop.framework.AopInfrastructureBean".equals(qname);
            }
          });
          return Result.create(result, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
        }
      }, false);
      psiClass.putUserData(INHERITANCE_CACHE_KEY, value);
    }
    return value.getValue().booleanValue();
  }

  private static boolean hasInterfaces(@Nonnull final PsiClass psiClass, @Nonnull Set<PsiClass> visited) {
    if (psiClass.getInterfaces().length > 0) return true;
    final PsiClass superClass = psiClass.getSuperClass();
    return superClass != null && visited.add(superClass) && hasInterfaces(superClass, visited);
  }

  public boolean acceptsBoundMethod(@Nonnull final PsiMethod method) {
    if (!super.acceptsBoundMethod(method)) return false;
    if (method.hasModifierProperty(PsiModifier.STATIC)) return false;
    if (method.hasModifierProperty(PsiModifier.FINAL)) return false;
    if (method.hasModifierProperty(PsiModifier.PRIVATE)) return false;
    return true;
  }

  @Override
  public boolean acceptsBoundMethodHeavy(@Nonnull PsiMethod method) {
    if (isJdkProxyType()) {
      final PsiClass psiClass = method.getContainingClass();
      if (psiClass == null || hasInterfaces(psiClass, new HashSet<PsiClass>()) && !isFromInterface(method, psiClass)) return false;
    }
    return super.acceptsBoundMethodHeavy(method);
  }

  public boolean isJdkProxyType() {
    return !myCglibProxyType.getValue();
  }

  private static boolean isFromInterface(final PsiMethod method, final PsiClass psiClass) {
    return !new MethodSuperSearcher().execute(new SuperMethodsSearch.SearchParameters(method, psiClass, true, false), new Processor<MethodSignatureBackedByPsiMethod>() {
      public boolean process(final MethodSignatureBackedByPsiMethod signature) {
        final PsiClass aClass = signature.getMethod().getContainingClass();
        return aClass == null || !aClass.isInterface();
      }
    });
  }

  public List<SpringModel> getSpringModels() {
    return myModels;
  }

}
