package com.intellij.spring.impl.ide.references;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiLiteralExpression;
import com.intellij.java.language.psi.PsiModifierListOwner;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ProcessingContext;

import jakarta.annotation.Nonnull;

/**
 * User: Sergey.Vasiliev
 */
public class SpringQualifierNameReferenceProvider extends PsiReferenceProvider {

  @Nonnull
  public PsiReference[] getReferencesByElement(@Nonnull PsiElement element, @Nonnull ProcessingContext context) {
    if (element instanceof PsiLiteralExpression) {
      PsiLiteralExpression literalExpression = (PsiLiteralExpression)element;
      if (literalExpression.getValue() instanceof String) {
        PsiModifierListOwner psiModifierListOwner = PsiTreeUtil.getParentOfType(element, PsiModifierListOwner.class);
        if (psiModifierListOwner instanceof PsiClass) {
          // stereotype components (@Service, @Component, @Repository, etc.)
          final PsiAnnotation qualifierAnnotation = PsiTreeUtil.getParentOfType(element, PsiAnnotation.class);
          if (qualifierAnnotation != null) {
          return new PsiReference[] {new PsiReferenceBase<PsiElement>(element) {
            public PsiElement resolve() {
              return qualifierAnnotation;
            }

            public Object[] getVariants() {
              return EMPTY_ARRAY;
            }
          }};
          }
        }  else {
          // @Autowired components (method, field, parameter)
          return new PsiReference[]{new SpringQualifierReference(literalExpression)};
        }
      }
    }
    return PsiReference.EMPTY_ARRAY;
  }
}