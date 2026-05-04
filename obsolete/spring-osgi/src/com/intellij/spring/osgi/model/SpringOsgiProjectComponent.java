package com.intellij.spring.osgi.model;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SpringOsgiProjectComponent implements ProjectComponent {

  public static SpringOsgiProjectComponent getInstance(@NotNull Project project) {
    return project.getComponent(SpringOsgiProjectComponent.class);
  }

  public SpringOsgiProjectComponent(final Project project) {
     
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return SpringOsgiProjectComponent.class.getName();
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }
}