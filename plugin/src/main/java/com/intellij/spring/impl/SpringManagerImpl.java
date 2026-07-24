/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl;

import com.intellij.java.impl.util.xml.converters.values.GenericDomValueConvertersRegistry;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.SpringModelProvider;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.model.converters.CustomConverterRegistry;
import com.intellij.spring.impl.ide.model.values.converters.*;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ServiceImpl;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.spring.impl.boot.AnnotationSpringModel;
import consulo.spring.impl.boot.SpringBootFileSet;
import consulo.spring.impl.context.SpringContextSetting;
import consulo.spring.impl.dom.SpringDomUtil;
import consulo.spring.impl.model.CompositeSpringModel;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.dom.DomFileElement;
import consulo.xml.dom.DomManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;

/**
 * @author Dmitry Avdeev
 */
@Singleton
@ServiceImpl
public class SpringManagerImpl extends SpringManager {

  @Deprecated
  private final SpringModelFactory myModelFactory;
  private final GenericDomValueConvertersRegistry myValueProvidersRegistry;
  private final CustomConverterRegistry myCustomConvertersRegistry;
  private final Project myProject;
  private final CachedValuesManager myCachedValuesManager;

  @Inject
  public SpringManagerImpl(Project project, CachedValuesManager cachedValuesManager) {
    myProject = project;
    myCachedValuesManager = cachedValuesManager;
    myModelFactory = new SpringModelFactory(project);
    myValueProvidersRegistry = new GenericDomValueConvertersRegistry();
    myCustomConvertersRegistry = new CustomConverterRegistry();

    registerValueConverters();
  }

  private void registerValueConverters() {
    myValueProvidersRegistry
      .registerConverter(new PlaceholderPropertiesConverter(), new PlaceholderPropertiesConverter.PlaceholderPropertiesCondition());

    myValueProvidersRegistry.registerConverter(new SpringBooleanValueConverter(false), PsiType.BOOLEAN);
    myValueProvidersRegistry.registerConverter(new SpringBooleanValueConverter(true), Boolean.class);

    myValueProvidersRegistry.registerNumberValueConverters();
    myValueProvidersRegistry.registerCharacterConverter();

    myValueProvidersRegistry.registerClassValueConverters();

    myValueProvidersRegistry.registerConverter(new ResourceValueConverter(), new ResourceValueConverter.ResourceValueConverterCondition());
    myValueProvidersRegistry.registerConverter(new FieldRetrievingFactoryBeanConverter(),
                                               new FieldRetrievingFactoryBeanConverter.FactoryClassAndPropertyCondition());
    myValueProvidersRegistry.registerConverter(new EnumValueConverter(), new EnumValueConverter.TypeCondition());
  }

  @RequiredReadAction
  @Override
  public SpringModel getModel(@Nonnull Module module) {
    return myCachedValuesManager.getCachedValue(module, () -> {
      return CachedValueProvider.Result.create(getModelImpl(module), PsiModificationTracker.MODIFICATION_COUNT);
    });
  }

  @RequiredReadAction
  private SpringModel getModelImpl(@Nonnull Module module) {
    SpringContextSetting setting = myProject.getInstance(SpringContextSetting.class);

    LinkedHashMap<SpringFileSet, SpringModelProvider> providerMap = new LinkedHashMap<>();
    for (SpringModelProvider provider : DumbService.getDumbAwareExtensions(myProject, SpringModelProvider.EP_NAME)) {
      SpringModuleExtension extension = module.getExtension(SpringModuleExtension.class);
      if (extension == null) {
        continue;
      }

      provider.collectFilesets(extension, fileSet -> providerMap.put(fileSet, provider));
    }

    if (providerMap.isEmpty()) {
      return null;
    }

    Map.Entry<SpringFileSet, SpringModelProvider> entry = ContainerUtil.getFirstItem(providerMap.entrySet());

    SpringFileSet key = entry.getKey();
    SpringModelProvider value = entry.getValue();

    // TODO settings
    return value.createModel(module, key);
  }

  @Override
  @Nonnull
  @RequiredReadAction
  public List<SpringModel> getAllModels(@Nonnull consulo.module.Module module) {
    SpringModuleExtension extension = ModuleUtilCore.getExtension(module, SpringModuleExtension.class);
    if (extension == null) {
      return Collections.emptyList();
    }
    Set<SpringFileSet> allSets = getAllSets(extension);
    if (allSets.isEmpty()) {
      return Collections.emptyList();
    }

    List<SpringModel> list = new SmartList<>();
    for (SpringFileSet set : allSets) {
      if (set instanceof SpringBootFileSet) {
        list.add(new AnnotationSpringModel(module, set));
      }
      else {
        list.addAll(SpringDomUtil.createModels(set, module));
      }
    }
    return list;
  }


  @RequiredReadAction
  private SpringModel getCombinedModelImpl(Module module) {
    List<SpringModel> allModels = getAllModels(module);
    if (allModels.isEmpty()) {
      return null;
    }
    return new CompositeSpringModel(module, allModels);
  }

  @Override
  public GenericDomValueConvertersRegistry getValueProvidersRegistry() {
    return myValueProvidersRegistry;
  }

  @Override
  public CustomConverterRegistry getCustomConverterRegistry() {
    return myCustomConvertersRegistry;
  }

  @Override
  public boolean isSpringBeans(@Nonnull XmlFile file) {
    return DomManager.getDomManager(file.getProject()).getFileElement(file, Beans.class) != null;
  }

  @Override
  @Nullable
  public SpringModel getSpringModelByFile(@Nonnull XmlFile file) {
    DomSpringModelImpl2.MyDomModelImpl domModel = (DomSpringModelImpl2.MyDomModelImpl)myModelFactory.getModelByConfigFile(file);
    if (domModel == null) {
      return null;
    }

    return domModel.getSpringModel();
  }

  @Override
  @Nullable
  public SpringModel getLocalSpringModel(@Nonnull XmlFile file) {
    DomFileElement<Beans> beans = myModelFactory.getDomRoot(file);
    return beans == null ? null : new DomSpringModelImpl2(beans,
                                                          Collections.singleton(file),
                                                          ModuleUtilCore.findModuleForPsiElement(file),
                                                          null);
  }

  @Override
  @Nonnull
  @RequiredReadAction
  public List<SpringFileSet> getProvidedModels(@Nonnull SpringModuleExtension extension) {
    List<SpringFileSet> results = new ArrayList<>();

    for (SpringModelProvider modelProvider : SpringModelProvider.EP_NAME.getExtensionList()) {
      modelProvider.collectFilesets(extension, results::add);
    }
    return results;
  }

  @Override
  @Nonnull
  public Set<SpringFileSet> getAllSets(@Nonnull SpringModuleExtension extension) {
    Set<SpringFileSet> fileSets = new HashSet<SpringFileSet>(extension.getFileSets());
    List<SpringFileSet> providedModels = getProvidedModels(extension);
    fileSets.addAll(providedModels);
    return fileSets;
  }
}
