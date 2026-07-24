package com.intellij.spring.impl.ide.model.jam;

import com.intellij.jam.JamService;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.constants.SpringAnnotationsConstants;
import com.intellij.spring.impl.ide.model.jam.javaConfig.JavaSpringConfigurationElement;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJamElement;
import com.intellij.spring.impl.ide.model.jam.stereotype.*;
import com.intellij.spring.impl.ide.model.jam.utils.JamAnnotationTypeUtil;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.spring.impl.boot.jam.SpringBootConfigurationElement;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@ServiceAPI(ComponentScope.MODULE)
@ServiceImpl
@Singleton
public class SpringJamModel {
  private final Module myModule;


  public static SpringJamModel getModel(@Nonnull Module module) {
    return module.getInstance(SpringJamModel.class);
  }

  @Inject
  public SpringJamModel(@Nonnull Module module) {
    myModule = module;
  }

  public List<SpringJamElement> getConfigurations() {
    JamService service = JamService.getJamService(myModule.getProject());
    GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);
    List<SpringJamElement> configurations = new ArrayList<>();

    configurations.addAll(service.getJamClassElements(JavaSpringConfigurationElement.META,
                                                      SpringAnnotationsConstants.SPRING_CONFIGURATION_ANNOTATION,
                                                      scope));

    configurations.addAll(service.getJamClassElements(SpringBootConfigurationElement.META,
                                                      SpringAnnotationsConstants.SPRING_BOOT_CONFIGURATION_ANNOTATION,
                                                      scope));
    return configurations;
  }

  @Nonnull
  public List<? extends SpringStereotypeElement> getAllStereotypeComponents(@Nonnull PsiClass psiMember) {
    JamService service = JamService.getJamService(myModule.getProject());

    return service
      .getAnnotatedMembersList(psiMember, true, true, false, true, SpringComponent.META, SpringController.META, SpringService.META,
                               SpringRepository.META);
  }

  @Nonnull
  public List<? extends SpringStereotypeElement> getAllStereotypeComponents() {
    List<SpringStereotypeElement> stereotypeElements = new ArrayList<>();

    stereotypeElements.addAll(getComponents());
    stereotypeElements.addAll(getControllers());
    stereotypeElements.addAll(getRepositories());
    stereotypeElements.addAll(getServices());
    stereotypeElements.addAll(getCustomStereotypeComponents());

    return stereotypeElements;
  }

  @Nonnull
  public List<? extends CustomSpringComponent> getCustomStereotypeComponents() {
    List<CustomSpringComponent> customSpringComponents = new ArrayList<>();
    JamService service = JamService.getJamService(myModule.getProject());
    GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    for (String anno : JamAnnotationTypeUtil.getUserDefinedCustomComponentAnnotations(myModule)) {
      customSpringComponents.addAll(service.getJamClassElements(SpringSemContributor.CUSTOM_COMPONENT_JAM_KEY, anno, scope));
    }

    return customSpringComponents;
  }

  @Nonnull
  public List<? extends SpringComponent> getComponents() {
    JamService service = JamService.getJamService(myModule.getProject());
    GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    return service.getJamClassElements(SpringComponent.META, SpringAnnotationsConstants.COMPONENT_ANNOTATION, scope);
  }

  @Nonnull
  public List<? extends SpringController> getControllers() {
    JamService service = JamService.getJamService(myModule.getProject());
    GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    return service.getJamClassElements(SpringController.META, SpringAnnotationsConstants.CONTROLLER_ANNOTATION, scope);
  }


  @Nonnull
  public List<? extends SpringRepository> getRepositories() {
    JamService service = JamService.getJamService(myModule.getProject());
    GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    return service.getJamClassElements(SpringRepository.META, SpringAnnotationsConstants.REPOSITORY_ANNOTATION, scope);
  }


  @Nonnull
  public List<? extends SpringService> getServices() {
    JamService service = JamService.getJamService(myModule.getProject());
    GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    return service.getJamClassElements(SpringService.META, SpringAnnotationsConstants.SERVICE_ANNOTATION, scope);
  }

  @Nonnull
  public List<? extends SpringComponentScan> getComponentScans() {
    JamService service = JamService.getJamService(myModule.getProject());
    GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    return service.getJamClassElements(SpringComponentScan.META, SpringAnnotationsConstants.COMPONENT_SCAN_ANNOTATION, scope);
  }
}