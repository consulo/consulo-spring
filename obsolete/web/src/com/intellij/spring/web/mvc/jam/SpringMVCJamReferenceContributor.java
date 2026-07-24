package com.intellij.spring.web.mvc.jam;

import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

/**
 * @author Dmitry Avdeev
 */
public class SpringMVCJamReferenceContributor  extends PsiReferenceContributor {
  
  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(
        PsiJavaPatterns.literalExpression().with(new PatternCondition<PsiLiteralExpression>("foo") {
          @Override
          public boolean accepts(@NotNull PsiLiteralExpression psiLiteralExpression, ProcessingContext context) {
            PsiNameValuePair pair = PsiTreeUtil.getParentOfType(psiLiteralExpression, PsiNameValuePair.class);
            if (pair != null) {
              @NonNls String name = pair.getName();
              if (name == null || name.equals("value")) {
                String qualifiedName = ((PsiAnnotation)pair.getParent().getParent()).getQualifiedName();
                return qualifiedName != null &&
                       (qualifiedName.equals(SpringMVCRequestMapping.REQUEST_MAPPING) ||
                        qualifiedName.equals(SpringMVCModelAttribute.MODEL_ATTRIBUTE));
              }
            }
            return false;
          }
        }),
        new PsiReferenceProviderBase() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            return new PsiReference[] { PsiReferenceBase.createSelfReference(element, PsiTreeUtil.getParentOfType(element, PsiAnnotation.class)) };
          }
        });
  }
}
