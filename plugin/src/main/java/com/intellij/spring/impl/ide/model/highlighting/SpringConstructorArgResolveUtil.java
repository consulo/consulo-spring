/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.highlighting;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.java.language.psi.codeStyle.VariableKind;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.converters.SpringBeanUtil;
import com.intellij.spring.impl.ide.model.converters.SpringConverterUtil;
import com.intellij.spring.impl.ide.model.xml.beans.ConstructorArg;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import consulo.language.editor.refactoring.rename.SuggestedNameInfo;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nullable;
import java.util.*;

public class SpringConstructorArgResolveUtil {

  private SpringConstructorArgResolveUtil() {
  }

  public static List<PsiMethod> findMatchingMethods(SpringBean springBean) {
    SpringModel model = SpringUtils.getSpringModel(springBean);
    return findMatchingMethods(springBean, model);
  }

  private static List<PsiMethod> findMatchingMethods(SpringBean springBean,
                                                     SpringModel springModel) {

    List<PsiMethod> methods = SpringBeanUtil.getInstantiationMethods(springBean);
    if (methods.size() == 0) {
      return methods;
    }
    Set<ConstructorArg> args = springBean.getAllConstructorArgs();
    boolean constructorAutowire = SpringAutowireUtil.isConstructorAutowire(springBean);
    Map<Integer, ConstructorArg> indexedArgs = getIndexedConstructorArgs(args);

    List<PsiMethod> accepted = new ArrayList<PsiMethod>(methods.size());
    for (PsiMethod method : methods) {
      PsiParameter[] parameters = method.getParameterList().getParameters();
      if (acceptMethodByAutowire(constructorAutowire, args, parameters) &&
          acceptMethodByParameterTypes(indexedArgs, constructorAutowire, springModel, args, parameters)) {

        accepted.add(method);
      }
    }

    return accepted;
  }

  public static boolean acceptMethodByAutowire(boolean constructorAutowire,
                                               Set<ConstructorArg> args,
                                               PsiParameter[] parameters) {

    if ((!constructorAutowire && parameters.length != args.size()) || (constructorAutowire && parameters.length < args.size())) {
      return false;
    }

    return true;
  }

  private static boolean acceptMethodByParameterTypes(Map<Integer, ConstructorArg> indexedArgs,
                                                      boolean constructorAutowire,
                                                      SpringModel springModel,
                                                      Set<ConstructorArg> args,
                                                      PsiParameter[] parameters) {
    for (int i = 0; i < parameters.length; i++) {
      PsiParameter parameter = parameters[i];
      if (!acceptParameter(parameter, args, indexedArgs, i)) {
        if (constructorAutowire && !SpringAutowireUtil.autowireByType(springModel, parameter.getType()).isEmpty()) {
          continue;
        }
        return false;
      }
    }
    return true;
  }

  public static boolean acceptParameter(PsiParameter parameter,
                                        Collection<ConstructorArg> list,
                                        Map<Integer, ConstructorArg> indexedArgs,
                                        int i) {
    PsiType psiType = parameter.getType();

    if (indexedArgs.get(i) != null) {
      ConstructorArg arg = indexedArgs.get(i);

      return hasProperArgumentType(psiType, arg);
    }
    else {
      for (ConstructorArg arg : list) {
        if (indexedArgs.values().contains(arg)) continue;

        if (hasProperArgumentType(psiType, arg)) return true;
      }
    }
    return false;
  }

  private static boolean hasProperArgumentType(PsiType requiredType, ConstructorArg arg) {
    PsiType constructorArgType = SpringBeanUtil.getRequiredType(arg);
    if (constructorArgType != null) {
      return constructorArgType.isAssignableFrom(requiredType);
    } else {
      PsiType[] types = arg.getTypesByValue();
      if (types == null) {
        return false;
      }
      for (PsiType valueType : types) {

        if ((requiredType.isAssignableFrom(valueType) || SpringUtils.isEffectiveClassType(arg, requiredType) ||
         SpringConverterUtil.isConvertable(valueType, requiredType, arg.getManager().getProject())))
          return true;
      }
      return false;
    }
  }

  // todo move it to SpringBean
  public static Map<Integer, ConstructorArg> getIndexedConstructorArgs(Collection<ConstructorArg> list) {
    Map<Integer, ConstructorArg> indexed = new HashMap<Integer, ConstructorArg>();

    for (ConstructorArg constructorArg : list) {
      Integer value = constructorArg.getIndex().getValue();
      if (value != null) {
        indexed.put(value, constructorArg);
      }
    }
    return indexed;
  }

