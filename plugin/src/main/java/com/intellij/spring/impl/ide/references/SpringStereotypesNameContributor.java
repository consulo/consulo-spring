package com.intellij.spring.impl.ide.references;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.patterns.PsiJavaPatterns;
import com.intellij.spring.impl.ide.constants.SpringAnnotationsConstants;
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
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
@ExtensionImpl
public class SpringStereotypesNameContributor extends PsiReferenceContributor {
  private static final Key<PsiElement> PSI_ELEMENT_KEY = Key.create("PSI_ELEMENT_KEY");

  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(PsiJavaPatterns.literalExpression().save(PSI_ELEMENT_KEY).annotationParam(
      PlatformPatterns.string().with(new PatternCondition<String>("stereotypeAnnos") {
        public boolean accepts(@Nonnull String s, ProcessingContext context) {
          PsiElement element = context.get(PSI_ELEMENT_KEY);

          Module module = ModuleUtilCore.findModuleForPsiElement(element);

          return getAnnotaionNames(module).contains(s);
        }
      }), "value"), new SpringStereotypesReferenceProvider());
  }

  private static List<String> getAnnotaionNames(@Nullable Module module) {
    List<String> annos = new ArrayList<String>();

    if (module == null) {
      annos.add(SpringAnnotationsConstants.COMPONENT_ANNOTATION);
      annos.add(SpringAnnotationsConstants.CONTROLLER_ANNOTATION);
      annos.add(SpringAnnotationsConstants.SERVICE_ANNOTATION);
      annos.add(SpringAnnotationsConstants.REPOSITORY_ANNOTATION);
    }
    else {
      annos.addAll(JamAnnotationTypeUtil.getCustomComponentAnnotations(module));
    }

    return annos;
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return JavaLanguage.INSTANCE;
  }
}
