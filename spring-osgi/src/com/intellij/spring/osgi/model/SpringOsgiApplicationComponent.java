package com.intellij.spring.osgi.model;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ApplicationComponent;
import consulo.disposer.Disposer;
import com.intellij.spring.SpringDomFileDescription;
import com.intellij.spring.factories.SpringFactoryBeansManager;
import com.intellij.spring.factories.resolvers.SingleObjectTypeResolver;
import com.intellij.spring.osgi.constants.SpringOsgiConstants;
import com.intellij.spring.osgi.inspections.SpringOsgiElementsInconsistencyInspection;
import com.intellij.spring.osgi.inspections.SpringOsgiServiceCommonInspection;
import com.intellij.spring.osgi.inspections.SpringOsgiListenerInspection;
import com.intellij.spring.osgi.model.xml.*;
import com.intellij.spring.osgi.model.xml.impl.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SpringOsgiApplicationComponent implements ApplicationComponent, InspectionToolProvider, Disposable {

  @NonNls
  @NotNull
  public String getComponentName() {
    return getClass().getName();
  }

  public void initComponent() {
    registerDomFileDescriptionComponents();

    registerMetaData();
    registerPresentations();

    registerRenameValidators();

    SpringFactoryBeansManager.getInstance().registerFactory(SpringOsgiConstants.OSGI_SERVICE_FACTORY_BEAN_CLASSNAME, new SingleObjectTypeResolver(SpringOsgiConstants.OSGI_SERVICE_REGISTRATION_CLASSNAME));
  }

  private static void registerDomFileDescriptionComponents() {
    final SpringDomFileDescription springDomFileDescription = SpringDomFileDescription.getInstance();

    springDomFileDescription.registerNamespacePolicy(SpringOsgiConstants.OSGI_NAMESPACE_KEY, SpringOsgiConstants.OSGI_NAMESPACE);
    springDomFileDescription.registerNamespacePolicy(SpringOsgiConstants.OSGI_COMPENDIUM_NAMESPACE_KEY, SpringOsgiConstants.OSGI_COMPENDIUM_NAMESPACE);

    springDomFileDescription.registerImplementation(Interfaces.class,  InterfacesImpl.class);
    springDomFileDescription.registerImplementation(Service.class,  ServiceImpl.class);
    springDomFileDescription.registerImplementation(Reference.class,  ReferenceImpl.class);
    springDomFileDescription.registerImplementation(List.class,  ListImpl.class);
    springDomFileDescription.registerImplementation(Set.class,  SetImpl.class);
    springDomFileDescription.registerImplementation(Bundle.class,  BundleImpl.class);
  }

  private static void registerMetaData() {
  }

  private static void registerPresentations() {
  }

  private static void registerRenameValidators() {
  }

  public void dispose() {
  }

  public void disposeComponent() {
    Disposer.dispose(this);
  }

  public Class[] getInspectionClasses() {
    return new Class[]{SpringOsgiElementsInconsistencyInspection.class, SpringOsgiServiceCommonInspection.class,
      SpringOsgiListenerInspection.class};
  }

}