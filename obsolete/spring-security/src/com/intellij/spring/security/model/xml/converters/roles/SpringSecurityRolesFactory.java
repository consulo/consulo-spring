package com.intellij.spring.security.model.xml.converters.roles;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import consulo.ide.impl.idea.openapi.module.ModuleServiceManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpringSecurityRolesFactory implements Disposable {

  public static SpringSecurityRolesFactory getInstance(final Module module) {
      return ModuleServiceManager.getService(module, SpringSecurityRolesFactory.class);
  }

  private final Set<SpringSecurityRole> myRoles = new HashSet<SpringSecurityRole>();
  private final Map<PsiFile, Long> myContributers = new HashMap<PsiFile, Long>();

  private final PsiFile myDummyFile;

  public SpringSecurityRolesFactory(final Module module) {
    myDummyFile = PsiFileFactory.getInstance(module.getProject()).createFileFromText("dummy_security_roles.java", "");
  }

  @NotNull
  public SpringSecurityRole getOrCreateRole(final String roleName, @NotNull PsiFile containingFile) {
    if (myContributers.containsKey(containingFile) && myContributers.get(containingFile) != containingFile.getModificationStamp()) {
      myRoles.clear();
      myContributers.clear();
    }

    for (SpringSecurityRole securityRole : myRoles) {
       if (roleName.equals(securityRole.getName())) return securityRole;
    }
    SpringSecurityRole role = new SpringSecurityRole(roleName, myDummyFile);
    myRoles.add(role);
    myContributers.put(containingFile, containingFile.getModificationStamp());

    return role;
  }

  @NotNull
  public Set<SpringSecurityRole> getRoles() {
    return myRoles;
  }

  public void dispose() {
      myRoles.clear();
  }
}