package com.intellij.spring.impl.ide.model.values.converters;

import com.intellij.java.language.psi.*;
import com.intellij.spring.impl.ide.model.values.PropertyValueConverter;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.util.lang.Pair;
import consulo.util.lang.function.Condition;
import consulo.xml.dom.*;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
    return new PsiReferenceBase<PsiElement>(element, true) {
      public PsiElement resolve() {
        PsiField psiField = psiClass.findFieldByName(stringValue, false);
        if (psiField == null && !psiClass.isEnum()) {
          return element;
        }
        return psiField;
      }

      public Object[] getVariants() {
        return getFields(psiClass);
      }
    };
  }

  public PsiField fromString(@Nullable @NonNls String s, ConvertContext context) {
    return null;
  }

  public String toString(@Nullable PsiField s, ConvertContext context) {
    return null;
  }

  @Nonnull
  public PsiReference[] createReferences(GenericDomValue genericDomValue, PsiElement element, ConvertContext context) {
    Converter converter = genericDomValue.getConverter();
    while (converter instanceof WrappingConverter) {
      if (converter instanceof PropertyValueConverter) {
        List<? extends PsiType> types = ((PropertyValueConverter)converter).getValueTypes(genericDomValue);
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

  public static class TypeCondition implements Condition<Pair<PsiType, GenericDomValue>> {
    private final List<String> EXCLUDE_CLASSES = Arrays.asList(Boolean.class.getName(), Locale.class.getName());

    public boolean value(Pair<PsiType, GenericDomValue> pair) {

      PsiType type = pair.getFirst();
      if (type != null && type instanceof PsiClassType) {
        if (EXCLUDE_CLASSES.contains(type.getCanonicalText())) return false;
        
        PsiClass psiClass = ((PsiClassType)type).resolve();
        if (psiClass != null) {
          if (psiClass.isEnum()) {
            return true;
          }
          for (PsiField psiField: psiClass.getFields()) {
            if (psiField.hasModifierProperty(PsiModifier.STATIC) &&
                psiField.hasModifierProperty(PsiModifier.PUBLIC) &&
                psiField.getType().equals(type)) {
                return true;
            }
          }
        }
      }
      return false;
    }
  }

  private static PsiField[] getFields(@Nonnull PsiClass psiClass) {
    ArrayList<PsiField> fields = new ArrayList<PsiField>();
    PsiField[] psiFields = psiClass.getFields();
    for (PsiField psiField : psiFields) {
      if (psiField.hasModifierProperty(PsiModifier.STATIC) && psiField.hasModifierProperty(PsiModifier.PUBLIC)) {
        PsiType type = psiField.getType();
        if (type instanceof PsiClassType) {
          PsiClass typeClass = ((PsiClassType)type).resolve();
          if (typeClass != null && typeClass.equals(psiClass)) {
            fields.add(psiField);
          }
        }
      }
    }

    return fields.toArray(new PsiField[fields.size()]);
  }
}
