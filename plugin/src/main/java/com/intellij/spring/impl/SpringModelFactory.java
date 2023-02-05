/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringImport;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.project.Project;
import consulo.spring.impl.DomSpringModel;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.pointer.VirtualFilePointer;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.util.xml.DomFileElement;
import consulo.xml.util.xml.model.impl.DomModelFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Dmitry Avdeev
*/
@Deprecated
public class SpringModelFactory extends DomModelFactory<Beans, DomSpringModel, PsiElement> {

  protected SpringModelFactory(Project project) {
    super(Beans.class, project, "spring");
  }

  /*
  We don't need this because every change of file sets will change project root modificator - and drop cache
  @NotNull
  public Object[] computeDependencies(@Nullable SpringModel model, @Nullable Module module) {
    final ArrayList<Object> dependencies = new ArrayList<Object>(5);
    dependencies.add(PsiModificationTracker.MODIFICATION_COUNT);
    if (module != null) {
      final Project project = module.getProject();
      final FacetFinderImpl finder = (FacetFinderImpl)FacetFinder.getInstance(project);
      if (finder != null) {
        dependencies.add(finder.getAllFacetsOfTypeModificationTracker(SpringFacet.FACET_TYPE_ID));
      }
      dependencies.add(ProjectRootManager.getInstance(project));
      final SpringModuleExtension facet = SpringModuleExtension.getInstance(module);
      if (facet != null){
        dependencies.add(facet.getConfiguration());
      }
    }
    return dependencies.toArray(new Object[dependencies.size()]);
  }*/

  protected List<DomSpringModel> computeAllModels(@Nonnull final Module module) {

    final SpringModuleExtension facet = SpringModuleExtension.getInstance(module);
    if (facet == null) {
      return Collections.emptyList();
    }
    final SpringManager springManager = SpringManager.getInstance(module.getProject());
    final Set<SpringFileSet> fileSets = springManager.getAllSets(facet);
    final ArrayList<DomSpringModel> models = new ArrayList<DomSpringModel>(fileSets.size());
    for (SpringFileSet set: fileSets) {
      if (set.isRemoved()) {
        continue;
      }
      final DomSpringModelImpl model = createModel(set, module);
      if (model != null) {
        models.add(model);
      }
    }
    setDependencies(models);
    return models;
  }

  @Nullable
  public DomSpringModelImpl createModel(final SpringFileSet set, final Module module) {
    final PsiManager psiManager = PsiManager.getInstance(module.getProject());
    Set<XmlFile> files = new LinkedHashSet<XmlFile>(set.getFiles().size());
    for (VirtualFilePointer filePointer: set.getFiles()) {
      final VirtualFile file = filePointer.getFile();
      if (file == null) {
        continue;
      }
      final PsiFile psiFile = psiManager.findFile(file);
      if (psiFile instanceof XmlFile) {
        final Beans dom = getDom((XmlFile)psiFile);
        if (dom != null) {
          files.add((XmlFile)psiFile);
          addIncludes(files, dom);
        }
      }
    }
    if (files.size() > 0) {
      final DomFileElement<Beans> element = createMergedModelRoot(files);
      if (element != null) {
        return new DomSpringModelImpl(element, files, module, set);
      }
    }
    return null;
  }

  private static void setDependencies(final List<DomSpringModel> models) {
    for (SpringModel model : models) {
      final List<String> dependencies = ((DomSpringModelImpl)model).getFileSet().getDependencies();
      if (dependencies.size() > 0) {
        final ArrayList<SpringModel> list = new ArrayList<SpringModel>(dependencies.size());
        for (Iterator<String> i = dependencies.iterator(); i.hasNext();) {
          String dependency = i.next();
          boolean valid = false;
          for (SpringModel depModel : models) {
            if (depModel != model && depModel.getId().equals(dependency)) {
              list.add(depModel);
              valid = true;
              break;
            }
          }
          if (!valid) {
            i.remove();
          }
        }
        ((DomSpringModelImpl)model).setDependencies(list.toArray(new SpringModel[list.size()]));
      }
    }
  }

  protected DomSpringModel computeModel(@Nonnull XmlFile psiFile, @Nullable Module module) {
    // trying to compute model for given module...
    DomSpringModel model = super.computeModel(psiFile, module);
    if (model != null) {
      return model;
    }
    final ModuleManager moduleManager = ModuleManager.getInstance(psiFile.getProject());
    if (module != null) {
      final List<Module> dependentModules = moduleManager.getModuleDependentModules(module);
      for (Module dependentModule: dependentModules) {
        model = super.computeModel(psiFile, dependentModule);
        if (model != null) {
          return model;
        }
      }
    }
    final Module[] allModules = moduleManager.getModules();
    for (Module aModule: allModules) {
      model = super.computeModel(psiFile, aModule);
      if (model != null) {
        return model;
      }
    }

    // no configuration found; compute model for single file...
    final DomFileElement<Beans> beans = getDomRoot(psiFile);
    if (beans != null) {
      final HashSet<XmlFile> files = new HashSet<XmlFile>();
      files.add(psiFile);
      addIncludes(files, beans.getRootElement());
      return new DomSpringModelImpl(files.size() > 1 ? createMergedModelRoot(files) : beans, files, module, null);
    }
    return null;
  }

  protected DomSpringModel createCombinedModel(@Nonnull final Set<XmlFile> configFiles,
                                            @Nonnull final DomFileElement<Beans> mergedModel,
                                            final DomSpringModel firstModel,
                                            final Module module) {
    return new DomSpringModelImpl(mergedModel, configFiles, module, null);
  }

  private void addIncludes(Set<XmlFile> files, final Beans dom) {
    for (SpringImport imp: dom.getImports()) {
      final PsiFile psiFile = imp.getResource().getValue();
      if (psiFile instanceof XmlFile && !files.contains(psiFile)) {
        final Beans child = getDom((XmlFile)psiFile);
        if (child != null) {
          files.add((XmlFile)psiFile);
          addIncludes(files, child);
        }
      }
    }
  }
}
