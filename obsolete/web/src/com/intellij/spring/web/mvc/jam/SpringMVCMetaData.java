package com.intellij.spring.web.mvc.jam;

import com.intellij.jam.JamPsiAnnotationParameterMetaData;
import com.intellij.jam.JamService;
import com.intellij.javaee.model.common.AnnotationMetaDataFilter;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.meta.MetaDataContributor;
import com.intellij.psi.meta.MetaDataRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Avdeev
 */
public class SpringMVCMetaData implements MetaDataContributor {

  public void contributeMetaData(final MetaDataRegistrar registrar) {
    registrar.registerMetaData(new AnnotationMetaDataFilter(SpringMVCRequestMapping.REQUEST_MAPPING), RequestMappingMeta.class);

    registrar.registerMetaData(new AnnotationMetaDataFilter(SpringMVCModelAttribute.MODEL_ATTRIBUTE), ModelAttributeMeta.class);
  }

  public static class RequestMappingMeta extends JamPsiAnnotationParameterMetaData<SpringMVCRequestMapping> {

    @NotNull
    protected SpringMVCRequestMapping getModelElement(final PsiModifierListOwner owner, final PsiAnnotation annotation) {
      return JamService.getJamService(owner.getProject()).getJamElement(SpringMVCRequestMapping.class, owner);
    }
  }

  public static class ModelAttributeMeta extends JamPsiAnnotationParameterMetaData<SpringMVCModelAttribute> {
    @NotNull
    protected SpringMVCModelAttribute getModelElement(final PsiModifierListOwner owner, final PsiAnnotation annotation) {
      return JamService.getJamService(owner.getProject()).getJamElement(SpringMVCModelAttribute.class, owner);
    }
  }
}
