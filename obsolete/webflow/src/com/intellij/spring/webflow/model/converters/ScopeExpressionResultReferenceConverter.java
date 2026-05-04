package com.intellij.spring.webflow.model.converters;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import consulo.ide.impl.psi.util.ReferenceSetBase;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

public class ScopeExpressionResultReferenceConverter implements CustomReferenceConverter {

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue genericDomValue, final PsiElement element, final ConvertContext context) {
    final ScopeReferenceSet referenceSet = new ScopeReferenceSet(element, genericDomValue);

    return referenceSet.getPsiReferences();
  }

  private static class ScopeReferenceSet extends ReferenceSetBase {
    private final GenericDomValue myDomValue;

    public ScopeReferenceSet(final PsiElement element, final GenericDomValue genericDomValue) {
      super(element, 0);
      myDomValue = genericDomValue;
    }

    @NotNull
    protected PsiReference createReference(final TextRange range, final int index) {
      switch (index) {
        case 0:
           return new WebflowScopeReference(getElement(), range, myDomValue);
        case 1:
          return new WebflowScopeVariableReference(getElement(), range, myDomValue, getReference(0).getCanonicalText());
        default:
          return new UnresolvableReference(range);
      }
    }

    private class UnresolvableReference extends PsiReferenceBase<PsiElement> {
      public UnresolvableReference(final TextRange range) {
        super(ScopeReferenceSet.this.getElement(), range, false);
      }

      public PsiElement resolve() {
        return null;
      }

      public Object[] getVariants() {
        return new Object[0];
      }
    }

  }

}
