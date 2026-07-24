package com.intellij.spring.impl.ide.references;

import com.intellij.java.language.psi.PsiLiteralExpression;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.util.ProcessingContext;

import jakarta.annotation.Nonnull;

public class SpringBeanNamesReferenceProvider extends PsiReferenceProvider {
  final public static String[] METHODS = new String[] {"containsBean", "getBean", "isSingleton", "getType", "getAliases"};

  @Nonnull
  public PsiReference[] getReferencesByElement(@Nonnull PsiElement element, @Nonnull ProcessingContext context) {
    if (element instanceof PsiLiteralExpression) {
      PsiLiteralExpression literalExpression = (PsiLiteralExpression)element;
      if (literalExpression.getValue() instanceof String) {
        return new PsiReference[] {
          new SpringBeanReference(literalExpression)
        };
      }
    }
    return PsiReference.EMPTY_ARRAY;
  }

}
