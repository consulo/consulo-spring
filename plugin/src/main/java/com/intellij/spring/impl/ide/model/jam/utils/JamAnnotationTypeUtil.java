package com.intellij.spring.impl.ide.model.jam.utils;

import com.intellij.java.indexing.search.searches.AnnotatedMembersSearch;
import com.intellij.java.language.psi.*;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.constants.SpringAnnotationsConstants;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.impl.ide.model.xml.beans.SpringPropertyDefinition;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.application.util.function.Processor;
import consulo.java.impl.model.annotations.AnnotationGenericValue;
import consulo.java.impl.model.annotations.AnnotationModelUtil;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.HashingStrategy;
import consulo.util.collection.Sets;
import consulo.util.dataholder.Key;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.*;

/**
 * User: Sergey.Vasiliev
 */
public class JamAnnotationTypeUtil {
  private static final Key<CachedValue<Collection<PsiClass>>> SPRING_MODULE_QUALIIFIER_ANNOTATIONS =
    new Key<CachedValue<Collection<PsiClass>>>("SPRING_MODULE_QUALIIFIER_ANNOTATIONS");
  private static final Key<CachedValue<Collection<PsiClass>>> SPRING_MODULE_COMPONENT_ANNOTATIONS =
    new Key<CachedValue<Collection<PsiClass>>>("SPRING_MODULE_COMPONENT_ANNOTATIONS");
  private static final HashingStrategy<PsiClass> HASHING_STRATEGY = new HashingStrategy<PsiClass>() {
    public int hashCode(final PsiClass object) {
      final String qualifiedName = object.getQualifiedName();
      return qualifiedName == null ? 0 : qualifiedName.hashCode();
    }

    public boolean equals(final PsiClass o1, final PsiClass o2) {
      return Comparing.equal(o1.getQualifiedName(), o2.getQualifiedName());
    }
  };

  private JamAnnotationTypeUtil() {
  }

  @Nonnull
  public static Collection<PsiClass> getAnnotationTypesWithChildren(final String annotationName, final Module module) {
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false);
    final PsiClass psiClass = JavaPsiFacade.getInstance(module.getProject()).findClass(annotationName, scope);

    final Set<PsiClass> classes = Sets.newHashSet(HASHING_STRATEGY);

    if (psiClass == null || !psiClass.isAnnotationType()) return Collections.emptyList();

    collectClassWithChildren(psiClass, classes, scope);

