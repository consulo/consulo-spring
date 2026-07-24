package com.intellij.spring.impl.ide.model.actions.generate;

import com.intellij.java.impl.codeInsight.lookup.LookupItemUtil;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.java.language.psi.codeStyle.VariableKind;
import com.intellij.java.language.psi.util.PropertyUtil;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringIcons;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.highlighting.SpringConstructorArgResolveUtil;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.DomSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.*;
import consulo.ide.impl.idea.ide.util.MemberChooser;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.refactoring.rename.SuggestedNameInfo;
import consulo.language.editor.template.*;
import consulo.language.psi.PsiCompiledElement;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.project.Project;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.ReadonlyStatusHandler;
import consulo.virtualFileSystem.VirtualFile;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomUtil;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public class GenerateSpringBeanDependenciesUtil {

  public static boolean acceptBean(SpringBean springBean, boolean isSetterDependency) {
    return getCandidates(springBean, isSetterDependency).size() > 0;
  }

  public static boolean acceptPsiClass(PsiClass psiClass, boolean isSetterDependency) {
    SpringModel model = getSpringModel(psiClass);
    if (model == null) return false;

    List<SpringBaseBeanPointer> beansByPsiClass = model.findBeansByPsiClass(psiClass);
    return beansByPsiClass != null && beansByPsiClass.size() > 0 && getCandidates(model, psiClass, isSetterDependency).size() > 0;
  }

  public static List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> generateDependenciesFor(@Nullable SpringModel springModel,
                                                                                                   @Nullable PsiClass psiClass,
                                                                                                   boolean isSetterDependency) {
    List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> createdProperties =
      new ArrayList<Pair<SpringInjection, SpringGenerateTemplatesHolder>>();

    if (springModel != null && psiClass != null) {
      List<SpringBeanPointer> list =
        SpringUtils.findBeansByClassName(springModel.getAllCommonBeans(true), psiClass.getQualifiedName());
      if (list.size() > 0) {
        for (SpringBeanPointer pointer : list) {
          CommonSpringBean springBean = pointer.getSpringBean();
          if (springBean instanceof SpringBean && acceptBean((SpringBean)springBean, isSetterDependency)) {
            return ensureFileWritable((SpringBean)springBean)
              ? generateDependenciesFor((SpringBean)springBean, isSetterDependency)
              : new ArrayList<Pair<SpringInjection, SpringGenerateTemplatesHolder>>();
          }
        }
      }
      else {
        List<SpringBeanPointer> beans =
          chooseDependentBeans(getCandidates(springModel, psiClass, isSetterDependency), psiClass.getProject(), isSetterDependency);
        if (beans.size() > 0) {
          return createBeanAndGenerateDependencies(psiClass, isSetterDependency, beans);
        }
      }
    }
    return createdProperties;
  }

  public static List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> createBeanAndGenerateDependencies(PsiClass psiClass,
                                                                                                             boolean isSetterDependency,
                                                                                                             List<SpringBeanPointer> beans) {
    CommonSpringBean springBean = beans.get(0).getSpringBean();
    SpringBean bean = null;
    if (springBean instanceof DomSpringBean) {
      DomSpringBean domSpringBean = (DomSpringBean)springBean;
      bean = createSpingBean(domSpringBean.getParentOfType(Beans.class, false), psiClass);
    }
    return bean == null
      ? new ArrayList<Pair<SpringInjection, SpringGenerateTemplatesHolder>>()
      : generateDependencies(bean, beans, isSetterDependency);
  }

  @Nullable
  private static SpringBean createSpingBean(Beans parentBeans, PsiClass psiClass) {
    if (!ensureFileWritable(parentBeans)) return null;

    SpringBean springBean = parentBeans.addBean();
    springBean.getClazz().setStringValue(psiClass.getQualifiedName());

    String[] strings = SpringUtils.suggestBeanNames(springBean);
    springBean.getId().setStringValue(strings.length > 0 ? strings[0] : "");

    return springBean;
  }

  private static boolean ensureFileWritable(DomElement domElement) {
    return ensureFileWritable(DomUtil.getFile(domElement).getVirtualFile(), domElement.getManager().getProject());
  }

  private static boolean ensureFileWritable(@Nullable VirtualFile virtualFile, Project project) {
    if (virtualFile != null && !virtualFile.isWritable()) {
      ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(virtualFile);
      if (status.hasReadonlyFiles()) return false;
    }
    return true;
  }

  public static List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> generateDependenciesFor(@Nullable SpringBean springBean,
                                                                                                   boolean isSetterDependency) {
    if (springBean == null || springBean.getBeanClass() == null) return Collections.emptyList();

    Project project = springBean.getManager().getProject();
    List<SpringBeanPointer> dependencies =
      chooseDependentBeans(getCandidates(springBean, isSetterDependency), project, isSetterDependency);

    return generateDependencies(springBean, dependencies, isSetterDependency);
  }

  public static List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> generateDependencies(@Nonnull SpringBean springBean,
                                                                                                List<SpringBeanPointer> dependencies,
                                                                                                boolean isSetterDependency) {
    List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> springInjections =
      new ArrayList<Pair<SpringInjection, SpringGenerateTemplatesHolder>>();

    SpringModel model = SpringUtils.getSpringModel(springBean);
    for (SpringBeanPointer bean : dependencies) {
      Pair<SpringInjection, SpringGenerateTemplatesHolder> pair =
        isSetterDependency ? createDependency(springBean, bean, model) : createConstructorArg(springBean, bean, model);

      if (pair != null) springInjections.add(pair);
    }
    return springInjections;

  }

  @Nonnull
  private static List<SpringBeanPointer> chooseDependentBeans(List<SpringBeanClassMember> candidates,
                                                              final Project project,
                                                              final boolean setterDependency) {
    List<SpringBeanPointer> chosenBeans = new ArrayList<SpringBeanPointer>();

    MemberChooser<SpringBeanClassMember> chooser = new MemberChooser<SpringBeanClassMember>(
      candidates.toArray(new SpringBeanClassMember[candidates.size()]), false, setterDependency, project) {
      protected ShowContainersAction getShowContainersAction() {
        return new ShowContainersAction(LocalizeValue.of(SpringBundle.message("spring.beans.chooser.show.context.files")),
                                        SpringIcons.CONFIG_FILE);
      }

      protected String getAllContainersNodeName() {
        return SpringBundle.message("spring.beans.chooser.all.context.files");
      }
    };

    chooser.setTitle(SpringBundle.message("spring.bean.dependencies.chooser.title"));
    chooser.setCopyJavadocVisible(false);
    chooser.show();

    if (chooser.getExitCode() == MemberChooser.OK_EXIT_CODE) {
      SpringBeanClassMember[] members = chooser.getSelectedElements(new SpringBeanClassMember[0]);
      if (members != null) {
        for (SpringBeanClassMember member : members) {
          chosenBeans.add(member.getSpringBean());
        }
      }
    }

    return chosenBeans;
  }

  @Nonnull
  public static List<SpringBeanClassMember> getCandidates(SpringBean springBean, boolean setterDependency) {
    List<SpringBeanClassMember> beanClassMembers = new ArrayList<SpringBeanClassMember>();

    SpringModel model = SpringUtils.getSpringModel(springBean);
    PsiClass springBeanClass = springBean.getBeanClass();
    if (springBeanClass != null) {
      Collection<? extends SpringBaseBeanPointer> allBeans = model.getAllCommonBeans();
      for (SpringBeanPointer pointer : allBeans) {
        if (pointer.isReferenceTo(springBean)) continue;

        PsiClass[] dependentBeanClasses = pointer.getEffectiveBeanType();
        if (canBeReferenced(pointer, allBeans) && dependentBeanClasses.length > 0 && !hasDependency(springBean,
                                                                                                    pointer,
                                                                                                    setterDependency)) {

          if (setterDependency && !isCompiledElementWithoutSetter(springBeanClass, dependentBeanClasses) || !setterDependency &&
            !isCompiledElementWithoutProperConstructor(
              springBean, model,
              springBean.getBeanClass(),
              dependentBeanClasses)) {
            beanClassMembers.add(new SpringBeanClassMember(pointer));
          }
        }
      }
    }
    return beanClassMembers;
  }

  private static boolean canBeReferenced(SpringBeanPointer bean, Collection<? extends SpringBeanPointer> beans) {
    return SpringUtils.getReferencedName(bean, beans) != null;
  }

  public static List<SpringBeanClassMember> getCandidates(@Nonnull SpringModel model,
                                                          PsiClass psiClass,
                                                          boolean setterDependency) {

    List<SpringBeanClassMember> beanClassMembers = new ArrayList<SpringBeanClassMember>();

    Collection<? extends SpringBeanPointer> allBeans = model.getAllCommonBeans();
    for (SpringBeanPointer bean : allBeans) {
      PsiClass[] dependentBeanClasses = bean.getEffectiveBeanType();

      if (canBeReferenced(bean, allBeans) && dependentBeanClasses.length > 0) {
        if ((setterDependency && !isCompiledElementWithoutSetter(psiClass, dependentBeanClasses)) ||
          (!setterDependency && !isCompiledElementWithoutProperConstructor(null, model, psiClass, dependentBeanClasses))) {
          beanClassMembers.add(new SpringBeanClassMember(bean));
        }
      }
    }
    return beanClassMembers;
  }

  private static boolean isCompiledElementWithoutProperConstructor(@Nullable SpringBean springBean,
                                                                   SpringModel model,
                                                                   PsiClass springBeanClass,
                                                                   PsiClass[] beanClasses) {
    if (springBeanClass instanceof PsiCompiledElement || springBeanClass.getOriginalElement() instanceof PsiCompiledElement) {

      if (springBean != null) {
        for (PsiClass beanClass : beanClasses) {
          if (getCompiledElementCandidateConstructor(springBean, springBeanClass, beanClass) != null) {
            return false;
          }
        }
      }
      else {
        List<SpringBeanPointer> list = SpringUtils.findBeansByClassName(model.getAllCommonBeans(true), springBeanClass.getQualifiedName());
        for (PsiClass beanClass : beanClasses) {
          for (SpringBeanPointer pointer : list) {
            CommonSpringBean bean = pointer.getSpringBean();
            if (bean instanceof SpringBean &&
              getCompiledElementCandidateConstructor((SpringBean)bean, springBeanClass, beanClass) != null) {
              return false;
            }
          }
        }
        for (PsiMethod constructor : springBeanClass.getConstructors()) {
          if (constructor.getParameterList().getParametersCount() == 1) {
            PsiType type = constructor.getParameterList().getParameters()[0].getType();
            PsiElementFactory psiElementFactory = JavaPsiFacade.getInstance(springBeanClass.getProject()).getElementFactory();
            for (PsiClass beanClass : beanClasses) {
              PsiClassType classType = psiElementFactory.createType(beanClass);
              if (type.isAssignableFrom(classType)) {
                return false;
              }
            }
          }
        }
      }
      return true;
    }
    return false;
  }

  @Nullable
  private static PsiMethod getCompiledElementCandidateConstructor(SpringBean currentBean,
                                                                  PsiClass currentBeanClass,
                                                                  PsiClass candidateParameterClass) {

    PsiType candidatePsiType =
      JavaPsiFacade.getInstance(currentBeanClass.getProject()).getElementFactory().createType(candidateParameterClass);

    if (SpringUtils.getConstructorArgs(currentBean).size() == 0) {
      return findConstructor(currentBeanClass.getConstructors(), Collections.singletonList(candidatePsiType));
    }
    else {
      List<PsiMethod> methods = SpringConstructorArgResolveUtil.findMatchingMethods(currentBean);

      for (PsiMethod method : methods) {
        List<PsiType> psiParameterTypes = getParameterTypes(method);
        psiParameterTypes.add(candidatePsiType);
        PsiMethod existedConstructor = findConstructor(currentBeanClass.getConstructors(), psiParameterTypes);
        if (existedConstructor != null) return existedConstructor;
      }
    }
    return null;
  }

  private static PsiMethod findConstructor(PsiMethod[] constructors, List<PsiType> psiParameterTypes) {
    for (PsiMethod constructor : constructors) {
      if (constructor.getParameterList().getParametersCount() == psiParameterTypes.size()) {
        boolean isAccepted = true;
        PsiParameter[] parameters = constructor.getParameterList().getParameters();
        for (int i = 0; i < psiParameterTypes.size(); i++) {
          if (!psiParameterTypes.get(i).isAssignableFrom(parameters[i].getType())) {
            isAccepted = false;
            break;
          }
        }
        if (isAccepted) return constructor;
      }
    }
    return null;
  }

  private static List<PsiType> getParameterTypes(PsiMethod method) {
    List<PsiType> psiParameterTypes = new ArrayList<PsiType>();
    PsiParameter[] parameters = method.getParameterList().getParameters();
    for (PsiParameter parameter : parameters) {
      psiParameterTypes.add(parameter.getType());
    }
    return psiParameterTypes;
  }

  private static boolean isCompiledElementWithoutSetter(PsiClass springBeanClass, PsiClass[] beanClasses) {
    if (springBeanClass instanceof PsiCompiledElement || springBeanClass.getOriginalElement() instanceof PsiCompiledElement) {
      for (PsiClass beanClass : beanClasses) {
        if (getExistedSetter(springBeanClass, beanClass) != null) return false;
      }
      return true;
    }

    return false;
  }

  private static boolean hasDependency(CommonSpringBean currentBean,
                                       SpringBeanPointer candidateBean,
                                       boolean isSetterDependency) {
    return isSetterDependency
      ? SpringUtils.getSetterDependencies(currentBean).contains(candidateBean)
      : SpringUtils.getConstructorDependencies(currentBean).contains(candidateBean);
  }

  @Nullable
  private static Pair<SpringInjection, SpringGenerateTemplatesHolder> createDependency(SpringBean currentBean,
                                                                                       SpringBeanPointer bean,
                                                                                       SpringModel model) {

    SpringGenerateTemplatesHolder templatesHolder = new SpringGenerateTemplatesHolder(currentBean.getManager().getProject());

    PsiClass currentBeanClass = currentBean.getBeanClass();
    PsiClass[] candidateBeanClasses = bean.getEffectiveBeanType();

    if (currentBeanClass != null && candidateBeanClasses.length > 0) {
      PsiMethod setter = getOrCreateSetter(bean, currentBeanClass, candidateBeanClasses, templatesHolder, model);

      if (setter != null) {
        SpringProperty property = currentBean.addProperty();
        property.getName().ensureXmlElementExists();
        property.getName().setStringValue(PropertyUtil.getPropertyNameBySetter(setter));
        property.getRefAttr().setStringValue(getReferencedName(currentBean, bean));
        return new Pair<SpringInjection, SpringGenerateTemplatesHolder>(property, templatesHolder);
      }
    }

    return null;
  }

  @Nullable
  private static Pair<SpringInjection, SpringGenerateTemplatesHolder> createConstructorArg(SpringBean currentBean,
                                                                                           SpringBeanPointer bean,
                                                                                           SpringModel model) {
    ConstructorArg arg = null;
    SpringGenerateTemplatesHolder holder = new SpringGenerateTemplatesHolder(currentBean.getManager().getProject());

    PsiClass currentBeanClass = currentBean.getBeanClass();
    PsiClass[] candidateBeanClasses = bean.getEffectiveBeanType();

    if (currentBeanClass != null && candidateBeanClasses.length > 0) {
      PsiMethod existedConstructor = findExistedConstructor(currentBean, currentBeanClass, candidateBeanClasses);

      if (existedConstructor == null) {
        if (!ensureFileWritable(currentBeanClass.getContainingFile().getVirtualFile(), currentBeanClass.getProject())) return null;

        existedConstructor = findProperConstructorAndAddParameter(currentBean, bean, currentBeanClass, candidateBeanClasses, holder, model);
      }
      arg = currentBean.addConstructorArg();
      arg.getRefAttr().setStringValue(getReferencedName(currentBean, bean));
      if (existedConstructor == null && SpringConstructorArgResolveUtil.findMatchingMethods(currentBean).size() == 0) {
        PsiMethod psiMethod = createConstructor(currentBean);
        if (psiMethod.getParameterList().getParametersCount() == 1) {
          PsiParameter parameter = psiMethod.getParameterList().getParameters()[0];
          PsiType type = parameter.getType();
          if (type instanceof PsiClassType) {
            PsiClass psiClass = ((PsiClassType)type).resolve();
            if (psiClass != null) {
              addCreateSetterTemplate(psiMethod, new PsiClass[]{psiClass}, bean, holder, model);
            }
          }
        }
      }
    }

    return new Pair<SpringInjection, SpringGenerateTemplatesHolder>(arg, holder);
  }

  @Nullable
  private static String getReferencedName(SpringBean currentBean, SpringBeanPointer bean) {
    SpringModel model = SpringUtils.getSpringModel(currentBean);
    return model != null ? SpringUtils.getReferencedName(bean, model.getAllCommonBeans(true)) : null;
  }

  @Nullable
  private static PsiMethod getOrCreateSetter(SpringBeanPointer candidateBean,
                                             PsiClass currentBeanClass,
                                             PsiClass[] candidateBeanClasses,
                                             SpringGenerateTemplatesHolder templatesHolder, SpringModel model) {
    for (PsiClass candidateBeanClass : candidateBeanClasses) {
      PsiMethod existedSetter = getExistedSetter(currentBeanClass, candidateBeanClass);
      if (existedSetter != null) return existedSetter;
    }

    boolean isWritable = ensureFileWritable(currentBeanClass.getContainingFile().getVirtualFile(), currentBeanClass.getProject());
    if (!isWritable) return null;

    PsiMethod setter = createSetter(candidateBean, currentBeanClass, candidateBeanClasses);

    addCreateSetterTemplate(setter, candidateBeanClasses, candidateBean, templatesHolder, model);

    return setter;
  }

  @Nullable
  private static PsiMethod findExistedConstructor(SpringBean currentBean,
                                                  PsiClass currentBeanClass,
                                                  PsiClass[] candidateParameterClasses) {
    List<PsiMethod> constructors = SpringConstructorArgResolveUtil.findMatchingMethods(currentBean);
    for (PsiClass candidateBeanClass : candidateParameterClasses) {
      for (PsiMethod constructor : constructors) {
        List<PsiType> psiParameterTypes = getParameterTypes(constructor);
        PsiClassType candidateBeanType =
          JavaPsiFacade.getInstance(currentBeanClass.getProject()).getElementFactory().createType(candidateBeanClass);

        psiParameterTypes.add(candidateBeanType);

        PsiMethod existedConstructorWithRequiredParameter = findConstructor(currentBeanClass.getConstructors(), psiParameterTypes);
        if (existedConstructorWithRequiredParameter != null) return existedConstructorWithRequiredParameter;
      }
    }
    return null;
  }

  @Nullable
  private static PsiMethod findProperConstructorAndAddParameter(SpringBean currentBean,
                                                                SpringBeanPointer bean,
                                                                PsiClass currentBeanClass,
                                                                PsiClass[] candidateParameterClasses,
                                                                SpringGenerateTemplatesHolder holder, SpringModel model) {
    PsiMethod properConstructor = null;
    List<PsiMethod> constructors = SpringConstructorArgResolveUtil.findMatchingMethods(currentBean);
    for (PsiClass candidateBeanClass : candidateParameterClasses) {
      for (PsiMethod constructor : constructors) {
        if (properConstructor == null ||
          properConstructor.getParameterList().getParametersCount() < constructor.getParameterList().getParametersCount()) {
          properConstructor = constructor;
        }
      }

      if (properConstructor != null) {
        addConstructorParameter(currentBeanClass, candidateBeanClass, properConstructor);
        addCreateSetterTemplate(properConstructor, new PsiClass[]{candidateBeanClass}, bean, holder,
                                properConstructor.getParameterList().getParametersCount() - 1, model);

        return properConstructor;
      }
    }
    return null;
  }

  @Nonnull
  private static PsiMethod createConstructor(SpringBean springBean) {
    PsiClass instantiationClass = null;
    PsiMethod instantiationMethod = null;

    PsiClass beanClass = springBean.getBeanClass();
    try {
      assert beanClass != null;
      PsiElementFactory elementFactory = JavaPsiFacade.getInstance(beanClass.getProject()).getElementFactory();

      if (isInstantiatedByFactory(springBean)) {
        SpringBeanPointer beanPointer = springBean.getFactoryBean().getValue();
        if (beanPointer != null) {
          instantiationClass = beanPointer.getBeanClass();
          String methodName = getInstantiationMethodName(instantiationClass, springBean);
          @NonNls String methodText = PsiModifier.PUBLIC + " " + beanClass.getName() + " " + methodName + "() { return null; }";
          instantiationMethod = elementFactory.createMethodFromText(methodText, null);
        }
      }
      else if (isInstantiatedByFactoryMethod(springBean)) {
        instantiationClass = beanClass;
        String methodName = getInstantiationMethodName(instantiationClass, springBean);
        @NonNls String methodText =
          PsiModifier.PUBLIC + " " + PsiModifier.STATIC + " " + beanClass.getName() + " " + methodName + "() { return null; }";
        instantiationMethod = elementFactory.createMethodFromText(methodText, null);
      }
      else {
        instantiationClass = beanClass;
        instantiationMethod = elementFactory.createConstructor();
      }

      List<PsiParameter> parameters = SpringConstructorArgResolveUtil.suggestParamsForConstructorArgs(springBean);
      assert instantiationMethod != null;
      for (PsiParameter parameter : parameters) {
        instantiationMethod.getParameterList().add(parameter);
      }
      assert instantiationClass != null;
      instantiationMethod = (PsiMethod)instantiationClass.add(instantiationMethod);

    }
    catch (IncorrectOperationException e) {
      throw new RuntimeException(e);
    }
    return instantiationMethod;
  }

  @Nonnull
  private static String getInstantiationMethodName(PsiClass factoryBeanClass, SpringBean springBean) {
    String methodName = springBean.getFactoryMethod().getStringValue();
    if (!StringUtil.isEmptyOrSpaces(methodName)) return methodName;
    PsiClass beanClass = springBean.getBeanClass();

    @NonNls String methodPrefix = "create";
    methodName = methodPrefix + beanClass.getName();
    int i = 0;
    while (factoryBeanClass.findMethodsByName(methodName, true).length > 0) {
      methodName = methodPrefix + beanClass.getName() + (++i);
    }

    return methodName;
  }

  private static boolean isInstantiatedByFactoryMethod(SpringBean springBean) {
    return DomUtil.hasXml(springBean.getFactoryMethod());
  }

  private static boolean isInstantiatedByFactory(SpringBean springBean) {
    return DomUtil.hasXml(springBean.getFactoryBean());
  }

  private static void addConstructorParameter(PsiClass currentBeanClass,
                                              PsiClass candidateBeanClass,
                                              PsiMethod constructor) {
    PsiElementFactory psiElementFactory = JavaPsiFacade.getInstance(currentBeanClass.getProject()).getElementFactory();
    try {
      PsiClassType psiClassType = psiElementFactory.createType(candidateBeanClass);
      SuggestedNameInfo nameInfo =
        JavaCodeStyleManager.getInstance(currentBeanClass.getProject())
                            .suggestVariableName(VariableKind.PARAMETER, null, null, psiClassType);
      String name = nameInfo.names[0];
      int i = 0;
      while (hasSuchName(constructor.getParameterList().getParameters(), name)) {
        name += ++i;
      }

      PsiParameter parameter = psiElementFactory.createParameter(name, psiClassType);
      constructor.getParameterList().add(parameter);
    }
    catch (IncorrectOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean hasSuchName(PsiParameter[] parameters, String name) {
    for (PsiParameter parameter : parameters) {
      if (name.equals(parameter.getName())) return true;
    }
    return false;
  }

  @Nonnull
  private static PsiMethod createSetter(SpringBeanPointer candidateBean,
                                        PsiClass currentBeanClass,
                                        PsiClass[] candidateBeanClasses) {
    PsiMethod method;
    try {
      PsiNameHelper psiNameHelper = JavaPsiFacade.getInstance(currentBeanClass.getProject()).getNameHelper();

      String beanName = candidateBean.getName();
      String name = beanName == null || !psiNameHelper.isIdentifier(beanName) ? candidateBeanClasses[0].getName() : beanName;

      PsiManager psiManager = PsiManager.getInstance(currentBeanClass.getProject());
      PsiElementFactory elementFactory = JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory();

      @NonNls String methodText = "public void set" + StringUtil.capitalize(name) + "(" + candidateBeanClasses[0].getQualifiedName() +
        " " + StringUtil.decapitalize(name) + ") { }";

      method = elementFactory.createMethodFromText(methodText, null);
      method = (PsiMethod)currentBeanClass.add(method);

      CodeStyleManager formatter = CodeStyleManager.getInstance(psiManager.getProject());
      JavaCodeStyleManager styler = JavaCodeStyleManager.getInstance(psiManager.getProject());
      styler.shortenClassReferences(formatter.reformat(method));
    }
    catch (IncorrectOperationException e) {
      throw new RuntimeException(e);
    }

    return method;
  }

  private static void addCreateSetterTemplate(PsiMethod method,
                                              PsiClass[] psiClasses,
                                              SpringBeanPointer bean,
                                              SpringGenerateTemplatesHolder templatesHolder, SpringModel model) {
    addCreateSetterTemplate(method, psiClasses, bean, templatesHolder, 0, model);
  }

  private static void addCreateSetterTemplate(final PsiMethod method,
                                              final PsiClass[] psiClasses,
                                              final SpringBeanPointer bean,
                                              SpringGenerateTemplatesHolder templatesHolder,
                                              final int paramId,
                                              final SpringModel model) {
    templatesHolder.addTemplateFactory(method.getParameterList(), new Supplier<Template>() {
      public Template get() {
        PsiParameter parameter = method.getParameterList().getParameters()[paramId];
        PsiTypeElement typeElement = parameter.getTypeElement();

        Collection<PsiClass> variants = getSuperTypeVariants(psiClasses);
        Expression interfaces = getSuperTypesExpression(typeElement.getType().getCanonicalText(), variants);

        Expression ids = getSuggestNamesExpression(method, bean, paramId, model);

        TemplateBuilder builder = TemplateBuilderFactory.getInstance().createTemplateBuilder(method.getParameterList());

        if (variants.size() > 1) {
          builder.replaceElement(typeElement, "type", interfaces, true);
        }
        builder.replaceElement(parameter.getNameIdentifier(), "names", ids, true);

        return builder.buildInlineTemplate();
      }
    });
  }

  private static Collection<PsiClass> getSuperTypeVariants(PsiClass[] psiClasses) {
    Collection<PsiClass> variants = new HashSet<PsiClass>();
    for (PsiClass beanClass : psiClasses) {
      variants.add(beanClass);
      variants.addAll(Arrays.asList(beanClass.getInterfaces()));

      for (PsiClass psiClass : beanClass.getSupers()) {
        if (Object.class.getName().equals(psiClass.getQualifiedName())) continue;
        variants.add(psiClass);
      }
    }
    return variants;
  }

  private static Expression getSuggestNamesExpression(final PsiMethod method, final SpringBeanPointer bean, final int paramId,
                                                      final SpringModel model) {
    final PsiParameter parameter = method.getParameterList().getParameters()[paramId];
    return new Expression() {
      public Result calculateResult(ExpressionContext context) {
        PsiDocumentManager.getInstance(context.getProject()).commitAllDocuments();
        PsiIdentifier psiIdentifier = parameter.getNameIdentifier();

        return new TextResult(psiIdentifier != null ? psiIdentifier.getText() : "foo");
      }

      public Result calculateQuickResult(ExpressionContext context) {
        return calculateResult(context);
      }

      public LookupElement[] calculateLookupItems(ExpressionContext context) {
        PsiDocumentManager.getInstance(context.getProject()).commitAllDocuments();
        LinkedHashSet<LookupElement> items = new LinkedHashSet<LookupElement>();
        for (String name : getSuggestedNames()) {
          items.add(LookupItemUtil.objectToLookupItem(name));
        }

        return items.toArray(new LookupElement[items.size()]);
      }

      private Collection<String> getSuggestedNames() {
        PsiNameHelper psiNameHelper = JavaPsiFacade.getInstance(method.getProject()).getNameHelper();
        Set<String> names = new HashSet<String>();
        String beanName = bean.getName();
        if (beanName != null) {
          for (String name : model.getAllBeanNames(beanName)) {
            if (psiNameHelper.isIdentifier(name)) {
              names.add(name);
            }
          }
        }

        JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(method.getProject());
        PsiParameter[] parameters = method.getParameterList().getParameters();
        if (parameters.length < paramId) {
          SuggestedNameInfo info = codeStyleManager
            .suggestVariableName(VariableKind.PARAMETER, null, null, parameters[paramId].getType());

          names.addAll(Arrays.asList(info.names));
        }

        return names;
      }
    };
  }

  private static Expression getSuperTypesExpression(final String psiType, final Collection<PsiClass> psiClasses) {
    return new Expression() {
      public Result calculateResult(ExpressionContext context) {
        return new TextResult(psiType);
      }

      public Result calculateQuickResult(ExpressionContext context) {
        return calculateResult(context);
      }

      public LookupElement[] calculateLookupItems(ExpressionContext context) {
        LinkedHashSet<LookupElement> items = new LinkedHashSet<LookupElement>();
        for (PsiClass psiClass : psiClasses) {
          items.add(LookupItemUtil.objectToLookupItem(psiClass));
        }
        return items.toArray(new LookupElement[items.size()]);
      }
    };
  }


  @Nullable
  private static PsiMethod getExistedSetter(PsiClass currentBeanClass, PsiClass setterPsiClass) {
    PsiClassType psiClassType = JavaPsiFacade.getInstance(setterPsiClass.getProject()).getElementFactory().createType(setterPsiClass);

    for (PsiMethod psiMethod : currentBeanClass.getAllMethods()) {
      if (PropertyUtil.isSimplePropertySetter(psiMethod)) {
        PsiType type = psiMethod.getParameterList().getParameters()[0].getType();
        if (type.isAssignableFrom(psiClassType)) {
          return psiMethod;
        }
      }
    }
    return null;
  }

  @Nullable
  public static Module getSpringModule(@Nonnull PsiClass psiClass) {
    ProjectFileIndex index = ProjectFileIndex.getInstance(psiClass.getProject());

    PsiFile psiFile = psiClass.getContainingFile();

    VirtualFile virtualFile = psiFile.getVirtualFile();

    if (virtualFile == null) return null;

    if (index.isLibraryClassFile(virtualFile) || index.isInLibrarySource(virtualFile)) {
      List<OrderEntry> orderEntries = index.getOrderEntriesForFile(virtualFile);
      for (OrderEntry orderEntry : orderEntries) {
        Module module = orderEntry.getOwnerModule();
        if (isSpringModule(module)) {
          return module;
        }
      }
    }

    Module module = index.getModuleForFile(virtualFile);
    return isSpringModule(module) ? module : null;
  }

  private static boolean isSpringModule(Module module) {
    return module != null && SpringModuleExtension.getInstance(module) != null;
  }

  @Nullable
  public static SpringModel getSpringModel(@Nullable PsiClass psiClass) {
    if (psiClass == null) return null;

    Module module = getSpringModule(psiClass);

    return module != null ? SpringManager.getInstance(psiClass.getProject()).getCombinedModel(module) : null;
  }
}
