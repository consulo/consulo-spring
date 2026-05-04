package com.intellij.spring.webflow.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.webflow.model.xml.Flow;
import com.intellij.spring.webflow.model.xml.WebflowDomModelManager;
import com.intellij.spring.webflow.model.xml.WebflowModel;
import com.intellij.spring.webflow.model.xml.WebflowModelFactory;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * User: plt
 */
public class WebflowDomModelManagerImpl extends WebflowDomModelManager {
 private final WebflowModelFactory myModelFactory;
  private final DomManager myDomManager;

  public WebflowDomModelManagerImpl(final Project project, DomManager domManager) {
    myDomManager = domManager;
    myModelFactory = new WebflowModelFactory(project);
  }

  public boolean isWebflow(@NotNull final XmlFile file) {
    return myDomManager.getFileElement(file, Flow.class) != null;
  }

  @Nullable
  public WebflowModel getWebflowModel(@NotNull final XmlFile file) {
    return myModelFactory.getModelByConfigFile(file);
  }

  public List<WebflowModel> getAllModels(@NotNull final Module module) {
    return myModelFactory.getAllModels(module);
  }
}
