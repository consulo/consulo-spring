/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: Sergey.Vasiliev
 * Date: Nov 13, 2006
 * Time: 5:22:24 PM
 */
package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.language.psi.*;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.CustomBean;
import com.intellij.spring.impl.ide.model.xml.CustomBeanWrapper;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import consulo.language.editor.util.PsiUtilBase;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.psi.xml.XmlTag;
import consulo.xml.util.xml.ConvertContext;
import consulo.xml.util.xml.DomElement;
import consulo.xml.util.xml.DomManager;
import consulo.xml.util.xml.DomUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpringConverterUtil {
  private static final Map<Class, Class[]> myConverters = new HashMap<Class, Class[]>();

  static {
    Class[] classes =
      {Object[].class, boolean[].class, byte[].class, short[].class, int[].class, long[].class, float[].class, double[].class};
    myConverters.put(Set.class, classes);
    myConverters.put(List.class, classes);
  }

  private SpringConverterUtil() {
  }

  @Nullable
  public static SpringModel getSpringModel(final ConvertContext context) {
    return getSpringModel(context.getInvocationElement());
  }

  @Nullable
  public static SpringModel getSpringModel(final DomElement element) {
    final XmlFile xmlFile = (XmlFile)DomUtil.getFile(element).getOriginalFile();

    return SpringManager.getInstance(xmlFile.getProject()).getSpringModelByFile(xmlFile);
  }

  @Nullable
  public static DomSpringBean getCurrentBean(final ConvertContext context) {
    return getCurrentBean(context.getInvocationElement());
  }

  @Nullable
  public static CommonSpringBean getCurrentBeanCustomAware(final ConvertContext context) {
    DomSpringBean bean = getCurrentBean(context);
    if (bean instanceof CustomBeanWrapper) {
      final CustomBeanWrapper wrapper = (CustomBeanWrapper)bean;
      List<CustomBean> list = wrapper.getCustomBeans();
      if (!list.isEmpty()) {
        return list.get(0);
      }
    }
    return bean;
  }

  @Nullable
  public static DomSpringBean getCurrentBean(final DomElement element) {
    final XmlTag tag = element.getXmlTag();
    if (tag != null) {
      final XmlTag originalElement = PsiUtilBase.getOriginalElement(tag, XmlTag.class);
      if (originalElement != tag && originalElement != null) {
        final DomElement domElement = DomManager.getDomManager(originalElement.getProject()).getDomElement(originalElement);
        if (domElement != null) {
          final DomSpringBean springBean = domElement.getParentOfType(DomSpringBean.class, false);
          if (springBean != null) {
            return springBean;
          }
        }
      }
    }
    return element.getParentOfType(DomSpringBean.class, false);
  }

  public static boolean isConvertable(@Nonnull final PsiType from, @Nonnull PsiType to, Project project) {

    if (to instanceof PsiClassType) {
      to = ((PsiClassType)to).rawType();
    }
    if (to.isAssignableFrom(from)) return true;
    if (from.equalsToText(CommonClassNames.JAVA_LANG_STRING)) {
      if (isStringConvertable(to)) return true;
    }

    for (Class registerdClass : myConverters.keySet()) {
      final PsiType registeredFromType = findType(registerdClass, project);
      if (registeredFromType != null && from.isAssignableFrom(registeredFromType)) {
        Class[] classes = myConverters.get(registerdClass);
        for (Class aClass : classes) {
          final PsiType registeredTooType = findType(aClass, project);
          if (registeredTooType != null && (registeredTooType.equals(to) || registeredTooType.isAssignableFrom(to))) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Nullable
  public static PsiType findType(Class aClass, Project project) {
    final PsiManager psiManager = PsiManager.getInstance(project);

    if (aClass.isArray()) {
      final Class componentType = aClass.getComponentType();

      final PsiType componentClassType = findType(componentType, project);
      return componentClassType != null ? componentClassType.createArrayType() : null;
    }
    else if (aClass.isPrimitive()) {
        return JavaPsiFacade.getInstance(project).getElementFactory().createPrimitiveType(aClass.getName());
    }
    else {

      final PsiClass psiClass =
        JavaPsiFacade.getInstance(psiManager.getProject()).findClass(aClass.getName(), GlobalSearchScope.allScope(project));
      if (psiClass == null) return null;

      return JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory().createType(psiClass);
    }
  }

  private static boolean isStringConvertable(final PsiType requiredType) {
    if (requiredType instanceof PsiClassType) {
      final PsiClass psiClass = ((PsiClassType)requiredType).resolve();
      if (psiClass != null) {
        for (PsiMethod constructor : psiClass.getConstructors()) {
          final PsiParameterList parameterList = constructor.getParameterList();
          if (parameterList.getParametersCount() == 1) {
            final PsiParameter parameter = parameterList.getParameters()[0];
            if (String.class.getCanonicalName().equals(parameter.getType().getCanonicalText())) {
              return true;
            }
          }
        }
      }
    }
    else if (requiredType instanceof PsiPrimitiveType) {
      PsiType[] convertable =
        new PsiType[]{PsiType.BOOLEAN, PsiType.BYTE, PsiType.CHAR, PsiType.DOUBLE, PsiType.FLOAT, PsiType.INT, PsiType.LONG, PsiType.SHORT};

      for (PsiType psiType : convertable) {
        if (requiredType.equals(psiType)) return true;
      }
    }

    return false;
  }
}
