package com.intellij.spring.security.model;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.spring.SpringManager;
import com.intellij.spring.security.model.xml.converters.SecurityRoleConstructorArgConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SpringSecurityProjectComponent implements ProjectComponent {
  private final Project myProject;

  public static SpringSecurityProjectComponent getInstance(@NotNull Project project) {
    return project.getComponent(SpringSecurityProjectComponent.class);
  }

  public SpringSecurityProjectComponent(final Project project) {

    myProject = project;
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return SpringSecurityProjectComponent.class.getName();
  }

  public void initComponent() {
    SpringManager.getInstance(myProject).getValueProvidersRegistry().registerConverter(new SecurityRoleConstructorArgConverter(), new SecurityRoleConstructorArgConverter.SecurityRoleConstructorArgCondition());
  }

  public void disposeComponent() {
  }
}