/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.jam.AopConstants;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiJavaPackage;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.logging.Logger;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author peter
 */
public class AllAdvisedElementsSearcher extends AopAdvisedElementsSearcher {
  private static final Logger LOG = Logger.getInstance(AllAdvisedElementsSearcher.class);
  private GlobalSearchScope myScope;

  @TestOnly
  public AllAdvisedElementsSearcher(PsiManager manager) {
    this(manager, GlobalSearchScope.allScope(manager.getProject()));

  }

  public AllAdvisedElementsSearcher(PsiManager manager, GlobalSearchScope scope) {
    super(manager);
    myScope = scope;
  }

  @Override
  public boolean test(Predicate<PsiClass> processor) {
    PsiJavaPackage psiPackage = JavaPsiFacade.getInstance(getManager().getProject()).findPackage("");
    return psiPackage == null || processPackage(processor, psiPackage, new ArrayList<>());
  }

  @Override
  public boolean isAcceptable(PsiClass psiClass) {
    return true;
  }

  private boolean processPackage(Predicate<PsiClass> processor, PsiJavaPackage psiPackage, List<PsiJavaPackage> visited) {
    if (visited.contains(psiPackage)) {
      LOG.error("Circular package structure:\n" + StringUtil.join(visited,
                                                                  psiPackage1 -> psiPackage1.getQualifiedName() + " === " + StringUtil.join(
                                                                    psiPackage1.getDirectories(),
                                                                    psiDirectory -> psiDirectory.getVirtualFile().getPath(), "; "), "\n"));
    }

    visited.add(psiPackage);
    if (!ContainerUtil.process(
        psiPackage.getClasses(myScope),
        psiClass -> psiClass.getModifierList().findAnnotation(AopConstants.ASPECT_ANNO) != null || processor.test(psiClass)
    )) {
      return false;
    }
    for (PsiJavaPackage aPackage : psiPackage.getSubPackages(myScope)) {
      if (!processPackage(processor, aPackage, new ArrayList<>(visited))) return false;
    }
    return true;
  }
}
