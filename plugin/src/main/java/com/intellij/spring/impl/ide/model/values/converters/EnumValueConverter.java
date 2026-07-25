package com.intellij.spring.impl.ide.model.values.converters;

import com.intellij.java.language.psi.*;
import com.intellij.spring.impl.ide.model.values.PropertyValueConverter;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.util.lang.Pair;
import consulo.xml.dom.*;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * @author Taras Tielkes
 */
public class EnumValueConverter extends Converter<PsiField> implements CustomReferenceConverter {
  @Nonnull
  public static PsiReference[] createReferences(PsiType type, GenericDomValue genericDomValue, PsiElement element) {
    String stringValue = genericDomValue.getStringValue();
    if (type != null && type instanceof PsiClassType) {
      PsiClass psiClass = ((PsiClassType)type).resolve();
      if (psiClass != null) {
        return new PsiReference[]{createReference(psiClass, element, stringValue)};
      }
    }

    return PsiReference.EMPTY_ARRAY;
  }

  private static PsiReference createReference(final PsiClass psiClass, final PsiElement element, final String stringValue) {
    return new PsiReferenceBase<>(element, true) {
      @Override
      @RequiredReadAction
      public PsiElement resolve() {
        PsiField psiField = psiClass.findFieldByName(stringValue, false);
        if (psiField == null && !psiClass.isEnum()) {
          return element;
        }
        return psiField;
      }

      @Override
      @RequiredReadAction
      public Object[] getVariants() {
        return getFields(psiClass);
      }
    };
  }

  @Override
  public PsiField fromString(@Nullable String s, ConvertContext context) {
    return null;
  }

  @Override
  public String toString(@Nullable PsiField s, ConvertContext context) {
    return null;
  }

  @Nonnull
  @Override
  public PsiReference[] createReferences(GenericDomValue genericDomValue, PsiElement element, ConvertContext context) {
    Converter converter = genericDomValue.getConverter();
    while (converter instanceof WrappingConverter) {
      if (converter instanceof PropertyValueConverter valueConverter) {
        List<? extends PsiType> types = valueConverter.getValueTypes(genericDomValue);
        for (PsiType type : types) {
          PsiReference[] psiReferences = createReferences(type, genericDomValue, element);
          if (psiReferences.length > 0) {
            return psiReferences;
          }
        }
      }
      converter = ((WrappingConverter) converter).getConverter(genericDomValue);
    }
    return PsiReference.EMPTY_ARRAY;
  }

  public static class TypeCondition implements Predicate<Pair<PsiType, GenericDomValue>> {
    private final List<String> EXCLUDE_CLASSES = Arrays.asList(Boolean.class.getName(), Locale.class.getName());

    @Override
    public boolean test(Pair<PsiType, GenericDomValue> pair) {
      PsiType type = pair.getFirst();
      if (type != null && type instanceof PsiClassType) {
        if (EXCLUDE_CLASSES.contains(type.getCanonicalText())) return false;
        
        PsiClass psiClass = ((PsiClassType)type).resolve();
        if (psiClass != null) {
          if (psiClass.isEnum()) {
            return true;
          }
          for (PsiField psiField: psiClass.getFields()) {
            if (psiField.isStatic() && psiField.isPublic() && psiField.getType().equals(type)) {
                return true;
            }
          }
        }
      }
      return false;
    }
  }

  private static PsiField[] getFields(@Nonnull PsiClass psiClass) {
    List<PsiField> fields = new ArrayList<>();
    PsiField[] psiFields = psiClass.getFields();
    for (PsiField psiField : psiFields) {
      if (psiField.isStatic()
        && psiField.isPublic()
        && psiField.getType() instanceof PsiClassType classType
        && psiClass.equals(classType.resolve())) {
          fields.add(psiField);
      }
    }

    return fields.toArray(new PsiField[fields.size()]);
  }
}
