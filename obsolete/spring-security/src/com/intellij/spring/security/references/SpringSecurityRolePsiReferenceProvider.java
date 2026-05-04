package com.intellij.spring.security.references;

import com.intellij.openapi.module.Module;
import consulo.ide.impl.idea.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public abstract class SpringSecurityRolePsiReferenceProvider<T extends PsiElement> extends PsiReferenceProviderBase {

  public static class XmlAttributeValueProvider extends SpringSecurityRolePsiReferenceProvider {
    @Override
    protected String getStringValue(PsiElement element) {
      if (element instanceof XmlAttributeValue) {
        return ((XmlAttributeValue)element).getValue();
      }
      return null;
    }
  }
  public static class PsiLiteralExpressionProvider extends SpringSecurityRolePsiReferenceProvider {
    @Override
    protected String getStringValue(PsiElement element) {
      if (element instanceof PsiLiteralExpression) {
        Object value = ((PsiLiteralExpression)element).getValue();
        return value instanceof String ? (String)value : null;
      }
      return null;
    }
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {
    final String value = getStringValue(element);

    return getSecurityRolesReferences(element, value);
  }

  public static PsiReference[] getSecurityRolesReferences(@NotNull PsiElement element, @Nullable String value) {
    Module module = ModuleUtil.findModuleForPsiElement(element);
    if (module != null && !StringUtil.isEmptyOrSpaces(value)) {
        List<PsiReference> references = new ArrayList<PsiReference>();
        for (String token : StringUtil.tokenize(value, ",")) {
          if (!StringUtil.isEmptyOrSpaces(token)) {
            references.add(getReference(element, token, module));
          }
        }
        return ArrayUtil.toObjectArray(references, PsiReference.class);
    }
    return PsiReference.EMPTY_ARRAY;
  }

  @Nullable
  protected abstract String getStringValue(PsiElement element);

  private static PsiReference getReference(PsiElement element, String token, Module module) {
    return new SpringSecurityRolePsiReference(element, token, module);
  }
}