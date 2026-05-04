package com.intellij.spring.webflow.model.xml;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * User: plt
 */
public abstract class WebflowDomModelManager {
  public static WebflowDomModelManager getInstance(Project project) {
    return ServiceManager.getService(project, WebflowDomModelManager.class);
  }

  public abstract boolean isWebflow(@NotNull final XmlFile file) ;

  @Nullable
  public abstract WebflowModel getWebflowModel(@NotNull final XmlFile file);

   public abstract List<WebflowModel> getAllModels(@NotNull Module module);
}