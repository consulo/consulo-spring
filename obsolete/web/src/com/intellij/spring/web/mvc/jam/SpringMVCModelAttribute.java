package com.intellij.spring.web.mvc.jam;

import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Avdeev
 */
public abstract class SpringMVCModelAttribute implements JamElement {
  @NonNls public static final String MODEL_ATTRIBUTE = "org.springframework.web.bind.annotation.ModelAttribute";

  private static final JamStringAttributeMeta.Single<String> VALUE_META = JamAttributeMeta.singleString("value");

  public static final JamAnnotationMeta MODEL_ATTRIBUTE_META = new JamAnnotationMeta(MODEL_ATTRIBUTE).addAttribute(VALUE_META);

  @Nullable
  public String getName() {
    final String value = MODEL_ATTRIBUTE_META.getAttribute(getPsiElement(), VALUE_META).getStringValue();
    if (value != null) {
      return value;
    }
    final PsiType psiType = getType();
    if (psiType instanceof PsiClassType) {
      final PsiClass psiClass = ((PsiClassType)psiType).resolve();
      if (psiClass != null) {
        return StringUtil.decapitalize(psiClass.getName());
      }
    }
    return null;
  }

  @Nullable
  public PsiType getType() {
    final PsiModifierListOwner psi = getPsiElement();
    if (psi instanceof PsiMethod) {
      return ((PsiMethod)psi).getReturnType();
    }
    if (psi instanceof PsiParameter) {
      return ((PsiParameter)psi).getType();
    }
    return null;
  }

  @NotNull
  @JamPsiConnector
  public abstract PsiModifierListOwner getPsiElement();

  @Nullable
  public PsiAnnotation getAnnotation() {
    return MODEL_ATTRIBUTE_META.getAnnotation(getPsiElement());
  }

}
