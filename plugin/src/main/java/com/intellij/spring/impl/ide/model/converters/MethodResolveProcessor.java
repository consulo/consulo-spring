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

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.language.impl.psi.scope.ElementClassHint;
import com.intellij.java.language.impl.psi.scope.NameHint;
import com.intellij.java.language.psi.PsiMethod;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.util.dataholder.Key;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class MethodResolveProcessor implements PsiScopeProcessor, ElementClassHint {

  private final NameHint myNameHint;
  private final List<PsiMethod> myMethods = new ArrayList<>();

  public MethodResolveProcessor() {
    myNameHint = null;
  }

  public MethodResolveProcessor(String name) {
    myNameHint = state -> name;
  }

  public PsiMethod[] getMethods() {
    return myMethods.toArray(new PsiMethod[myMethods.size()]);
  }

  @Override
  public boolean execute(PsiElement element, ResolveState state) {
    myMethods.add((PsiMethod)element);
    return true;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getHint(Key<T> hintKey) {
    if (hintKey == ElementClassHint.KEY) {
      return (T)this;
    }
    if (hintKey == NameHint.KEY) {
      return (T)myNameHint;
    }
    return null;
  }

  @Override
  public void handleEvent(Event event, Object associated) {
  }

  @Override
  public boolean shouldProcess(ElementClassHint.DeclarationKind kind) {
    return kind == ElementClassHint.DeclarationKind.METHOD;
  }
}
