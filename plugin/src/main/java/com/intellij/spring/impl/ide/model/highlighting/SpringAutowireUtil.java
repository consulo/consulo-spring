/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.java.language.codeInsight.AnnotationUtil;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PropertyUtil;
import com.intellij.java.language.psi.util.PsiUtil;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.constants.SpringAnnotationsConstants;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.jam.qualifiers.SpringJamQualifier;
import com.intellij.spring.impl.ide.model.jam.utils.JamAnnotationTypeUtil;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.*;
import consulo.java.impl.model.annotations.AnnotationModelUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.util.*;

public class SpringAutowireUtil {
  private SpringAutowireUtil() {
  }

  public static Map<PsiMethod, Collection<SpringBaseBeanPointer>> getByTypeAutowiredProperties(SpringBean springBean,
                                                                                               SpringModel model) {
    Map<PsiMethod, Collection<SpringBaseBeanPointer>> autowiredMap = new HashMap<PsiMethod, Collection<SpringBaseBeanPointer>>();
    PsiClass beanClass = springBean.getBeanClass();
    if (beanClass != null) {
      if (model != null && isByTypeAutowired(springBean)) {
        for (PsiMethod psiMethod : beanClass.getAllMethods()) {
          if (isPropertyAutowired(psiMethod, springBean)) {
            PsiParameter parameter = psiMethod.getParameterList().getParameters()[0];
            Collection<SpringBaseBeanPointer> list = autowireByType(model, parameter.getType());
            if (list.size() > 0) {
              autowiredMap.put(psiMethod, list);
            }
          }
        }
      }
    }

    return autowiredMap;
  }

  @Nonnull
  private static List<SpringBaseBeanPointer> excludeAutowireCandidates(@Nullable List<SpringBaseBeanPointer> beans) {
    List<SpringBaseBeanPointer> beanPointers = new ArrayList<SpringBaseBeanPointer>();
    if (beans != null) {
      for (SpringBaseBeanPointer bean : beans) {
        if (isAutowireCandidate(bean.getSpringBean())) {
          beanPointers.add(bean);
        }
      }
    }
    return beanPointers;
  }

  @Nonnull
  public static List<SpringBaseBeanPointer> excludeAutowireCandidatesForCommonBeans(@Nullable List<SpringBaseBeanPointer> beans) {
    List<SpringBaseBeanPointer> list = new ArrayList<SpringBaseBeanPointer>();
    if (beans != null) {
      for (SpringBaseBeanPointer beanPointer : beans) {
        CommonSpringBean commonSpringBean = beanPointer.getSpringBean();
        if (commonSpringBean instanceof DomSpringBean) {
          if (isAutowireCandidate(commonSpringBean)) {
            list.add(beanPointer);
          }
        }
        else {
          list.add(beanPointer);
        }
      }
    }
    return list;
  }

  private static boolean isAutowireCandidate(CommonSpringBean springBean) {
    if (!(springBean instanceof SpringBean)) return true;
    Boolean autoWireCandidate = ((SpringBean)springBean).getAutowireCandidate().getValue();

    return autoWireCandidate == null || autoWireCandidate.booleanValue();

  }

  public static Map<PsiType, Collection<SpringBaseBeanPointer>> getConstructorAutowiredProperties(SpringBean springBean,
                                                                                                  SpringModel model) {
    Map<PsiType, Collection<SpringBaseBeanPointer>> autowiredMap = new HashMap<PsiType, Collection<SpringBaseBeanPointer>>();
    PsiClass beanClass = springBean.getBeanClass();
    if (beanClass != null) {
      if (isConstructorAutowire(springBean)) {
        boolean instantiatedByFactory = SpringConstructorArgResolveUtil.isInstantiatedByFactory(springBean);

        PsiMethod checkedMethod = instantiatedByFactory
          ? springBean.getFactoryMethod().getValue()
          : SpringConstructorArgResolveUtil.getSpringBeanConstructor(springBean, model);

        if (checkedMethod != null) {
          List<ConstructorArg> list = SpringUtils.getConstructorArgs(springBean);
          Map<Integer, ConstructorArg> indexedArgs = SpringConstructorArgResolveUtil.getIndexedConstructorArgs(list);
          PsiParameter[] parameters = checkedMethod.getParameterList().getParameters();
          for (int i = 0; i < parameters.length; i++) {
            PsiParameter parameter = parameters[i];
            if (!SpringConstructorArgResolveUtil.acceptParameter(parameter, list, indexedArgs, i)) {
              PsiType psiType = parameter.getType();
              Collection<SpringBaseBeanPointer> springBeans = autowireByType(model, psiType);
              if (springBeans.size() > 0) {
                autowiredMap.put(psiType, springBeans);
              }
            }
          }
        }
      }
    }

    return autowiredMap;
  }

