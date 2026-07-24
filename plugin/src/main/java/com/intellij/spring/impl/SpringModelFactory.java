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
import consulo.spring.impl.model.BaseSpringModel;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import consulo.util.collection.ContainerUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.pointer.VirtualFilePointer;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.dom.DomFileElement;
import consulo.xml.util.xml.model.impl.DomModelFactory;
import consulo.xml.util.xml.model.impl.DomModelImpl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;

/**
 * @author Dmitry Avdeev
*/
@Deprecated
public class SpringModelFactory extends DomModelFactory<Beans, DomModelImpl<Beans>, PsiElement> {

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

  @Override
  protected List<DomModelImpl<Beans>> computeAllModels(@Nonnull Module module) {

    SpringModuleExtension facet = SpringModuleExtension.getInstance(module);
    if (facet == null) {
      return Collections.emptyList();
    }
    SpringManager springManager = SpringManager.getInstance(module.getProject());
    Set<SpringFileSet> fileSets = springManager.getAllSets(facet);
    ArrayList<DomSpringModel> models = new ArrayList<>(fileSets.size());
    for (SpringFileSet set: fileSets) {
      if (set.isRemoved()) {
        continue;
      }
      DomSpringModelImpl2 model = createModel(set, module);
      if (model != null) {
        models.add(model);
      }
    }
    setDependencies(models);
    return ContainerUtil.map(models, DomSpringModel::getDomModel);
  }

  @Nullable
  public DomSpringModelImpl2 createModel(SpringFileSet set, Module module) {
    PsiManager psiManager = PsiManager.getInstance(module.getProject());
    Set<XmlFile> files = new LinkedHashSet<>(set.getFiles().size());
    for (VirtualFilePointer filePointer: set.getFiles()) {
      VirtualFile file = filePointer.getFile();
      if (file == null) {
        continue;
      }
      PsiFile psiFile = psiManager.findFile(file);
      if (psiFile instanceof XmlFile) {
        Beans dom = getDom((XmlFile)psiFile);
        if (dom != null) {
          files.add((XmlFile)psiFile);
          addIncludes(files, dom);
        }
      }
    }
    if (files.size() > 0) {
      DomFileElement<Beans> element = createMergedModelRoot(files);
      if (element != null) {
        return new DomSpringModelImpl2(element, files, module, set);
      }
    }
    return null;
  }

  private static void setDependencies(List<DomSpringModel> models) {
    for (SpringModel model : models) {
      List<String> dependencies = model.getFileSet().getDependencies();
      if (dependencies.size() > 0) {
        ArrayList<SpringModel> list = new ArrayList<>(dependencies.size());
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
        ((BaseSpringModel)model).setDependencies(list.toArray(new SpringModel[list.size()]));
      }
    }
  }

  @Override
  protected DomModelImpl<Beans> computeModel(@Nonnull XmlFile psiFile, @Nullable Module module) {
    // trying to compute model for given module...
    DomModelImpl<Beans> model = super.computeModel(psiFile, module);
    if (model != null) {
      return model;
    }
    ModuleManager moduleManager = ModuleManager.getInstance(psiFile.getProject());
    if (module != null) {
      List<Module> dependentModules = moduleManager.getModuleDependentModules(module);
      for (Module dependentModule: dependentModules) {
        model = super.computeModel(psiFile, dependentModule);
        if (model != null) {
          return model;
        }
      }
    }
    Module[] allModules = moduleManager.getModules();
    for (Module aModule: allModules) {
      model = super.computeModel(psiFile, aModule);
      if (model != null) {
        return model;
      }
    }

    // no configuration found; compute model for single file...
    DomFileElement<Beans> beans = getDomRoot(psiFile);
    if (beans != null) {
      HashSet<XmlFile> files = new HashSet<>();
      files.add(psiFile);
      addIncludes(files, beans.getRootElement());
      DomSpringModelImpl2 modelImpl2 =
        new DomSpringModelImpl2(files.size() > 1 ? createMergedModelRoot(files) : beans, files, module, null);
      return modelImpl2.getDomModel();
    }
    return null;
  }

  @Override
  protected DomModelImpl<Beans> createCombinedModel(@Nonnull Set<XmlFile> set,
                                               @Nonnull DomFileElement<Beans> domFileElement,
                                               DomModelImpl<Beans> beansDomModel,
                                               Module module) {
    return new DomSpringModelImpl2(domFileElement, set, module, null).getDomModel();
  }

  private void addIncludes(Set<XmlFile> files, Beans dom) {
    for (SpringImport imp: dom.getImports()) {
      PsiFile psiFile = imp.getResource().getValue();
      if (psiFile instanceof XmlFile && !files.contains(psiFile)) {
        Beans child = getDom((XmlFile)psiFile);
        if (child != null) {
          files.add((XmlFile)psiFile);
          addIncludes(files, child);
        }
      }
    }
  }
}
