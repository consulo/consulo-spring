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
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class JamPsiMethodSpringBean extends JamPsiMemberSpringBean<PsiMethod> {

  @Override
  @Nonnull
  @JamPsiConnector
  public abstract PsiMethod getPsiElement();

  @Override
  @Nullable
  public String getBeanName() {
    return getPsiElement().getName();
  }

  @Override
  @Nullable
  public PsiClass getBeanClass() {
    PsiMethod method = getPsiElement();

    if (method != null) {
      PsiType returnType = method.getReturnType();
      if (returnType instanceof PsiClassType) {
        return ((PsiClassType)returnType).resolve();
      }
    }
    return null;
  }
}