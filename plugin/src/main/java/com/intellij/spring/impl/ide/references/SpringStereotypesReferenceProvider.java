package com.intellij.spring.impl.ide.references;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiLiteralExpression;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;

import javax.annotation.Nonnull;

/**
 * User: Sergey.Vasiliev
 */
public class SpringStereotypesReferenceProvider extends PsiReferenceProvider {

  @Nonnull
  public PsiReference[] getReferencesByElement(@Nonnull PsiElement element, @Nonnull final ProcessingContext context) {
    if (element instanceof PsiLiteralExpression) {
      final PsiLiteralExpression literalExpression = (PsiLiteralExpression)element;
      if (literalExpression.getValue() instanceof String) {

        final PsiAnnotation psiAnnotation = PsiTreeUtil.getParentOfType(element, PsiAnnotation.class);

        if (psiAnnotation != null) {
          return new PsiReference[]{PsiReferenceBase.createSelfReference(element, psiAnnotation)};
        }
      }
    }
    return PsiReference.EMPTY_ARRAY;
  }
}
