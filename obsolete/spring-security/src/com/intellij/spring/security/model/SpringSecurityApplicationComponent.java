package com.intellij.spring.security.model;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ApplicationComponent;
import consulo.disposer.Disposer;
import com.intellij.spring.SpringDomFileDescription;
import com.intellij.spring.security.SpringSecurityBundle;
import com.intellij.spring.security.constants.SpringSecurityConstants;
import com.intellij.spring.security.inspections.SpringSecurityElementsInconsistencyInspection;
import com.intellij.spring.security.inspections.SpringSecurityFiltersConfiguredInspection;
import com.intellij.spring.security.model.xml.LdapServer;
import com.intellij.spring.security.model.xml.UserService;
import com.intellij.spring.security.model.xml.AuthenticationManager;
import com.intellij.spring.security.model.xml.impl.LdapServerImpl;
import com.intellij.spring.security.model.xml.impl.UserServiceImpl;
import com.intellij.spring.security.model.xml.impl.AuthenticationManagerImpl;
import com.intellij.util.xml.TypeNameManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SpringSecurityApplicationComponent implements ApplicationComponent, InspectionToolProvider, Disposable {

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
  }

  private static void registerDomFileDescriptionComponents() {
    final SpringDomFileDescription springDomFileDescription = SpringDomFileDescription.getInstance();

    springDomFileDescription.registerNamespacePolicy(SpringSecurityConstants.SECURITY_NAMESPACE_KEY, SpringSecurityConstants.SECURITY_NAMESPACE);

    springDomFileDescription.registerImplementation(LdapServer.class, LdapServerImpl.class);
    springDomFileDescription.registerImplementation(UserService.class, UserServiceImpl.class);
    springDomFileDescription.registerImplementation(AuthenticationManager.class, AuthenticationManagerImpl.class);
  }

  private static void registerMetaData() {
  }

  private static void registerPresentations() {
     TypeNameManager.registerTypeName(LdapServer.class, SpringSecurityBundle.message("ldap.server.type"));
     TypeNameManager.registerTypeName(UserService.class, SpringSecurityBundle.message("user.service.type"));
     TypeNameManager.registerTypeName(AuthenticationManager.class, SpringSecurityBundle.message("auth.manager.type"));
  }

  private static void registerRenameValidators() {
  }

  public void dispose() {
  }

  public void disposeComponent() {
    Disposer.dispose(this);
  }

  public Class[] getInspectionClasses() {
    return new Class[]{SpringSecurityElementsInconsistencyInspection.class, SpringSecurityFiltersConfiguredInspection.class};
  }

}