    return classes;
  }

  @Nonnull
  public static List<PsiClass> getQualifierAnnotationTypesWithChildren(final Module module) {
    final List<PsiClass> list = new ArrayList<PsiClass>();

    list.addAll(
      getAnnotationTypesWithChildren(module, SPRING_MODULE_QUALIIFIER_ANNOTATIONS, SpringAnnotationsConstants.QUALIFIER_ANNOTATION));
    list.addAll(getImplicitQualifierAnnotations(module)); // IDEADEV-27559

    return list;
  }

  // IDEADEV-27559
  public static List<PsiClass> getImplicitQualifierAnnotations(final consulo.module.Module module) {
    final List<PsiClass> list = new ArrayList<PsiClass>();

    final JavaPsiFacade facade = JavaPsiFacade.getInstance(module.getProject());
    final GlobalSearchScope moduleSearchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);

    final PsiClass configurerClass = facade.findClass(SpringAnnotationsConstants.CUSTOM_AUTOWIRE_CONFIGURER_CLASS, moduleSearchScope);

    if (configurerClass != null) {
      final SpringModel springModel = SpringManager.getInstance(module.getProject()).getCombinedModel(module);
      if(springModel != null) {
        final List<SpringBaseBeanPointer> beanPointers = springModel.findBeansByPsiClassWithInheritance(configurerClass);

        for (SpringBaseBeanPointer beanPointer : beanPointers) {
          final CommonSpringBean bean = beanPointer.getSpringBean();
          final SpringPropertyDefinition propertyDefinition = SpringUtils.findPropertyByName(bean, "customQualifierTypes");
          if (propertyDefinition != null) {
            for (String value : SpringUtils.getListOrSetValues(propertyDefinition)) {
              if (!StringUtil.isEmptyOrSpaces(value)) {
                final PsiClass psiClass = facade.findClass(value, moduleSearchScope);
                if (psiClass != null && psiClass.isAnnotationType()) {
                  list.add(psiClass);
                }
              }
            }
          }
        }
      }
    }
    return list;
  }

  @Nonnull
  public static List<String> getUserDefinedCustomComponentAnnotations(final consulo.module.Module module) {
    List<String> annotations = getCustomComponentAnnotations(module);
    for (String annotation : SpringAnnotationsConstants.SPRING_COMPONENT_ANNOTATIONS) {
      annotations.remove(annotation);
    }
    return annotations;
  }

  @Nonnull
  public static List<String> getCustomComponentAnnotations(final consulo.module.Module module) {
    final Collection<PsiClass> classes =
      getAnnotationTypesWithChildren(module, SPRING_MODULE_COMPONENT_ANNOTATIONS, SpringAnnotationsConstants.COMPONENT_ANNOTATION);
    return ContainerUtil.mapNotNull(classes, psiClass -> psiClass.getQualifiedName());
  }

  private static Collection<PsiClass> getAnnotationTypesWithChildren(@Nullable final Module module,
                                                                     final Key<CachedValue<Collection<PsiClass>>> key,
                                                                     final String annotationName) {
    if (module == null) return Collections.emptyList();

    CachedValue<Collection<PsiClass>> cachedValue = module.getUserData(key);
    if (cachedValue == null) {
      cachedValue = CachedValuesManager.getManager(module.getProject())
        .createCachedValue(new CachedValueProvider<Collection<PsiClass>>() {
          public Result<Collection<PsiClass>> compute() {
            final Collection<PsiClass> classes = getAnnotationTypesWithChildren(annotationName, module);
            return new Result<Collection<PsiClass>>(classes, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
          }
        }, false);

      module.putUserData(key, cachedValue);
    }
    final Collection<PsiClass> classes = cachedValue.getValue();

    return classes == null ? Collections.<PsiClass>emptyList() : classes;
  }

  private static void collectClassWithChildren(final PsiClass psiClass, final Set<PsiClass> classes, final GlobalSearchScope scope) {
    classes.add(psiClass);

    for (PsiClass aClass : getChildren(psiClass, scope)) {
      if (!classes.contains(aClass)) {
        collectClassWithChildren(aClass, classes, scope);
      }
    }
  }

  private static Set<PsiClass> getChildren(final PsiClass psiClass, final GlobalSearchScope scope) {
    if (!isAcceptedFor(psiClass, ElementType.ANNOTATION_TYPE, ElementType.TYPE)) return Collections.emptySet();

    final String name = psiClass.getQualifiedName();
    if (name == null) return Collections.emptySet();

    final Set<PsiClass> result = Sets.newHashSet(HASHING_STRATEGY);

    AnnotatedMembersSearch.search(psiClass, scope).forEach(new Processor<PsiMember>() {
      public boolean process(final PsiMember psiMember) {
        if (psiMember instanceof PsiClass && ((PsiClass)psiMember).isAnnotationType()) {
          result.add((PsiClass)psiMember);
        }
        return true;
      }
    });

    return result;
  }

  public static boolean isAcceptedFor(final PsiClass psiClass, final ElementType... elementTypes) {
    final PsiModifierList modifierList = psiClass.getModifierList();
    if (modifierList != null) {
      final PsiAnnotation psiAnnotation = modifierList.findAnnotation(Target.class.getName());
      if (psiAnnotation != null) {
        final List<AnnotationGenericValue<ElementType>> values =
          AnnotationModelUtil.getEnumArrayValue(psiAnnotation, "value", ElementType.class);
        for (AnnotationGenericValue<ElementType> value : values) {
          for (ElementType elementType : elementTypes) {
            if (elementType.equals(value.getValue())) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }
}