  private static boolean isPropertyDefined(SpringBean springBean, String propertyName) {
    for (SpringPropertyDefinition springProperty : springBean.getAllProperties()) {
      if (propertyName.equals(springProperty.getPropertyName())) {
        return true;
      }
    }
    return false;
  }

  public static Map<PsiMethod, SpringBaseBeanPointer> getByNameAutowiredProperties(SpringBean springBean) {
    Map<PsiMethod, SpringBaseBeanPointer> autowiredMap = new HashMap<PsiMethod, SpringBaseBeanPointer>();
    PsiClass beanClass = springBean.getBeanClass();
    if (beanClass != null) {
      SpringModel model = SpringUtils.getSpringModel(springBean);
      if (isByNameAutowired(springBean)) {
        for (PsiMethod psiMethod : beanClass.getAllMethods()) {
          if (PropertyUtil.isSimplePropertySetter(psiMethod)) {
            PsiParameter parameter = psiMethod.getParameterList().getParameters()[0];
            Collection<SpringBaseBeanPointer> list = autowireByType(model, parameter.getType());

            String propertyName = PropertyUtil.getPropertyNameBySetter(psiMethod);
            for (SpringBaseBeanPointer bean : list) {
              if (SpringUtils.getAllBeanNames(bean.getSpringBean()).contains(propertyName)) {
                autowiredMap.put(psiMethod, bean);
              }
            }
          }
        }
      }
    }

    return autowiredMap;
  }

  private static boolean isPropertyAutowired(PsiMethod psiMethod, SpringBean springBean) {
    if (PropertyUtil.isSimplePropertySetter(psiMethod)) {
      PsiParameter parameter = psiMethod.getParameterList().getParameters()[0];
      PsiType psiType = parameter.getType();
      if (psiType instanceof PsiClassType) {
        PsiClass psiClass = ((PsiClassType)psiType).resolve();

        return psiClass != null && !isPropertyDefined(springBean, PropertyUtil.getPropertyNameBySetter(psiMethod));
      }
    }
    return false;
  }

  public static boolean isByTypeAutowired(SpringBean springBean) {
    return springBean.getBeanAutowire().equals(Autowire.BY_TYPE);
  }

  public static boolean isByNameAutowired(SpringBean springBean) {
    return springBean.getBeanAutowire().equals(Autowire.BY_NAME);
  }

  public static boolean isConstructorAutowire(SpringBean springBean) {
    return springBean.getBeanAutowire().equals(Autowire.CONSTRUCTOR);
  }

  public static Map<PsiMember, List<SpringBaseBeanPointer>> getAutowireAnnotationProperties(CommonSpringBean springBean,
                                                                                            @Nonnull SpringModel model) {
    Map<PsiMember, List<SpringBaseBeanPointer>> map = new HashMap<PsiMember, List<SpringBaseBeanPointer>>();
    PsiClass beanClass = springBean.getBeanClass();
    if (beanClass != null) {
      for (PsiMethod psiMethod : getAnnotatedAutowiredMethods(beanClass)) {
        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
          PsiAnnotation psiAnnotation = getQualifiedAnnotation(parameter, model.getModule());
          if (psiAnnotation != null) {
            addAutowiredBeans(map, psiMethod, getQualifiedBeans(psiAnnotation, model));
          }
          else {
            addAutowiredBeans(map, psiMethod, SpringUtils.getBeansByType(parameter.getType(), model));
          }
        }
      }

      for (PsiField psiField : getAnnotatedAutowiredFields(beanClass)) {
        PsiAnnotation psiAnnotation = getQualifiedAnnotation(psiField, model.getModule());
        if (psiAnnotation != null) {
          addAutowiredBeans(map, psiField, getQualifiedBeans(psiAnnotation, model));
        }
        else {
          addAutowiredBeans(map, psiField, SpringUtils.getBeansByType(psiField.getType(), model));
        }
      }
    }

