package com.intellij.spring.impl.ide.java.providers;

import com.intellij.aop.jam.AopConstants;
import com.intellij.java.language.codeInsight.AnnotationUtil;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifierListOwner;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.constants.SpringAnnotationsConstants;
import com.intellij.spring.impl.ide.model.SpringUtils;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.ImplicitUsageProvider;
import consulo.language.psi.PsiElement;

import java.util.List;

@ExtensionImpl
public class SpringImplicitUsageProvider implements ImplicitUsageProvider {
  private static final

  List<String> WUSED_SYMBOL_ANNOTATIONS = List.of(SpringAnnotationsConstants.AUTOWIRED_ANNOTATION,
                                                  SpringAnnotationsConstants.JAVAX_RESOURCE_ANNOTATION,
                                                  SpringAnnotationsConstants.JAVAX_PRE_DESTROY_ANNOTATION,
                                                  SpringAnnotationsConstants.JAVAX_POST_CONSTRUCT_ANNOTATION,
                                                  SpringAnnotationsConstants.JAKARTA_RESOURCE_ANNOTATION,
                                                  SpringAnnotationsConstants.JAKARTA_PRE_DESTROY_ANNOTATION,
                                                  SpringAnnotationsConstants.JAKARTA_POST_CONSTRUCT_ANNOTATION,
                                                  SpringAnnotationsConstants.VALUE_ANNOTATION,
                                                  "org.springframework.jmx.export.annotation.ManagedOperation",
                                                  "org.springframework.jmx.export.annotation.ManagedAttribute",
                                                  AopConstants.BEFORE_ANNO,
                                                  AopConstants.AFTER_ANNO,
                                                  AopConstants.AROUND_ANNO,
                                                  AopConstants.AFTER_RETURNING_ANNO,
                                                  AopConstants.AFTER_THROWING_ANNO);

  @Override
  public boolean isImplicitUsage(PsiElement element) {
    return isImplicitWrite(element) || isBeanConstructor(element);
  }

  @Override
  public boolean isImplicitWrite(PsiElement element) {
    return element instanceof PsiModifierListOwner &&
      AnnotationUtil.isAnnotated((PsiModifierListOwner)element, WUSED_SYMBOL_ANNOTATIONS, 0);
  }

  private static boolean isBeanConstructor(PsiElement element) {
    if (element instanceof PsiMethod && ((PsiMethod)element).isConstructor()) {
      PsiClass containingClass = ((PsiMethod)element).getContainingClass();
      if (containingClass == null) {
        return false;
      }
      SpringModel springModel = SpringUtils.getModelByPsiElement(element);
      return springModel != null && !springModel.findBeansByPsiClass(containingClass).isEmpty();
    }
    return false;
  }
}