  @Nullable
  public static PsiMethod getSpringBeanConstructor(SpringBean springBean, SpringModel springModel) {
    if (isInstantiatedByFactory(springBean)) return null;

    List<PsiMethod> psiMethods =
      SpringConstructorArgResolveUtil.findMatchingMethods(springBean, springModel);

    PsiMethod resolvedConstructor = null;
    for (PsiMethod psiMethod : psiMethods) {
      if (resolvedConstructor == null ||
          resolvedConstructor.getParameterList().getParametersCount() < psiMethod.getParameterList().getParametersCount()) {
        resolvedConstructor = psiMethod;
      }
    }
    return resolvedConstructor;
  }

  public static boolean isInstantiatedByFactory(SpringBean springBean) {
    return springBean.getFactoryMethod().getXmlAttribute() != null;
  }

  public static boolean hasEmptyConstructor(SpringBean springBean) {
    PsiClass beanClass = springBean.getBeanClass(false);
    if (beanClass != null) {
      PsiMethod[] constructors = beanClass.getConstructors();

      if (constructors.length == 0) return true;

      for (PsiMethod constructor : constructors) {
        if (constructor.getParameterList().getParametersCount() == 0) return true;
      }
    }
    return false;
  }

  public static String suggestParamsForConstructorArgsAsString(SpringBean springBean) {
    List<String> params = new ArrayList<String>();
    for (PsiParameter psiParameter : SpringConstructorArgResolveUtil.suggestParamsForConstructorArgs(springBean)) {
      params.add(psiParameter.getText());
    }

    return StringUtil.join(params, ",");
  }

  public static List<PsiParameter> suggestParamsForConstructorArgs(SpringBean springBean) {
    List<PsiParameter> methodParameters = new ArrayList<PsiParameter>();

    Project project = springBean.getManager().getProject();
    PsiElementFactory elementFactory = JavaPsiFacade.getInstance(project).getElementFactory();

    PsiClass aClass = JavaPsiFacade.getInstance(project).findClass("java.lang.String", GlobalSearchScope.allScope(project));
    assert aClass != null;
    PsiClassType defaultParamType = elementFactory.createType(aClass);

    JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(project);

    List<String> existedNames = new ArrayList<String>();
    List<ConstructorArg> constructorArgs = SpringUtils.getConstructorArgs(springBean);

    ConstructorArg[] sortedArgs = sortConstructorArgsByIndex(constructorArgs);

    for (ConstructorArg arg : sortedArgs) {
      if (arg == null) continue; // if indexes param od different constructor-args are eqaul 
      PsiType type = arg.getType().getValue();
      if (type == null) {
        PsiType[] psiTypes = arg.getTypesByValue();
        if (psiTypes != null && psiTypes.length > 0) {
          type = psiTypes[0];
        }
      }

      if (type == null || type.equals(PsiType.NULL)) type = defaultParamType;

      SuggestedNameInfo nameInfo = codeStyleManager.suggestVariableName(VariableKind.PARAMETER, null, null, type);
      String name = nameInfo.names[0];

      int i = 1;
      while (existedNames.contains(name)) {
        name += ++i;
      }

      existedNames.add(name);

      try {
        PsiParameter psiParameter = elementFactory.createParameter(name, type);
        methodParameters.add(psiParameter);
      }
      catch (IncorrectOperationException e) {
        throw new RuntimeException(e);
      }
    }
    return methodParameters;
  }

  private static ConstructorArg[] sortConstructorArgsByIndex(List<ConstructorArg> constructorArgs) {
    ConstructorArg[] args = new ConstructorArg[constructorArgs.size()];

    Map<Integer, ConstructorArg> indexedConstructorArgs = SpringConstructorArgResolveUtil.getIndexedConstructorArgs(constructorArgs);

    if (indexedConstructorArgs.size() == 0) {
      return constructorArgs.toArray(new ConstructorArg[constructorArgs.size()]);
    }

    List<ConstructorArg> indexed = new ArrayList<ConstructorArg>();
    for (Integer index : indexedConstructorArgs.keySet()) {
      int i = index.intValue();
      if (i >= 0 && i < args.length) {
        ConstructorArg arg = indexedConstructorArgs.get(index);
        args[i] = arg;
        indexed.add(arg);
      }
    }

    for (ConstructorArg constructorArg : constructorArgs) {
      if (!indexed.contains(constructorArg)) {
        for (int i = 0; i < args.length; i++) {
          if (args[i] == null) {
            args[i] = constructorArg;
          }
        }
      }
    }

    return args;
  }
}
