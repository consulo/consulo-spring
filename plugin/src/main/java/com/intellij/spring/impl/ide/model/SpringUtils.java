/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.spring.impl.ide.model;

import com.intellij.java.impl.openapi.roots.libraries.JarVersionDetectionUtil;
import com.intellij.java.language.psi.PsiElementFactory;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.java.language.psi.codeStyle.VariableKind;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.spring.impl.DomSpringModelImpl2;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.constants.SpringConstants;
import com.intellij.spring.impl.ide.factories.SpringFactoryBeansManager;
import com.intellij.spring.impl.ide.model.converters.SpringConverterUtil;
import com.intellij.spring.impl.ide.model.values.PlaceholderUtils;
import com.intellij.spring.impl.ide.model.values.PropertyValueConverter;
import com.intellij.spring.impl.ide.model.values.converters.PlaceholderPropertiesConverter;
import com.intellij.spring.impl.ide.model.xml.*;
import com.intellij.spring.impl.ide.model.xml.beans.*;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.application.util.function.Processor;
import consulo.codeEditor.Editor;
import consulo.language.editor.refactoring.rename.SuggestedNameInfo;
import consulo.language.psi.*;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.Sets;
import consulo.util.collection.SmartList;
import consulo.util.dataholder.Key;
import consulo.util.lang.CharFilter;
import consulo.util.lang.Comparing;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ref.Ref;
import consulo.virtualFileSystem.VirtualFile;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.dom.*;
import consulo.xml.dom.reflect.AbstractDomChildrenDescription;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class SpringUtils {
  public static final String SPRING_DELIMITERS = ",; ";

  public static final CharFilter ourFilter = ch -> SPRING_DELIMITERS.indexOf(ch) >= 0;

  private SpringUtils() {
  }

  public static List<String> tokenize(@Nonnull String str) {
    ArrayList<String> list = new ArrayList<String>();
    tokenize(str, list);
    return list;
  }

  public static void tokenize(@Nonnull String str, Collection<String> tokens) {

    StringTokenizer st = new StringTokenizer(str, SPRING_DELIMITERS);
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      token = token.trim();
      if (token.length() > 0) {
        tokens.add(token);
      }
    }
  }

  @Nullable
  public static String getReferencedName(CommonSpringBean bean) {
    SpringModel model = getSpringModelForBean(bean);
    return model != null ? getReferencedName(SpringBeanPointer.createSpringBeanPointer(bean), model.getAllCommonBeans(true)) : null;
  }

  @Nullable
  public static String getReferencedName(SpringBeanPointer bean, Collection<? extends SpringBeanPointer> allBeans) {
    String beanName = bean.getName();
    if (beanName != null) return beanName;

    for (PsiClass psiClass : bean.getEffectiveBeanType()) {
      String className = psiClass.getQualifiedName();
      if (className == null) continue;

      List<SpringBeanPointer> list = findBeansByClassName(allBeans, className);
      if (list.size() == 1) {
        return className;
      }
    }

    return null;
  }


  @Nonnull
  public static List<SpringBeanPointer> findBeansByClassName(@Nonnull Collection<? extends SpringBeanPointer> beans,
                                                             @Nonnull String className) {
    List<SpringBeanPointer> result = new ArrayList<SpringBeanPointer>();
    for (SpringBeanPointer bean : beans) {
      PsiClass beanClass = bean.getBeanClass();
      if (beanClass != null && className.equals(beanClass.getQualifiedName())) result.add(bean);
    }

    return result;
  }

  public static List<SpringModel> getNonEmptySpringModelsByFile(XmlFile file) {
    consulo.module.Module module = ModuleUtilCore.findModuleForPsiElement(file);
    if (module == null) return Collections.singletonList(SpringManager.getInstance(file.getProject()).getSpringModelByFile(file));

    XmlFile originalFile = (XmlFile)file.getOriginalFile();

    return ContainerUtil.findAll(getNonEmptySpringModels(module), springModel -> springModel.getConfigFiles().contains(originalFile));
  }

  private static final Key<CachedValue<List<SpringModel>>> NON_EMPTY_SPRING_MODELS_CACHE = Key.create("NON_EMPTY_SPRING_MODELS_CACHE");

  public static List<SpringModel> getNonEmptySpringModels(@Nonnull final consulo.module.Module module) {
    CachedValue<List<SpringModel>> value = module.getUserData(NON_EMPTY_SPRING_MODELS_CACHE);
    if (value == null) {
      value = CachedValuesManager.getManager(module.getProject()).createCachedValue(new CachedValueProvider<List<SpringModel>>() {
        public Result<List<SpringModel>> compute() {
          return Result.create(computeNonEmptyModels(module), PsiModificationTracker.MODIFICATION_COUNT);
        }
      }, false);
      module.putUserData(NON_EMPTY_SPRING_MODELS_CACHE, value);
    }
    return value.getValue();
  }

  private static List<SpringModel> computeNonEmptyModels(final Module module) {
    Project project = module.getProject();
    final SpringManager manager = SpringManager.getInstance(project);

    final Ref<Boolean> hasModels = Ref.create(false);
    final Ref<Boolean> hasFacets = Ref.create(false);

    final List<SpringModel> result = new SmartList<SpringModel>();
    ModuleUtilCore.visitMeAndDependentModules(module, new Processor<Module>() {
      public boolean process(Module module) {
        if (!hasFacets.get().booleanValue()) {
          hasFacets.set(SpringModuleExtension.getInstance(module) != null);
        }

        List<SpringModel> models = manager.getAllModels(module);
        for (SpringModel model : models) {
          hasModels.set(true);
          result.add(model);
        }
        return true;
      }
    });

    if (result.isEmpty() && !hasModels.get().booleanValue() && hasFacets.get().booleanValue()) {
      List<DomFileElement<Beans>> models = new SmartList<DomFileElement<Beans>>();
      Set<XmlFile> modelFiles = new HashSet<XmlFile>();
      GlobalSearchScope scope =
        GlobalSearchScope.moduleWithDependentsScope(module).intersectWith(GlobalSearchScope.projectScope(project));
      Collection<VirtualFile> files = DomService.getInstance().getDomFileCandidates(Beans.class, project, scope);
      for (VirtualFile virtualFile : files) {
        consulo.module.Module mod = ModuleUtilCore.findModuleForFile(virtualFile, project);
        if (mod == null || SpringModuleExtension.getInstance(mod) == null) continue;

        PsiFile file1 = PsiManager.getInstance(project).findFile(virtualFile);
        if (file1 instanceof XmlFile) {
          XmlFile xmlFile = (XmlFile)file1;
          modelFiles.add(xmlFile);
          DomFileElement<Beans> element = DomManager.getDomManager(project).getFileElement(xmlFile, Beans.class);
          ContainerUtil.addIfNotNull(models, element);
        }
      }
      if (models.isEmpty()) return Collections.emptyList();

      final DomFileElement<Beans> merged = DomService.getInstance().createModelMerger().mergeModels(DomFileElement.class, models);
      return Collections.<SpringModel>singletonList(new DomSpringModelImpl2(merged, modelFiles, module, null) {
        @Override
        public String toString() {
          return "No fileset mock model";
        }
      });
    }

    return result;
  }

  @Nonnull
  public static SpringModel getSpringModel(SpringModelElement modelElement) {
    Project project = modelElement.getManager().getProject();

    SpringModel model = SpringManager.getInstance(project).getSpringModelByFile(DomUtil.getFile(modelElement));
    assert model != null;
    return model;
  }

  @Nullable
  private static SpringModel getSpringModelForBean(CommonSpringBean springBean) {
    if (springBean instanceof SpringModelElement) {
      return getSpringModel((SpringModelElement)springBean);
    }
    else {
      consulo.module.Module module = springBean.getModule();
      return module == null ? null : SpringManager.getInstance(module.getProject()).getCombinedModel(module);
    }
  }

  @Nullable
  public static SpringPropertyDefinition findPropertyByName(@Nonnull CommonSpringBean bean, @NonNls @Nonnull String propertyName) {
    return findPropertyByName(bean, propertyName, true);
  }

  @Nullable
  public static SpringPropertyDefinition findPropertyByName(@Nonnull CommonSpringBean bean,
                                                            @Nonnull final String propertyName,
                                                            boolean searchInParentBean) {
    for (SpringPropertyDefinition property : getProperties(bean)) {
      if (propertyName.equals(property.getPropertyName())) {
        return property;
      }
    }
    final Ref<SpringPropertyDefinition> ref = new Ref<SpringPropertyDefinition>();
    if (searchInParentBean && bean instanceof SpringBean) {
      visitParents((SpringBean)bean, true, new Processor<SpringBean>() {
        public boolean process(SpringBean springBean) {
          ref.set(findPropertyByName(springBean, propertyName, false));
          return ref.get() == null;
        }
      });
    }
    return ref.get();
  }

  @Nullable
  public static String getStringPropertyValue(@Nonnull SpringPropertyDefinition property) {
    GenericDomValue<?> element = property.getValueElement();
    return element == null ? null : element.getStringValue();
  }

  @Nonnull
  public static Collection<String> getValueVariants(@Nonnull SpringPropertyDefinition property) {
    GenericDomValue value = getPropertyDomValue(property);
    if (value == null) return Collections.emptyList();
    return getValueVariants(value);
  }

  @Nonnull
  public static Collection<String> getValueVariants(GenericDomValue value) {
    String stringValue = value.getStringValue();
    if (StringUtil.isEmpty(stringValue)) return Collections.emptyList();
    Converter converter = ((PropertyValueConverter)value.getConverter()).getConverter(value);
    if (!(converter instanceof PlaceholderPropertiesConverter)) return Collections.singletonList(stringValue);

    return PlaceholderUtils.getExpandedVariants(value);
  }

  @Nullable
  public static GenericDomValue<?> getPropertyDomValue(@Nonnull SpringPropertyDefinition property) {
    GenericDomValue<?> valueElement = property.getValueElement();
    return valueElement != null && valueElement.getStringValue() == null ? null : valueElement;
  }

  public static List<SpringValueHolderDefinition> getValueHolders(@Nonnull CommonSpringBean bean) {
    return bean instanceof DomSpringBean
      ? DomUtil.getDefinedChildrenOfType((DomElement)bean, SpringValueHolderDefinition.class)
      : Collections.<SpringValueHolderDefinition>emptyList();
  }

  public static List<CommonSpringBean> getChildBeans(@Nonnull DomElement parent, boolean includeParsedCustomBeanWrappers) {
    ArrayList<CommonSpringBean> result = new ArrayList<CommonSpringBean>();
    for (DomSpringBean bean : DomUtil.getDefinedChildrenOfType(parent, DomSpringBean.class)) {
      if (bean instanceof CustomBeanWrapper) {
        if (includeParsedCustomBeanWrappers || !((CustomBeanWrapper)bean).isParsed()) {
          result.add(bean);
        }
        result.addAll(((CustomBeanWrapper)bean).getCustomBeans());
      }
      else {
        result.add(bean);
      }
    }
    return result;
  }

  public static List<SpringPropertyDefinition> getProperties(@Nonnull CommonSpringBean bean) {
    return bean instanceof DomSpringBean
      ? DomUtil.getDefinedChildrenOfType((DomElement)bean, SpringPropertyDefinition.class)
      : Collections.<SpringPropertyDefinition>emptyList();
  }

  public static List<ConstructorArg> getConstructorArgs(@Nonnull CommonSpringBean bean) {
    return bean instanceof SpringBean ? ((SpringBean)bean).getConstructorArgs() : Collections.<ConstructorArg>emptyList();
  }

  @Nullable
  public static Pair<String, PsiElement> getPropertyValue(@Nonnull SpringPropertyDefinition property) {
    GenericDomValue<?> value = getPropertyDomValue(property);
    return value == null ? null : Pair.<String, PsiElement>create(value.getStringValue(), DomUtil.getValueElement(value));
  }

  public static boolean isEffectiveClassType(SpringInjection injection, PsiType requiredType) {
    PsiType[] types = injection.getTypesByValue();
    if (types == null) {
      return false;
    }
    for (PsiType valueType : types) {

      if (valueType instanceof PsiClassType) {
        SpringBeanPointer springBean = getReferencedSpringBean(injection);

        if (springBean != null && isEffectiveClassType(requiredType, springBean.getSpringBean())) {
          return true;
        }
      }
    }

    return false;
  }

  public static boolean isEffectiveClassType(CommonSpringBean context, PsiType requiredType, Module module) {
    PsiClass[] effectiveBeanTypes = getEffectiveBeanTypes(context);

    PsiClass psiClass = resolvePsiClass(requiredType, module);
    if (psiClass != null) {
      for (PsiClass aClass : effectiveBeanTypes) {
        if (InheritanceUtil.isInheritorOrSelf(aClass, psiClass, true)) return true;
      }
      return isUnusualBeanFactoriesUsed(context);
    }
    return false;
  }

  private static boolean isUnusualBeanFactoriesUsed(CommonSpringBean context) {
    PsiClass beanClass = context.getBeanClass();
    if (beanClass != null && SpringFactoryBeansManager.isBeanFactory(beanClass)) {
      SpringFactoryBeansManager manager = SpringFactoryBeansManager.getInstance();

      // unregistered factory or unknown product types  (for instance, IDEADEV-30892)
      return !manager.isFactoryRegistered(beanClass) || manager.getProductTypeClassNames(beanClass, context).size() == 0;
    }

    return false;
  }

  public static boolean isEffectiveClassType(PsiType psiType, @Nonnull CommonSpringBean context) {
    return isEffectiveClassType(context, psiType, context.getModule());
  }

  @Nullable
  private static PsiClass resolvePsiClass(PsiType psiType, Module module) {
    if (psiType instanceof PsiClassType) {
      return ((PsiClassType)psiType).resolve();
    }
    if (psiType instanceof PsiPrimitiveType) {
      if (module != null) {
        GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false);
        PsiManager psiManager = PsiManager.getInstance(module.getProject());

        PsiClassType boxedType = ((PsiPrimitiveType)psiType).getBoxedType(psiManager, scope);
        if (boxedType != null) {
          return boxedType.resolve();
        }
      }
    }
    return null;
  }

  @Nullable
  public static SpringBeanPointer getReferencedSpringBean(SpringPropertyDefinition definition) {
    GenericDomValue<SpringBeanPointer> element = definition.getRefElement();
    if (element != null) {
      SpringBeanPointer springBeanPointer = element.getValue();
      if (springBeanPointer != null) return springBeanPointer;
    }
    return definition instanceof SpringInjection ? getReferencedSpringBean((SpringInjection)definition) : null;
  }

  @Nullable
  public static SpringBeanPointer getReferencedSpringBean(SpringInjection injection) {
    SpringBeanPointer refAttrPointer = injection.getRefAttr().getValue();
    if (refAttrPointer != null) {
      return refAttrPointer;
    }
    else if (DomUtil.hasXml(injection.getRef())) {
      SpringRef springRef = injection.getRef();

      SpringBeanPointer beanPointer = springRef.getBean().getValue();
      if (beanPointer != null) {
        return beanPointer;
      }
      else {
        SpringBeanPointer localPointer = springRef.getLocal().getValue();
        if (localPointer != null) {
          return localPointer;
        }
        else {
          SpringBeanPointer parentPointer = springRef.getParentAttr().getValue();
          if (parentPointer != null) {
            return parentPointer;
          }
        }
      }
    }
    else if (DomUtil.hasXml(injection.getBean())) {
      return SpringBeanPointer.createSpringBeanPointer(injection.getBean());
    }

    return null;
  }

  public static String[] suggestBeanNames(@Nullable CommonSpringBean springBean) {
    if (springBean != null) {
      PsiClass beanClass = springBean.getBeanClass();
      if (beanClass != null) {
        SpringModel model = getSpringModelForBean(springBean);
        JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(beanClass.getProject());
        PsiElementFactory elementFactory = JavaPsiFacade.getInstance(beanClass.getProject()).getElementFactory();

        LinkedHashSet<String> initialNames = new LinkedHashSet<String>();

        PsiClass[] productClasses = getEffectiveBeanTypes(springBean);
        if (productClasses.length > 0) {
          for (PsiClass productClass : productClasses) {
            PsiClassType classType = elementFactory.createType(productClass);
            SuggestedNameInfo nameInfo = codeStyleManager.suggestVariableName(VariableKind.PARAMETER, null, null, classType);
            initialNames.addAll(Arrays.asList(nameInfo.names));
          }
        }
        else {
          PsiClassType classType = elementFactory.createType(beanClass);
          SuggestedNameInfo nameInfo = codeStyleManager.suggestVariableName(VariableKind.PARAMETER, null, null, classType);
          initialNames.addAll(Arrays.asList(nameInfo.names));
        }

        List<String> uniqueNames = new ArrayList<String>();
        for (String name : initialNames) {
          String suggestedName = name;
          int i = 1;
          while (model.findBean(suggestedName) != null || uniqueNames.contains(suggestedName)) {
            suggestedName = name + (++i);
          }
          uniqueNames.add(suggestedName);
        }
        return ArrayUtil.toStringArray(uniqueNames);
      }
    }
    return new String[0];
  }

  /**
   * @return bean type or product type (for factoryBeans). Multiple values if factory product proxies multiple interfaces
   */
  @Nonnull
  public static PsiClass[] getEffectiveBeanTypes(@Nonnull CommonSpringBean bean) {
    PsiClass beanClass = bean.getBeanClass();
    Collection<PsiClass> effectiveTypes = new HashSet<PsiClass>();
    ContainerUtil.addIfNotNull(effectiveTypes, beanClass);

    for (SpringBeanEffectiveTypeProvider provider : SpringBeanEffectiveTypeProvider.EP_NAME.getExtensionList()) {
      provider.processEffectiveTypes(bean, effectiveTypes);
    }

    return effectiveTypes.toArray(new PsiClass[effectiveTypes.size()]);
  }

  @Nullable
  public static SpringBean getSpringBeanForCurrentCaretPosition(Editor editor, PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    PsiElement element = file.findElementAt(offset);
    return findBeanFromPsiElement(element);
  }

  @Nullable
  public static SpringBean findBeanFromPsiElement(PsiElement element) {
    return DomUtil.findDomElement(element, SpringBean.class);
  }

  @Nonnull
  public static String getPresentationBeanName(SpringBeanPointer pointer) {
    String beanName = pointer.getName();

    if (beanName == null) {
      PsiClass psiClass = pointer.getBeanClass();
      if (psiClass != null) beanName = psiClass.getName();

      CommonSpringBean springBean = pointer.getSpringBean();
      if (springBean instanceof SpringBean) {
        String unresolvedClassName = ((SpringBean)springBean).getClazz().getStringValue();
        if (unresolvedClassName != null) beanName = unresolvedClassName;
      }
    }
    return beanName == null ? SpringBundle.message("spring.bean.dependency.graph.node.unknown") : beanName;
  }

  public static List<SpringBeanPointer> getSetterDependencies(CommonSpringBean springBean) {

    List<SpringBeanPointer> dependencies = new ArrayList<SpringBeanPointer>();
    if (springBean instanceof DomSpringBean) {
      for (SpringPropertyDefinition property : getProperties((DomSpringBean)springBean)) {
        if (property instanceof SpringValueHolder) {
          dependencies.addAll(getSpringValueHolderDependencies(property));
        }
      }
    }
    return dependencies;
  }

  public static List<SpringBeanPointer> getConstructorDependencies(CommonSpringBean springBean) {
    List<SpringBeanPointer> dependencies = new ArrayList<SpringBeanPointer>();
    if (springBean instanceof DomSpringBean) {
      for (ConstructorArg arg : getConstructorArgs((DomSpringBean)springBean)) {
        dependencies.addAll(getSpringValueHolderDependencies(arg));
      }
    }
    return dependencies;
  }

  public static List<SpringBaseBeanPointer> getSpringValueHolderDependencies(SpringValueHolderDefinition valueHolder) {
    Set<SpringBaseBeanPointer> beans = new LinkedHashSet<SpringBaseBeanPointer>();
    addValueHolder(valueHolder, beans);
    return new ArrayList<SpringBaseBeanPointer>(beans);

  }

  private static void addValueHolder(SpringValueHolderDefinition definition, Set<SpringBaseBeanPointer> beans) {
    GenericDomValue<SpringBeanPointer> element = definition.getRefElement();
    if (element != null) {
      addBasePointer(element, beans);
    }

    if (definition instanceof SpringValueHolder) {
      SpringValueHolder valueHolder = (SpringValueHolder)definition;
      addSpringRefBeans(valueHolder.getRef(), beans);
      addIdrefBeans(valueHolder.getIdref(), beans);

      if (DomUtil.hasXml(valueHolder.getList())) {
        addCollectionReferences(valueHolder.getList(), beans);
      }
      if (DomUtil.hasXml(valueHolder.getSet())) {
        addCollectionReferences(valueHolder.getSet(), beans);
      }

      if (DomUtil.hasXml(valueHolder.getMap())) {
        addMapReferences(valueHolder.getMap(), beans);
      }

      if (DomUtil.hasXml(valueHolder.getBean())) {
        beans.add(DomSpringBeanPointer.createDomSpringBeanPointer(valueHolder.getBean()));
      }
    }
  }

  private static void addBasePointer(GenericValue<SpringBeanPointer> value, Collection<SpringBaseBeanPointer> beans) {
    ContainerUtil.addIfNotNull(beans, getBasePointer(value.getValue()));
  }

  private static void addMapReferences(SpringMap map, Set<SpringBaseBeanPointer> beans) {
    for (SpringEntry entry : map.getEntries()) {
      addValueHolder(entry, beans);
    }
  }

  private static void addIdrefBeans(Idref idref, Set<SpringBaseBeanPointer> beans) {
    addBasePointer(idref.getLocal(), beans);
    addBasePointer(idref.getBean(), beans);
  }

  private static void addSpringRefBeans(SpringRef springRef, Set<SpringBaseBeanPointer> beans) {
    if (DomUtil.hasXml(springRef)) {
      addBasePointer(springRef.getBean(), beans);
      addBasePointer(springRef.getLocal(), beans);
    }
  }

  private static void addCollectionReferences(CollectionElements elements, Set<SpringBaseBeanPointer> beans) {
    for (SpringRef springRef : elements.getRefs()) {
      addSpringRefBeans(springRef, beans);
    }
    for (Idref idref : elements.getIdrefs()) {
      addIdrefBeans(idref, beans);
    }
    for (ListOrSet listOrSet : elements.getLists()) {
      addCollectionReferences(listOrSet, beans);
    }
    for (ListOrSet listOrSet : elements.getSets()) {
      addCollectionReferences(listOrSet, beans);
    }
    for (SpringBean innerBean : elements.getBeans()) {
      beans.add(DomSpringBeanPointer.createDomSpringBeanPointer(innerBean));
    }
    for (SpringMap map : elements.getMaps()) {
      addMapReferences(map, beans);
    }
  }

  @Nullable
  public static PsiFile getContainingFile(CommonSpringBean springBean) {
    return springBean.getContainingFile();
  }

  public static boolean isAssignable(Project project, PsiClassType type, String className) {
    PsiClass psiClass = type.resolve();
    if (psiClass != null) {
      PsiManager psiManager = PsiManager.getInstance(project);
      PsiClass required =
        JavaPsiFacade.getInstance(psiManager.getProject()).findClass(className, GlobalSearchScope.allScope(project));
      if (required != null && InheritanceUtil.isInheritorOrSelf(psiClass, required, true)) {
        return true;
      }
    }

    return false;
  }

  @Nullable
  public static SpringBeanPointer getBeanPointer(SpringModel model, @NonNls @Nonnull String beanName) {
    String beanReferenceName = beanName.startsWith("&") ? beanName.substring(1) : beanName;  // IDEADEV-13855
    return findBean(model, beanReferenceName);
  }

  @Nullable
  public static SpringBeanPointer findBean(SpringModel model, String beanName) {
    if (StringUtil.isEmptyOrSpaces(beanName)) return null;

    return model.findBean(beanName);
  }

  public static boolean isCompiled(@Nonnull PsiElement element) {
    return element instanceof PsiCompiledElement || element.getOriginalElement() instanceof PsiCompiledElement;
  }

  public static void navigate(@Nullable DomElement domElement) {
    if (domElement == null) return;

    DomElementsNavigationManager navigationManager = DomElementsNavigationManager.getManager(domElement.getManager().getProject());
    navigationManager.getDomElementsNavigateProvider(DomElementsNavigationManager.DEFAULT_PROVIDER_NAME).navigate(domElement, true);
  }

  public static List<SpringBaseBeanPointer> getSpringBeans(PsiClass aClass) {
    List<SpringBaseBeanPointer> beans = new SmartList<SpringBaseBeanPointer>();
    consulo.module.Module module = ModuleUtilCore.findModuleForPsiElement(aClass);
    if (module != null) {
      SpringModel springModel = SpringManager.getInstance(aClass.getProject()).getCombinedModel(module);
      if (springModel != null) {
        beans.addAll(springModel.findBeansByPsiClass(aClass));
      }
    }
    return beans;
  }

  public static List<PsiType> resolveGenerics(PsiClassType classType) {
    List<PsiType> generics = new ArrayList<PsiType>();
    PsiClassType.ClassResolveResult resolveResult = classType.resolveGenerics();
    PsiClass psiClass = resolveResult.getElement();
    PsiSubstitutor substitutor = resolveResult.getSubstitutor();
    if (psiClass != null && substitutor != null) {
      for (PsiTypeParameter typeParameter : psiClass.getTypeParameters()) {
        generics.add(substitutor.substitute(typeParameter));

      }
    }
    return generics;
  }

  public static boolean isCollectionType(PsiType psiType, Project project) {
    PsiType collectionType = SpringConverterUtil.findType(Collection.class, project);

    return collectionType != null && collectionType.isAssignableFrom(psiType);
  }

  @Nullable
  public static PsiType getGenericCollectonType(PsiType psiType) {
    List<PsiType> psiTypes = resolveGenerics((PsiClassType)psiType);
    return psiTypes.size() == 1 ? psiTypes.get(0) : null;
  }

  @Nullable
  public static SpringBaseBeanPointer getBasePointer(SpringBeanPointer springBeanPointer) {
    return springBeanPointer == null ? null : springBeanPointer.getBasePointer();
  }

  @Nonnull
  public static SpringBean getTopLevelBean(@Nonnull SpringBean bean) {
    DomElement parent = bean.getParent();
    if (parent instanceof Beans) {
      return bean;
    }
    else {
      assert parent != null;
      SpringBean parentBean = bean.getParentOfType(SpringBean.class, true);
      return getTopLevelBean(parentBean);
    }
  }

  public static boolean visitParents(SpringBean springBean, boolean strict, Processor<SpringBean> processor) {
    SpringBeanPointer parent = springBean.getParentBean().getValue();
    if (parent == null) {
      return true;
    }
    HashSet<CommonSpringBean> visited = new HashSet<CommonSpringBean>();
    if (!strict) {
      visited.add(springBean);
      if (!processor.process(springBean)) {
        return false;
      }
    }
    CommonSpringBean bean = parent.getSpringBean();
    while (bean instanceof SpringBean) {
      if (!processor.process((SpringBean)bean)) {
        return false;
      }

      parent = ((SpringBean)bean).getParentBean().getValue();
      if (parent == null) {
        return true;
      }
      bean = parent.getSpringBean();
      if (visited.contains(bean)) {
        return true;
      }
      visited.add(bean);
    }
    return true;
  }

  public static <T extends GenericDomValue<?>> T getMergedValue(SpringBean springBean, T value) {
    final AbstractDomChildrenDescription description = value.getChildDescription();
    final Ref<T> ref = new Ref<T>(value);
    visitParents(springBean, false, new Processor<SpringBean>() {
      public boolean process(SpringBean springBean) {
        List<? extends DomElement> list = description.getValues(springBean);
        if (list.size() == 1) {
          T t = (T)list.get(0);
          if (DomUtil.hasXml(t)) {
            ref.set(t);
            return false;
          }
        }
        return true;
      }
    });
    return ref.get();
  }

  public static <T extends DomElement> Set<T> getMergedSet(SpringBean springBean, Function<SpringBean, Collection<T>> getter) {
    Set<T> set = new HashSet<T>(getter.apply(springBean));
    visitParents(springBean, false, springBean1 -> {
      set.addAll(getter.apply(springBean1));
      return true;
    });
    return set;
  }

  public static boolean compareQualifiers(SpringQualifier one, SpringQualifier two) {
    if (!Comparing.equal(one.getQualifierType(), two.getQualifierType())) return false;
    if (!Comparing.equal(one.getQualifierValue(), two.getQualifierValue())) return false;
    List<? extends QualifierAttribute> list1 = one.getQualifierAttributes();
    int size1 = list1.size();
    List<? extends QualifierAttribute> list2 = two.getQualifierAttributes();
    int size2 = list2.size();
    if (size1 != size2) return false;
    if (size1 == 0) return true;
    Set<QualifierAttribute> set = Sets.newHashSet(QualifierAttribute.HASHING_STRATEGY);
    set.addAll(list1);
    return set.containsAll(list2);
  }

  @Nonnull
  public static List<SpringBaseBeanPointer> getBeansByType(PsiType type, SpringModel model) {
    if (type instanceof PsiClassType) {
      PsiClass psiClass = ((PsiClassType)type).resolve();
      if (psiClass != null) {
        return model.findBeansByEffectivePsiClassWithInheritance(psiClass);
      }
    }
    return Collections.emptyList();
  }

  public static boolean isSpring25(@Nonnull Module module) {
    String s = JarVersionDetectionUtil.detectJarVersion(SpringConstants.SPRING_VERSION_CLASS, module);
    boolean b = s != null && !s.startsWith("1.") && !s.startsWith("2.0") && !s.startsWith("2.1");
    return b;
  }

  @Nonnull
  public static Set<String> getListOrSetValues(@Nonnull SpringPropertyDefinition property) {
    if (property instanceof SpringProperty) {
      SpringProperty springProperty = (SpringProperty)property;
      if (DomUtil.hasXml(springProperty.getList())) {
        return getValues(springProperty.getList());
      }
      else if (DomUtil.hasXml(springProperty.getSet())) {
        return getValues(springProperty.getSet());
      }
    }
    return Collections.emptySet();
  }

  @Nonnull
  public static Set<String> getValues(@Nonnull ListOrSet listOrSet) {
    Set<String> values = new HashSet<String>();
    for (SpringValue value : listOrSet.getValues()) {
      if (value.getValue() != null) {
        values.add(value.getStringValue());
      }
    }
    return values;
  }

  public static Set<String> getAllBeanNames(SpringBean bean) {
    String beanName = bean.getBeanName();
    return beanName == null ? Collections.<String>emptySet() : getSpringModel(bean).getAllBeanNames(beanName);
  }

  public static Set<String> getAllBeanNames(CommonSpringBean bean) {
    String beanName = bean.getBeanName();
    if (beanName == null) {
      return Collections.emptySet();
    }
    SpringModel model = getSpringModelForBean(bean);
    return model == null ? Collections.<String>emptySet() : model.getAllBeanNames(beanName);
  }

  @Nullable
  public static SpringModel getModelByPsiElement(PsiElement element) {
    if (element == null) {
      return null;
    }
    Module module = ModuleUtilCore.findModuleForPsiElement(element);
    return module == null ? null : SpringManager.getInstance(element.getProject()).getCombinedModel(module);
  }
}
