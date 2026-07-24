package com.intellij.spring.impl.ide.references;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.patterns.PsiJavaPatterns;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.model.jam.utils.JamAnnotationTypeUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.pattern.PatternCondition;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceContributor;
import consulo.language.psi.PsiReferenceRegistrar;
import consulo.language.util.ModuleUtilCore;
import consulo.language.util.ProcessingContext;
import consulo.module.Module;
import consulo.util.dataholder.Key;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
@ExtensionImpl
public class SpringQualifierNameContributor extends PsiReferenceContributor {
  private static final Key<PsiElement> PSI_ELEMENT_KEY = Key.create("psiElement");


  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(
      PsiJavaPatterns.literalExpression().save(PSI_ELEMENT_KEY).annotationParam(
        PlatformPatterns.string().with(new PatternCondition<String>("customSpringAnno") {
          public boolean accepts(@Nonnull String s, ProcessingContext context) {
            PsiElement element = context.get(PSI_ELEMENT_KEY);

            Module module = ModuleUtilCore.findModuleForPsiElement(element);
            if (module != null) {
              List<PsiClass> classes = JamAnnotationTypeUtil.getQualifierAnnotationTypesWithChildren(module);
              for (PsiClass qualifierAnnoClass : classes) {
                if (s.equals(qualifierAnnoClass.getQualifiedName())) return true;
              }

            }
            return false;
          }
        }), "value"), new SpringQualifierNameReferenceProvider());
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return JavaLanguage.INSTANCE;
  }
}
