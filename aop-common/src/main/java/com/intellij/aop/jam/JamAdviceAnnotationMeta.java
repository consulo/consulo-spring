/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.jam;

import com.intellij.jam.reflect.JamAnnotationArchetype;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.java.language.psi.PsiMethod;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public abstract class JamAdviceAnnotationMeta extends JamAnnotationMeta {

  protected JamAdviceAnnotationMeta(@Nonnull @NonNls String annoName, @Nullable JamAnnotationArchetype archetype) {
    super(annoName, archetype);
  }

  public abstract AopAdviceImpl createAdvice(PsiMethod method);

  @Override
  public JamAdviceAnnotationMeta addAttribute(JamAttributeMeta<?> attributeMeta) {
    super.addAttribute(attributeMeta);
    return this;
  }
}
