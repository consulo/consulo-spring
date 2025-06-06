/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.spring.impl.ide.model.jam;

import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.java.language.psi.PsiClass;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class JamPsiClassSpringBean extends JamPsiMemberSpringBean<PsiClass> {

  @Nonnull
  @JamPsiConnector
  public abstract PsiClass getPsiElement();

  @Nullable
  public String getBeanName() {
    return StringUtil.decapitalize(getPsiElement().getName());
  }

  @Nullable
  public PsiClass getBeanClass() {
    return getPsiElement();
  }
}