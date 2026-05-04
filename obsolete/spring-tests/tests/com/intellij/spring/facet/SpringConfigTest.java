/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.impl.ui.FacetErrorPanel;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.WizardContext;
import consulo.ide.impl.idea.openapi.application.PathManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.FacetsProvider;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import consulo.util.dataholder.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.spring.model.xml.HeavySpringTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import consulo.util.collection.MultiMap;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;

/**
 * @author Dmitry Avdeev
 */
public class SpringConfigTest extends HeavySpringTestCase {

  private FacetEditorContext createContext(final Facet facet) {
    return new FacetEditorContext() {
      @NotNull
      public Project getProject() {
        return myFixture.getProject();
      }

      @Nullable
      public Library findLibrary(@NotNull final String name) {
        return null;
      }

      @Nullable
      public ModuleBuilder getModuleBuilder() {
        return null;
      }

      @NotNull
      public Facet getFacet() {
        return facet;
      }

      @Nullable
      public Facet getParentFacet() {
        return null;
      }

      @NotNull
      public FacetsProvider getFacetsProvider() {
        throw new UnsupportedOperationException("'getFacetsProvider' not implemented in " + getClass().getName());
      }

      @NotNull
      public ModulesProvider getModulesProvider() {
        throw new UnsupportedOperationException("'getModulesProvider' not implemented in " + getClass().getName());
      }

      @NotNull
      public ModifiableRootModel getModifiableRootModel() {
        throw new UnsupportedOperationException();
      }

      @NotNull
      public ModuleRootModel getRootModel() {
        throw new UnsupportedOperationException();
      }

      public boolean isNewFacet() {
        return false;
      }

      @NotNull
      public Module getModule() {
        return myFixture.getModule();
      }

      public Library[] getLibraries() {
        return new Library[0];
      }

      @Nullable
      public WizardContext getWizardContext() {
        return null;
      }

      public Library createProjectLibrary(final String name, final VirtualFile[] roots, final VirtualFile[] sources) {
        throw new UnsupportedOperationException("'createProjectLibrary' not implemented in " + getClass().getName());
      }

      public VirtualFile[] getLibraryFiles(final Library library, final OrderRootType rootType) {
        return VirtualFile.EMPTY_ARRAY;
      }

      @NotNull
      public String getFacetName() {
        return "";
      }

      public <T> T getUserData(final Key<T> key) {
        return null;
      }

      public <T> void putUserData(final Key<T> key, final T value) {
      }
    };
  }

  public SpringConfigTest() {
    super(false);
  }

  public void testConfigSearcher() {
    final SpringConfigsSearcher configsSearcher = new SpringConfigsSearcher(createContext(null));
    configsSearcher.search();
    final MultiMap<Module,PsiFile> map = configsSearcher.getFilesByModules();
    assertEquals(1, map.size());
    final Module module = myFixture.getModule();
    final Collection<PsiFile> psiFiles = map.get(module);
    assertEquals(2, psiFiles.size());
    final MultiMap<VirtualFile, PsiFile> jars = configsSearcher.getJars();
    assertEquals(1, jars.size());
    final Collection<? extends PsiFile> jarFiles = jars.values();
    assertEquals(1, jarFiles.size());
  }

  public void testSpringConfiguration() {
    final SpringFacet facet = new WriteAction<SpringFacet>() {
      protected void run(final Result<SpringFacet> result) {
        result.setResult(
            FacetManager.getInstance(myModule).addFacet(SpringFacetType.INSTANCE, SpringFacetType.INSTANCE.getDefaultFacetName(), null));
      }
    }.execute().getResultObject();
    final SpringFacetConfiguration configuration = facet.getConfiguration();
    final FacetErrorPanel errorPanel = new FacetErrorPanel();
    final FacetEditorTab[] tabs = configuration.createEditorTabs(createContext(facet), errorPanel.getValidatorsManager());
    assertEquals(2, tabs.length);
    SpringConfigurationTab configurationTab = (SpringConfigurationTab)tabs[0];
    UIUtil.pump();
    configurationTab.reset();
    UIUtil.pump();
    configurationTab.disposeUIResources();
    UIUtil.pump();
  }

  @NonNls
  public String getBasePath() {
    return "/svnPlugins/spring/spring-tests/testData/config/";
  }

  protected void configureModule(JavaModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.addSourceRoot(getTestDataPath());
    moduleBuilder.addLibraryJars("spring", PathManager.getHomePath().replace(File.separatorChar, '/') + super.getBasePath(), "spring2.jar");
  }
}
