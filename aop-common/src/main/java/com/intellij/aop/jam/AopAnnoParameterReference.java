/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.jam;

import com.intellij.aop.LocalAopModel;
import com.intellij.jam.model.util.JamCommonUtil;
import com.intellij.java.language.psi.PsiAnnotationMemberValue;
import com.intellij.java.language.psi.PsiLiteral;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiParameter;
import consulo.annotation.access.RequiredReadAction;
import consulo.aop.localize.AopLocalize;
import consulo.language.psi.EmptyResolveMessageProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;

/**
 * @author peter
*/
public class AopAnnoParameterReference extends PsiReferenceBase<PsiAnnotationMemberValue> implements EmptyResolveMessageProvider {
  private final PsiMethod myMethod;

  public AopAnnoParameterReference(final PsiLiteral element) {
    super(element);
    myMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
  }

  @Override
  @RequiredReadAction
  public final PsiElement resolve() {
    return AopAdviceImpl.findParameter(myMethod, getCanonicalText());
  }

  @Override
  @RequiredReadAction
  public Object[] getVariants() {
    final PsiParameter[] parameters = myMethod.getParameterList().getParameters();
    return parameters.length > 0 && LocalAopModel.isJoinPointParamer(parameters[0]) ? ArrayUtil.remove(parameters, 0) : parameters;
  }

  @Nonnull
  @Override
  public final String getCanonicalText() {
    return StringUtil.notNullize(JamCommonUtil.getObjectValue(getElement(), String.class));
  }

  @Nonnull
  @Override
  public LocalizeValue buildUnresolvedMessage(@Nonnull String ref) {
    return AopLocalize.errorCannotResolveParameter(ref);
  }
}
