/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide;

import com.intellij.java.impl.util.xml.converters.values.GenericDomValueConvertersRegistry;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.model.converters.CustomConverterRegistry;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.ide.ServiceManager;
import consulo.module.Module;
import consulo.project.Project;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import consulo.xml.language.psi.XmlFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Set;

@ServiceAPI(ComponentScope.PROJECT)
public abstract class SpringManager {

  public static SpringManager getInstance(Project project) {
    return ServiceManager.getService(project, SpringManager.class);
  }

  @RequiredReadAction
  @Nullable
  public abstract SpringModel getModel(@Nonnull Module module);

  public abstract boolean isSpringBeans(@Nonnull XmlFile file);

  @Nullable
  public abstract SpringModel getSpringModelByFile(@Nonnull XmlFile file);

  @Nullable
  public abstract SpringModel getLocalSpringModel(@Nonnull XmlFile file);

  /**
   * Returns all models configured in given module.
   *
   * @param module a module.
   * @return list of models; empty list if no models found.
   * @see #getCombinedModel(consulo.module.Module)
   */
  @Nonnull
  @RequiredReadAction
  public abstract List<SpringModel> getAllModels(@Nonnull consulo.module.Module module);

  /**
   * Returns result of merging all models configured in given module.
   *
   * @param module a module.
   * @return null if no models found.
   * @see #getAllModels(consulo.module.Module)
   */
  @Nullable
  @RequiredReadAction
  @Deprecated
  public SpringModel getCombinedModel(@Nullable Module module) {
    if (module== null) {
      return null;
    }
    return getModel(module);
  }

  /**
   * Returns models provided by all {@link SpringModelProvider}s.
   *
   * @param extension
   * @return models provided by {@link SpringModelProvider}.
   * @see SpringModelProvider
   */
  @Nonnull
  @RequiredReadAction
  public abstract List<SpringFileSet> getProvidedModels(@Nonnull SpringModuleExtension extension);

  /**
   * Returns all configured and provided file sets.
   *
   * @param extension
   * @return all working file sets for the module.
   * @see #getProvidedModels(SpringModuleExtension)
   */
  @Nonnull
  public abstract Set<SpringFileSet> getAllSets(@Nonnull SpringModuleExtension extension);

  public abstract GenericDomValueConvertersRegistry getValueProvidersRegistry();

  public abstract CustomConverterRegistry getCustomConverterRegistry();
}