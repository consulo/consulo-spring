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
import com.intellij.spring.impl.DomSpringModelImpl;
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
import consulo.util.lang.function.Condition;
import consulo.util.lang.ref.Ref;
import consulo.virtualFileSystem.VirtualFile;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.util.xml.*;
import consulo.xml.util.xml.reflect.AbstractDomChildrenDescription;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class SpringUtils {
  public static final String SPRING_DELIMITERS = ",; ";

  public static final CharFilter ourFilter = ch -> SPRING_DELIMITERS.indexOf(ch) >= 0;

  private SpringUtils() {
  }

  public static List<String> tokenize(@Nonnull String str) {
    final ArrayList<String> list = new ArrayList<String>();
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
  public static String getReferencedName(final CommonSpringBean bean) {
    final SpringModel model = getSpringModelForBean(bean);
    return model != null ? getReferencedName(SpringBeanPointer.createSpringBeanPointer(bean), model.getAllCommonBeans(true)) : null;
  }

  @Nullable
  public static String getReferencedName(final SpringBeanPointer bean, final Collection<? extends SpringBeanPointer> allBeans) {
    final String beanName = bean.getName();
    if (beanName != null) return beanName;

    for (PsiClass psiClass : bean.getEffectiveBeanType()) {
      final String className = psiClass.getQualifiedName();
      if (className == null) continue;

      final List<SpringBeanPointer> list = findBeansByClassName(allBeans, className);
      if (list.size() == 1) {
        return className;
      }
    }

    return null;
  }


  @Nonnull
  public static List<SpringBeanPointer> findBeansByClassName(@Nonnull final Collection<? extends SpringBeanPointer> beans,
                                                             @Nonnull String className) {
    List<SpringBeanPointer> result = new ArrayList<SpringBeanPointer>();
    for (SpringBeanPointer bean : beans) {
      final PsiClass beanClass = bean.getBeanClass();
      if (beanClass != null && className.equals(beanClass.getQualifiedName())) result.add(bean);
    }

    return result;
  }

  public static List<SpringModel> getNonEmptySpringModelsByFile(final XmlFile file) {
    final consulo.module.Module module = ModuleUtilCore.findModuleForPsiElement(file);
    if (module == null) return Collections.singletonList(SpringManager.getInstance(file.getProject()).getSpringModelByFile(file));

    final XmlFile originalFile = (XmlFile)file.getOriginalFile();

    return ContainerUtil.findAll(getNonEmptySpringModels(module), new Condition<SpringModel>() {
      public boolean value(final SpringModel springModel) {
        return springModel.getConfigFiles().contains(originalFile);
      }
    });
  }

  private static final Key<CachedValue<List<SpringModel>>> NON_EMPTY_SPRING_MODELS_CACHE = Key.create("NON_EMPTY_SPRING_MODELS_CACHE");

  public static List<SpringModel> getNonEmptySpringModels(@Nonnull final consulo.module.Module module) {
    CachedValue<List<SpringModel>> value = module.getUserData(NON_EMPTY_SPRING_MODELS_CACHE);
    if (value == null) {
      value = CachedValuesManager.getManager(module.getProject()).createCachedValue(new CachedValueProvider<List<SpringModel>>() {
        public Result<List<SpringModel>> compute() {
          return Result.create(computeNonEmptyModels(module), PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
        }
      }, false);
      module.putUserData(NON_EMPTY_SPRING_MODELS_CACHE, value);
    }
    return value.getValue();
  }

  private static List<SpringModel> computeNonEmptyModels(final Module module) {
    final Project project = module.getProject();
    final SpringManager manager = SpringManager.getInstance(project);

    final Ref<Boolean> hasModels = Ref.create(false);
    final Ref<Boolean> hasFacets = Ref.create(false);

    final List<SpringModel> result = new SmartList<SpringModel>();
    ModuleUtilCore.visitMeAndDependentModules(module, new Processor<Module>() {
      public boolean process(final Module module) {
        if (!hasFacets.get().booleanValue()) {
          hasFacets.set(SpringModuleExtension.getInstance(module) != null);
        }

        final List<SpringModel> models = manager.getAllModels(module);
        for (final SpringModel model : models) {
          hasModels.set(true);
          result.add(model);
        }
        return true;
      }
    });

    if (result.isEmpty() && !hasModels.get().booleanValue() && hasFacets.get().booleanValue()) {
      List<DomFileElement<Beans>> models = new SmartList<DomFileElement<Beans>>();
      Set<XmlFile> modelFiles = new HashSet<XmlFile>();
      final GlobalSearchScope scope =
        GlobalSearchScope.moduleWithDependentsScope(module).intersectWith(GlobalSearchScope.projectScope(project));
      final Collection<VirtualFile> files = DomService.getInstance().getDomFileCandidates(Beans.class, project, scope);
      for (final VirtualFile virtualFile : files) {
        final consulo.module.Module mod = ModuleUtilCore.findModuleForFile(virtualFile, project);
        if (mod == null || SpringModuleExtension.getInstance(mod) == null) continue;

        final PsiFile file1 = PsiManager.getInstance(project).findFile(virtualFile);
        if (file1 instanceof XmlFile) {
          final XmlFile xmlFile = (XmlFile)file1;
          modelFiles.add(xmlFile);
          final DomFileElement<Beans> element = DomManager.getDomManager(project).getFileElement(xmlFile, Beans.class);
          ContainerUtil.addIfNotNull(models, element);
        }
      }
      if (models.isEmpty()) return Collections.emptyList();

      final DomFileElement<Beans> merged = DomService.getInstance().createModelMerger().mergeModels(DomFileElement.class, models);
      return Collections.<SpringModel>singletonList(new DomSpringModelImpl(merged, modelFiles, module, null) {
        @Override
        public String toString() {
          return "No fileset mock model";
        }
      });
    }

    return result;
  }

  @Nonnull
  public static SpringModel getSpringModel(final SpringModelElement modelElement) {
    final Project project = modelElement.getManager().getProject();

    final SpringModel model = SpringManager.getInstance(project).getSpringModelByFile(DomUtil.getFile(modelElement));
    assert model != null;
    return model;
  }

  @Nullable
  private static SpringModel getSpringModelForBean(final CommonSpringBean springBean) {
    if (springBean instanceof SpringModelElement) {
      return getSpringModel((SpringModelElement)springBean);
    }
    else {
      final consulo.module.Module module = springBean.getModule();
      return module == null ? null : SpringManager.getInstance(module.getProject()).getCombinedModel(module);
    }
  }

  @Nullable
  public static SpringPropertyDefinition findPropertyByName(@Nonnull final CommonSpringBean bean, @NonNls @Nonnull String propertyName) {
    return findPropertyByName(bean, propertyName, true);
  }

  @Nullable
  public static SpringPropertyDefinition findPropertyByName(@Nonnull final CommonSpringBean bean,
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
    final GenericDomValue<?> element = property.getValueElement();
    return element == null ? null : element.getStringValue();
  }

  @Nonnull
  public static Collection<String> getValueVariants(@Nonnull SpringPropertyDefinition property) {
    final GenericDomValue value = getPropertyDomValue(property);
    if (value == null) return Collections.emptyList();
    return getValueVariants(value);
  }

  @Nonnull
  public static Collection<String> getValueVariants(final GenericDomValue value) {
    final String stringValue = value.getStringValue();
    if (StringUtil.isEmpty(stringValue)) return Collections.emptyList();
    final Converter converter = ((PropertyValueConverter)value.getConverter()).getConverter(value);
    if (!(converter instanceof PlaceholderPropertiesConverter)) return Collections.singletonList(stringValue);

    return PlaceholderUtils.getExpandedVariants(value);
  }

  @Nullable
  public static GenericDomValue<?> getPropertyDomValue(@Nonnull SpringPropertyDefinition property) {
    final GenericDomValue<?> valueElement = property.getValueElement();
    return valueElement != null && valueElement.getStringValue() == null ? null : valueElement;
  }

  public static List<SpringValueHolderDefinition> getValueHolders(@Nonnull CommonSpringBean bean) {
    return bean instanceof DomSpringBean
      ? DomUtil.getDefinedChildrenOfType((DomElement)bean, SpringValueHolderDefinition.class)
      : Collections.<SpringValueHolderDefinition>emptyList();
  }

  public static List<CommonSpringBean> getChildBeans(@Nonnull DomElement parent, final boolean includeParsedCustomBeanWrappers) {
    final ArrayList<CommonSpringBean> result = new ArrayList<CommonSpringBean>();
    for (final DomSpringBean bean : DomUtil.getDefinedChildrenOfType(parent, DomSpringBean.class)) {
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
    final GenericDomValue<?> value = getPropertyDomValue(property);
    return value == null ? null : Pair.<String, PsiElement>create(value.getStringValue(), DomUtil.getValueElement(value));
  }

  public static boolean isEffectiveClassType(final SpringInjection injection, final PsiType requiredType) {
    PsiType[] types = injection.getTypesByValue();
    if (types == null) {
      return false;
    }
    for (PsiType valueType : types) {

      if (valueType instanceof PsiClassType) {
        final SpringBeanPointer springBean = getReferencedSpringBean(injection);

        if (springBean != null && isEffectiveClassType(requiredType, springBean.getSpringBean())) {
          return true;
        }
      }
    }

    return false;
  }

  public static boolean isEffectiveClassType(final CommonSpringBean context, final PsiType requiredType, final Module module) {
    final PsiClass[] effectiveBeanTypes = getEffectiveBeanTypes(context);

    final PsiClass psiClass = resolvePsiClass(requiredType, module);
    if (psiClass != null) {
      for (PsiClass aClass : effectiveBeanTypes) {
        if (InheritanceUtil.isInheritorOrSelf(aClass, psiClass, true)) return true;
      }
      return isUnusualBeanFactoriesUsed(context);
    }
    return false;
  }

  private static boolean isUnusualBeanFactoriesUsed(final CommonSpringBean context) {
    final PsiClass beanClass = context.getBeanClass();
    if (beanClass != null && SpringFactoryBeansManager.isBeanFactory(beanClass)) {
      final SpringFactoryBeansManager manager = SpringFactoryBeansManager.getInstance();

      // unregistered factory or unknown product types  (for instance, IDEADEV-30892)
      return !manager.isFactoryRegistered(beanClass) || manager.getProductTypeClassNames(beanClass, context).size() == 0;
    }

    return false;
  }

  public static boolean isEffectiveClassType(final PsiType psiType, @Nonnull final CommonSpringBean context) {
    return isEffectiveClassType(context, psiType, context.getModule());
  }

  @Nullable
  private static PsiClass resolvePsiClass(final PsiType psiType, final Module module) {
    if (psiType instanceof PsiClassType) {
      return ((PsiClassType)psiType).resolve();
    }
    if (psiType instanceof PsiPrimitiveType) {
      if (module != null) {
        final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false);
        final PsiManager psiManager = PsiManager.getInstance(module.getProject());

        final PsiClassType boxedType = ((PsiPrimitiveType)psiType).getBoxedType(psiManager, scope);
        if (boxedType != null) {
          return boxedType.resolve();
        }
      }
    }
    return null;
  }

  @Nullable
  public static SpringBeanPointer getReferencedSpringBean(SpringPropertyDefinition definition) {
    final GenericDomValue<SpringBeanPointer> element = definition.getRefElement();
    if (element != null) {
      final SpringBeanPointer springBeanPointer = element.getValue();
      if (springBeanPointer != null) return springBeanPointer;
    }
    return definition instanceof SpringInjection ? getReferencedSpringBean((SpringInjection)definition) : null;
  }

  @Nullable
  public static SpringBeanPointer getReferencedSpringBean(SpringInjection injection) {
    final SpringBeanPointer refAttrPointer = injection.getRefAttr().getValue();
    if (refAttrPointer != null) {
      return refAttrPointer;
    }
    else if (DomUtil.hasXml(injection.getRef())) {
      final SpringRef springRef = injection.getRef();

      final SpringBeanPointer beanPointer = springRef.getBean().getValue();
      if (beanPointer != null) {
        return beanPointer;
      }
      else {
        final SpringBeanPointer localPointer = springRef.getLocal().getValue();
        if (localPointer != null) {
          return localPointer;
        }
        else {
          final SpringBeanPointer parentPointer = springRef.getParentAttr().getValue();
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
      final PsiClass beanClass = springBean.getBeanClass();
      if (beanClass != null) {
        final SpringModel model = getSpringModelForBean(springBean);
        final JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(beanClass.getProject());
        final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(beanClass.getProject()).getElementFactory();

        final LinkedHashSet<String> initialNames = new LinkedHashSet<String>();

        final PsiClass[] productClasses = getEffectiveBeanTypes(springBean);
        if (productClasses.length > 0) {
          for (PsiClass productClass : productClasses) {
            final PsiClassType classType = elementFactory.createType(productClass);
            SuggestedNameInfo nameInfo = codeStyleManager.suggestVariableName(VariableKind.PARAMETER, null, null, classType);
            initialNames.addAll(Arrays.asList(nameInfo.names));
          }
        }
        else {
          final PsiClassType classType = elementFactory.createType(beanClass);
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
  public static PsiClass[] getEffectiveBeanTypes(@Nonnull final CommonSpringBean bean) {
    final PsiClass beanClass = bean.getBeanClass();
    Collection<PsiClass> effectiveTypes = new HashSet<PsiClass>();
    ContainerUtil.addIfNotNull(effectiveTypes, beanClass);

    for (SpringBeanEffectiveTypeProvider provider : SpringBeanEffectiveTypeProvider.EP_NAME.getExtensionList()) {
      provider.processEffectiveTypes(bean, effectiveTypes);
    }

    return effectiveTypes.toArray(new PsiClass[effectiveTypes.size()]);
  }

  @Nullable
  public static SpringBean getSpringBeanForCurrentCaretPosition(final Editor editor, final PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    PsiElement element = file.findElementAt(offset);
    return findBeanFromPsiElement(element);
  }

  @Nullable
  public static SpringBean findBeanFromPsiElement(final PsiElement element) {
    return DomUtil.findDomElement(element, SpringBean.class);
  }

  @Nonnull
  public static String getPresentationBeanName(final SpringBeanPointer pointer) {
    String beanName = pointer.getName();

    if (beanName == null) {
      final PsiClass psiClass = pointer.getBeanClass();
      if (psiClass != null) beanName = psiClass.getName();

      final CommonSpringBean springBean = pointer.getSpringBean();
      if (springBean instanceof SpringBean) {
        final String unresolvedClassName = ((SpringBean)springBean).getClazz().getStringValue();
        if (unresolvedClassName != null) beanName = unresolvedClassName;
      }
    }
    return beanName == null ? SpringBundle.message("spring.bean.dependency.graph.node.unknown") : beanName;
  }

  public static List<SpringBeanPointer> getSetterDependencies(final CommonSpringBean springBean) {

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

  public static List<SpringBeanPointer> getConstructorDependencies(final CommonSpringBean springBean) {
    List<SpringBeanPointer> dependencies = new ArrayList<SpringBeanPointer>();
    if (springBean instanceof DomSpringBean) {
      for (ConstructorArg arg : getConstructorArgs((DomSpringBean)springBean)) {
        dependencies.addAll(getSpringValueHolderDependencies(arg));
      }
    }
    return dependencies;
  }

  public static List<SpringBaseBeanPointer> getSpringValueHolderDependencies(final SpringValueHolderDefinition valueHolder) {
    Set<SpringBaseBeanPointer> beans = new LinkedHashSet<SpringBaseBeanPointer>();
    addValueHolder(valueHolder, beans);
    return new ArrayList<SpringBaseBeanPointer>(beans);

  }

  private static void addValueHolder(final SpringValueHolderDefinition definition, final Set<SpringBaseBeanPointer> beans) {
    final GenericDomValue<SpringBeanPointer> element = definition.getRefElement();
    if (element != null) {
      addBasePointer(element, beans);
    }

    if (definition instanceof SpringValueHolder) {
      final SpringValueHolder valueHolder = (SpringValueHolder)definition;
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

  private static void addBasePointer(final GenericValue<SpringBeanPointer> value, final Collection<SpringBaseBeanPointer> beans) {
    ContainerUtil.addIfNotNull(beans, getBasePointer(value.getValue()));
  }

  private static void addMapReferences(final SpringMap map, final Set<SpringBaseBeanPointer> beans) {
    for (SpringEntry entry : map.getEntries()) {
      addValueHolder(entry, beans);
    }
  }

  private static void addIdrefBeans(final Idref idref, final Set<SpringBaseBeanPointer> beans) {
    addBasePointer(idref.getLocal(), beans);
    addBasePointer(idref.getBean(), beans);
  }

  private static void addSpringRefBeans(final SpringRef springRef, final Set<SpringBaseBeanPointer> beans) {
    if (DomUtil.hasXml(springRef)) {
      addBasePointer(springRef.getBean(), beans);
      addBasePointer(springRef.getLocal(), beans);
    }
  }

  private static void addCollectionReferences(final CollectionElements elements, final Set<SpringBaseBeanPointer> beans) {
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
  public static PsiFile getContainingFile(final CommonSpringBean springBean) {
    return springBean.getContainingFile();
  }

  public static boolean isAssignable(final Project project, final PsiClassType type, final String className) {
    final PsiClass psiClass = type.resolve();
    if (psiClass != null) {
      final PsiManager psiManager = PsiManager.getInstance(project);
      final PsiClass required =
        JavaPsiFacade.getInstance(psiManager.getProject()).findClass(className, GlobalSearchScope.allScope(project));
      if (required != null && InheritanceUtil.isInheritorOrSelf(psiClass, required, true)) {
        return true;
      }
    }

    return false;
  }

  @Nullable
  public static SpringBeanPointer getBeanPointer(final SpringModel model, @NonNls @Nonnull String beanName) {
    String beanReferenceName = beanName.startsWith("&") ? beanName.substring(1) : beanName;  // IDEADEV-13855
    return findBean(model, beanReferenceName);
  }

  @Nullable
  public static SpringBeanPointer findBean(final SpringModel model, String beanName) {
    if (StringUtil.isEmptyOrSpaces(beanName)) return null;

    return model.findBean(beanName);
  }

  public static boolean isCompiled(@Nonnull PsiElement element) {
    return element instanceof PsiCompiledElement || element.getOriginalElement() instanceof PsiCompiledElement;
  }

  public static void navigate(@Nullable DomElement domElement) {
    if (domElement == null) return;

    final DomElementsNavigationManager navigationManager = DomElementsNavigationManager.getManager(domElement.getManager().getProject());
    navigationManager.getDomElementsNavigateProvider(DomElementsNavigationManager.DEFAULT_PROVIDER_NAME).navigate(domElement, true);
  }

  public static List<SpringBaseBeanPointer> getSpringBeans(final PsiClass aClass) {
    final List<SpringBaseBeanPointer> beans = new SmartList<SpringBaseBeanPointer>();
    final consulo.module.Module module = ModuleUtilCore.findModuleForPsiElement(aClass);
    if (module != null) {
      final SpringModel springModel = SpringManager.getInstance(aClass.getProject()).getCombinedModel(module);
      if (springModel != null) {
        beans.addAll(springModel.findBeansByPsiClass(aClass));
      }
    }
    return beans;
  }

  public static List<PsiType> resolveGenerics(PsiClassType classType) {
    List<PsiType> generics = new ArrayList<PsiType>();
    final PsiClassType.ClassResolveResult resolveResult = classType.resolveGenerics();
    final PsiClass psiClass = resolveResult.getElement();
    final PsiSubstitutor substitutor = resolveResult.getSubstitutor();
    if (psiClass != null && substitutor != null) {
      for (PsiTypeParameter typeParameter : psiClass.getTypeParameters()) {
        generics.add(substitutor.substitute(typeParameter));

      }
    }
    return generics;
  }

  public static boolean isCollectionType(final PsiType psiType, final Project project) {
    final PsiType collectionType = SpringConverterUtil.findType(Collection.class, project);

    return collectionType != null && collectionType.isAssignableFrom(psiType);
  }

  @Nullable
  public static PsiType getGenericCollectonType(final PsiType psiType) {
    final List<PsiType> psiTypes = resolveGenerics((PsiClassType)psiType);
    return psiTypes.size() == 1 ? psiTypes.get(0) : null;
  }

  @Nullable
  public static SpringBaseBeanPointer getBasePointer(final SpringBeanPointer springBeanPointer) {
    return springBeanPointer == null ? null : springBeanPointer.getBasePointer();
  }

  @Nonnull
  public static SpringBean getTopLevelBean(@Nonnull SpringBean bean) {
    final DomElement parent = bean.getParent();
    if (parent instanceof Beans) {
      return bean;
    }
    else {
      assert parent != null;
      final SpringBean parentBean = bean.getParentOfType(SpringBean.class, true);
      return getTopLevelBean(parentBean);
    }
  }

  public static boolean visitParents(final SpringBean springBean, final boolean strict, Processor<SpringBean> processor) {
    SpringBeanPointer parent = springBean.getParentBean().getValue();
    if (parent == null) {
      return true;
    }
    final HashSet<CommonSpringBean> visited = new HashSet<CommonSpringBean>();
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
      public boolean process(final SpringBean springBean) {
        final List<? extends DomElement> list = description.getValues(springBean);
        if (list.size() == 1) {
          final T t = (T)list.get(0);
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

  public static <T extends DomElement> Set<T> getMergedSet(final SpringBean springBean, final Function<SpringBean, Collection<T>> getter) {
    final Set<T> set = new HashSet<T>(getter.apply(springBean));
    visitParents(springBean, false, springBean1 -> {
      set.addAll(getter.apply(springBean1));
      return true;
    });
    return set;
  }

  public static boolean compareQualifiers(SpringQualifier one, final SpringQualifier two) {
    if (!Comparing.equal(one.getQualifierType(), two.getQualifierType())) return false;
    if (!Comparing.equal(one.getQualifierValue(), two.getQualifierValue())) return false;
    final List<? extends QualifierAttribute> list1 = one.getQualifierAttributes();
    final int size1 = list1.size();
    final List<? extends QualifierAttribute> list2 = two.getQualifierAttributes();
    final int size2 = list2.size();
    if (size1 != size2) return false;
    if (size1 == 0) return true;
    final Set<QualifierAttribute> set = Sets.newHashSet(QualifierAttribute.HASHING_STRATEGY);
    set.addAll(list1);
    return set.containsAll(list2);
  }

  @Nonnull
  public static List<SpringBaseBeanPointer> getBeansByType(final PsiType type, final SpringModel model) {
    if (type instanceof PsiClassType) {
      final PsiClass psiClass = ((PsiClassType)type).resolve();
      if (psiClass != null) {
        return model.findBeansByEffectivePsiClassWithInheritance(psiClass);
      }
    }
    return Collections.emptyList();
  }

  public static boolean isSpring25(@Nonnull final Module module) {
    final String s = JarVersionDetectionUtil.detectJarVersion(SpringConstants.SPRING_VERSION_CLASS, module);
    final boolean b = s != null && !s.startsWith("1.") && !s.startsWith("2.0") && !s.startsWith("2.1");
    return b;
  }

  @Nonnull
  public static Set<String> getListOrSetValues(@Nonnull final SpringPropertyDefinition property) {
    if (property instanceof SpringProperty) {
      final SpringProperty springProperty = (SpringProperty)property;
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
  public static Set<String> getValues(@Nonnull final ListOrSet listOrSet) {
    final Set<String> values = new HashSet<String>();
    for (SpringValue value : listOrSet.getValues()) {
      if (value.getValue() != null) {
        values.add(value.getStringValue());
      }
    }
    return values;
  }

  public static Set<String> getAllBeanNames(final SpringBean bean) {
    final String beanName = bean.getBeanName();
    return beanName == null ? Collections.<String>emptySet() : getSpringModel(bean).getAllBeanNames(beanName);
  }

  public static Set<String> getAllBeanNames(final CommonSpringBean bean) {
    final String beanName = bean.getBeanName();
    if (beanName == null) {
      return Collections.emptySet();
    }
    final SpringModel model = getSpringModelForBean(bean);
    return model == null ? Collections.<String>emptySet() : model.getAllBeanNames(beanName);
  }

  @Nullable
  public static SpringModel getModelByPsiElement(final PsiElement element) {
    if (element == null) {
      return null;
    }
    final Module module = ModuleUtilCore.findModuleForPsiElement(element);
    return module == null ? null : SpringManager.getInstance(element.getProject()).getCombinedModel(module);
  }
}
