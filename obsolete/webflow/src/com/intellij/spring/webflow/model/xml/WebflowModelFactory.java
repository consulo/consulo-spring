package com.intellij.spring.webflow.model.xml;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.webflow.impl.WebflowModelImpl;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomService;
import com.intellij.util.xml.ModuleContentRootSearchScope;
import com.intellij.util.xml.model.impl.DomModelFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Set;

/**
 * User: plt
 */
public class WebflowModelFactory extends DomModelFactory<Flow, WebflowModel, PsiElement> {

  public WebflowModelFactory(Project project) {
    super(Flow.class, project, "webflow");
  }

  public WebflowModel getModel(@NotNull PsiElement context) {
    PsiFile psiFile = context.getContainingFile();
    if (psiFile instanceof XmlFile) {
      return getModelByConfigFile((XmlFile)psiFile);
    }
    return null;
  }

  protected List<WebflowModel> computeAllModels(@NotNull Module module) {
    List<WebflowModel> models = new ArrayList<WebflowModel>();

    Collection<VirtualFile> webflowlFiles = new HashSet<VirtualFile>();

    webflowlFiles.addAll(DomService.getInstance().getDomFileCandidates(Flow.class, module.getProject(), new ModuleContentRootSearchScope(module)));
    webflowlFiles.addAll(DomService.getInstance().getDomFileCandidates(Flow.class, module.getProject(), GlobalSearchScope.moduleWithLibrariesScope(module)));

    for (VirtualFile webflowlFile : webflowlFiles) {
      PsiFile file = PsiManager.getInstance(module.getProject()).findFile(webflowlFile);
      if (file instanceof XmlFile) {
        WebflowModel webflowModel = computeModel((XmlFile)file, module);
        if (webflowModel != null) {
           models.add(webflowModel);
        }
      }
    }

    return models;
  }

  @Nullable
  public WebflowModel getModelByConfigFile(@Nullable XmlFile psiFile) {
    if (psiFile == null) {
      return null;
    }
    return computeModel(psiFile, psiFile.getModule());
  }

  protected WebflowModel computeModel(@NotNull XmlFile psiFile, @Nullable Module module) {
    return createSingleModel(psiFile);
  }

  @Nullable
  private WebflowModel createSingleModel(XmlFile psiFile) {
    DomFileElement<Flow> componentsDomFileElement = getDomRoot(psiFile);
    if (componentsDomFileElement != null) {
      HashSet<XmlFile> files = new HashSet<XmlFile>();
      files.add(psiFile);

      DomFileElement<Flow> fileElement = files.size() > 1 ? createMergedModelRoot(files) : componentsDomFileElement;

      if (fileElement != null) {
        return new WebflowModelImpl(fileElement, files);
      }
    }
    return null;
  }

  protected WebflowModel createCombinedModel(@NotNull Set<XmlFile> configFiles,
                                              @NotNull DomFileElement<Flow> mergedModel,
                                              WebflowModel firstModel,
                                              Module module) {
    throw new UnsupportedOperationException();
  }
}