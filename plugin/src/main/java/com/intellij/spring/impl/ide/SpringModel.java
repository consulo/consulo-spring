/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.model.context.ComponentScan;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.SpringQualifier;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.module.Module;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomFileElement;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public interface SpringModel {

  SpringModel[] EMPTY_ARRAY = new SpringModel[0];

  @Nonnull
  String getId();

  @Nonnull
  SpringModel[] getDependencies();

  SpringFileSet getFileSet();

  @Nullable
  SpringBeanPointer findBean(@NonNls @Nonnull String beanName);

  @Nullable
  SpringBeanPointer findParentBean(@NonNls @Nonnull String beanName);

  /**
   * Returns all beans configured in the model and its dependencies.
   * @return all beans configured in the model and its dependencies.
   */
  @Nonnull
  Collection<SpringBaseBeanPointer> getAllDomBeans();

  @Nonnull
  Collection<SpringBaseBeanPointer> getAllDomBeans(boolean withDepenedencies);

  @Nonnull
  Set<String> getAllBeanNames(@Nonnull String beanName);

  boolean isNameDuplicated(@Nonnull String beanName);

  @Nonnull
  Collection<? extends SpringBaseBeanPointer> getAllCommonBeans(boolean withDepenedencies);

  @Nonnull
  default Collection<? extends SpringBaseBeanPointer> getAllCommonBeans() {
    return getAllCommonBeans(true);
  }

  @Nonnull
  Collection<? extends SpringBaseBeanPointer> getAllParentBeans();

  @Nonnull
  List<SpringBaseBeanPointer> findBeansByPsiClass(@Nonnull PsiClass psiClass);

  @Nonnull
  List<SpringBaseBeanPointer> findBeansByPsiClassWithInheritance(@Nonnull PsiClass psiClass);

  @Nonnull
  List<SpringBaseBeanPointer> findBeansByEffectivePsiClassWithInheritance(@Nonnull PsiClass psiClass);

  @Nonnull
  List<SpringBaseBeanPointer> getChildren(@Nonnull SpringBeanPointer parent);

  @Nonnull
  List<SpringBaseBeanPointer> getDescendants(@Nonnull CommonSpringBean context);

  @Nullable
  Module getModule();

  @Nonnull
  Collection<SpringBaseBeanPointer> getOwnBeans();

  @Nonnull
  List<SpringBaseBeanPointer> findQualifiedBeans(@Nonnull SpringQualifier qualifier);

  @Nonnull
  Collection<XmlTag> getCustomBeanCandidates(String id);

  @Nonnull
  Set<XmlFile> getConfigFiles();

  @Nonnull
  List<DomFileElement<Beans>> getRoots();

  List<? extends ComponentScan> getComponentScans();

  default boolean isImplicitConfiguration(@Nonnull PsiClass psiClass) {
    return false;
  }
}