    return map;
  }

  private static void addAutowiredBeans(Map<PsiMember, List<SpringBaseBeanPointer>> map, PsiMember psiMember,

                                        List<SpringBaseBeanPointer> beans) {
    List<SpringBaseBeanPointer> list = excludeAutowireCandidatesForCommonBeans(beans);
    if (list.size() > 0) {
      if (!map.containsKey(psiMember)) {
        map.put(psiMember, list);
      }
      else {
        map.get(psiMember).addAll(list);
      }
    }
  }

  @Nonnull
  public static List<SpringBaseBeanPointer> getQualifiedBeans(@Nonnull PsiAnnotation psiAnnotation,
                                                              @Nullable SpringModel model) {
    //3.11.3. Fine-tuning annotation-based autowiring with qualifiers
    if (model == null) return Collections.emptyList();
    SpringJamQualifier qualifier = new SpringJamQualifier(psiAnnotation, null, null);
    return model.findQualifiedBeans(qualifier);
  }

  @Nullable
  public static PsiAnnotation getQualifiedAnnotation(PsiModifierListOwner modifierListOwner) {
    return getQualifiedAnnotation(modifierListOwner, ModuleUtilCore.findModuleForPsiElement(modifierListOwner));
  }

  @Nullable
  private static PsiAnnotation getQualifiedAnnotation(PsiModifierListOwner modifierListOwner, @Nullable Module module) {
    if (module == null) return null;

    List<PsiClass> annotationTypeClasses = JamAnnotationTypeUtil.getQualifierAnnotationTypesWithChildren(module);

    for (PsiClass annotationTypeClass : annotationTypeClasses) {
      if ((JamAnnotationTypeUtil.isAcceptedFor(annotationTypeClass, ElementType.FIELD) && modifierListOwner instanceof PsiField) ||
        (JamAnnotationTypeUtil.isAcceptedFor(annotationTypeClass, ElementType.PARAMETER) && modifierListOwner instanceof PsiParameter)) {
        PsiAnnotation annotation = AnnotationUtil.findAnnotation(modifierListOwner, annotationTypeClass.getQualifiedName());

        if (annotation != null) {
          return annotation;
        }
      }
    }

    return null;
  }

  @Nullable
  public static PsiAnnotation getAutowiredAnnotation(@Nonnull PsiModifierListOwner owner) {
    return AnnotationUtil.findAnnotation(owner, SpringAnnotationsConstants.INJECT_ANNOTATIONS);
  }

  @Nullable
  public static PsiAnnotation getResourceAnnotation(@Nonnull PsiModifierListOwner owner) {
    PsiModifierList modifierList = owner.getModifierList();
    if (modifierList != null) {
      return modifierList.findAnnotation(SpringAnnotationsConstants.JAVAX_RESOURCE_ANNOTATION);
    }
    return null;
  }

  public static boolean isAutowiredByAnnotation(@Nonnull PsiModifierListOwner owner) {
    return AnnotationUtil.isAnnotated(owner, SpringAnnotationsConstants.INJECT_ANNOTATIONS, 0);
  }

  public static boolean isRequired(@Nonnull PsiModifierListOwner owner) {
    PsiModifierList modifierList = owner.getModifierList();
    if (modifierList != null) {
      PsiAnnotation required = modifierList.findAnnotation(SpringAnnotationsConstants.REQUIRED_ANNOTATION);
      if (required != null) {
        return true;
      }
      PsiAnnotation autowiredAnnotation = getAutowiredAnnotation(owner);
      if (autowiredAnnotation != null) {
        Boolean value = AnnotationModelUtil.getBooleanValue(autowiredAnnotation, "required", true).getValue();
        return value == null || value.booleanValue();
      }
    }
    return true;
  }

  @Nonnull
  public static List<PsiMethod> getAnnotatedAutowiredMethods(@Nonnull PsiClass psiClass) {
    List<PsiMethod> methods = new ArrayList<PsiMethod>();
    for (PsiMethod psiMethod : psiClass.getAllMethods()) {
      if (isAutowiredByAnnotation(psiMethod)) {
        methods.add(psiMethod);
      }
    }
    return methods;
  }

  @Nonnull
  public static List<PsiField> getAnnotatedAutowiredFields(@Nonnull PsiClass psiClass) {
    List<PsiField> fields = new ArrayList<PsiField>();
    for (PsiField psiField : psiClass.getAllFields()) {
      if (isAutowiredByAnnotation(psiField)) {
        fields.add(psiField);
      }
    }
    return fields;
  }

  @Nonnull
  public static List<SpringBaseBeanPointer> autowireByType(@Nonnull SpringModel model, PsiType psiType) {
    if (psiType instanceof PsiClassType) {
      PsiType beanType = PsiUtil.extractIterableTypeParameter(psiType, false);
      if (beanType == null) {
        beanType = psiType;
      }
      if (beanType instanceof PsiClassType) {
        PsiClass psiClass = ((PsiClassType)beanType).resolve();
        if (psiClass != null) {
          return excludeAutowireCandidates(model.findBeansByEffectivePsiClassWithInheritance(psiClass));
        }
      }
    }
    return Collections.emptyList();
  }

  @NonNls
  private final static Set<String> STANDARD_AUTOWIRINGS =
    new HashSet<String>(Arrays.asList("javax.servlet.http.HttpServletRequest",
                                      "javax.servlet.http.HttpSession",

                                      "org.springframework.beans.factory.BeanFactory",
                                      "org.springframework.context.ApplicationContext",
                                      "org.springframework.context.ApplicationEventPublisher",
                                      "org.springframework.core.io.ResourceLoader"

    ));

  public static boolean isAutowiredByDefault(@Nonnull PsiType psiType) {
    String text = psiType.getCanonicalText();
    return text != null && STANDARD_AUTOWIRINGS.contains(text);
  }
}
