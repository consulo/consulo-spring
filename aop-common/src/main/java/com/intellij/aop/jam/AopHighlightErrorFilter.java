/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.jam;

import com.intellij.aop.psi.AopPointcutExpressionFile;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.HighlightErrorFilter;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiErrorElement;

import jakarta.annotation.Nonnull;

/**
 * @author peter
 */
@ExtensionImpl
public class AopHighlightErrorFilter extends HighlightErrorFilter {

  public boolean shouldHighlightErrorElement(@Nonnull PsiErrorElement element) {
    return !value(element);
  }

  public static boolean value(PsiErrorElement psiErrorElement) {
    PsiFile file = psiErrorElement.getContainingFile();
    if (file instanceof AopPointcutExpressionFile) {
      if (((AopPointcutExpressionFile)file).getAopModel().getAdvisedElementsSearcher().shouldSuppressErrors()) return true;
    }

    return false;
  }
}
