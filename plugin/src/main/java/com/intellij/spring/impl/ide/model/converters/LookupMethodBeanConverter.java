/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.xml.beans.LookupMethod;
import consulo.xml.dom.ConvertContext;

import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class LookupMethodBeanConverter extends SpringBeanResolveConverter {

  @Nullable
  public List<PsiClassType> getRequiredClasses(ConvertContext context) {
    LookupMethod lookupMethod = (LookupMethod)context.getInvocationElement().getParent();
    assert lookupMethod != null;
    PsiMethod psiMethod = lookupMethod.getName().getValue();
    if (psiMethod != null) {
      PsiType returnType = psiMethod.getReturnType();
      if (returnType instanceof PsiClassType) {
        return  Collections.singletonList((PsiClassType)returnType);
      }
    }
    return null;
  }
}