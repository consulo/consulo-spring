package com.intellij.spring.web.mvc.jam;

import com.intellij.jam.JamElement;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.*;
import com.intellij.psi.*;
import consulo.ide.impl.idea.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public abstract class SpringMVCRequestMapping<T extends PsiMember> implements JamElement {

  @NonNls public static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
  private static final JamStringAttributeMeta.Collection<String> URLS_META = JamAttributeMeta.collectionString("value");

  static final JamAnnotationMeta REQUEST_MAPPING_ANNO_META = new JamAnnotationMeta(REQUEST_MAPPING).
    addAttribute(URLS_META);

  public List<JamStringAttributeElement<String>> getMappingUrls() {
    return REQUEST_MAPPING_ANNO_META.getAttribute(getPsiElement(), URLS_META);
  }

  public List<String> getUrls() {
    return ContainerUtil.mapNotNull(getMappingUrls(), new NullableFunction<JamStringAttributeElement<String>, String>() {
      public String fun(final JamStringAttributeElement<String> stringAnnotationGenericValue) {
        return stringAnnotationGenericValue.getStringValue();
      }
    });
  }

  public String getName() {
    final List<String> urls = getUrls();
    return urls.isEmpty() ? "" : urls.get(0);
  }

  public abstract List<SpringMVCModelAttribute> getModelAttributes();

  @JamPsiConnector
  public abstract T getPsiElement();

  @Nullable
  public PsiAnnotation getAnnotation() {
    return REQUEST_MAPPING_ANNO_META.getAnnotation(getPsiElement());
  }

  public abstract static class ClassMapping extends SpringMVCRequestMapping<PsiClass> {
    private static final JamChildrenQuery<SpringMVCModelAttribute> METHOD_ATTRIBUTES_QUERY =
      JamChildrenQuery.annotatedMethods(SpringMVCModelAttribute.MODEL_ATTRIBUTE_META, SpringMVCModelAttribute.class);

    public static final JamClassMeta<ClassMapping> META = new JamClassMeta<ClassMapping>(ClassMapping.class).
      addAnnotation(REQUEST_MAPPING_ANNO_META).
      addChildrenQuery(METHOD_ATTRIBUTES_QUERY);

    public List<SpringMVCModelAttribute> getModelAttributes() {
      return METHOD_ATTRIBUTES_QUERY.findChildren(PsiRef.real(getPsiElement()));
    }
  }

  public abstract static class MethodMapping extends SpringMVCRequestMapping<PsiMethod> {

    private static final JamChildrenQuery<SpringMVCModelAttribute> PARAMETER_ATTRIBUTES_QUERY =
      JamChildrenQuery.annotatedParameters(SpringMVCModelAttribute.MODEL_ATTRIBUTE_META, SpringMVCModelAttribute.class);

    public static final JamMethodMeta<MethodMapping> META = new JamMethodMeta<MethodMapping>(MethodMapping.class).
      addAnnotation(REQUEST_MAPPING_ANNO_META).
      addChildrenQuery(PARAMETER_ATTRIBUTES_QUERY);

    public List<SpringMVCModelAttribute> getModelAttributes() {
      return PARAMETER_ATTRIBUTES_QUERY.findChildren(PsiRef.real(getPsiElement()));
    }

  }

}
