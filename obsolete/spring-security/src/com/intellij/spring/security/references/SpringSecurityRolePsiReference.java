package com.intellij.spring.security.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.TextRange;
import com.intellij.spring.security.model.xml.converters.roles.SpringSecurityRolesFactory;
import com.intellij.spring.security.model.xml.converters.roles.SpringSecurityRole;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
* @author Serega.Vasiliev
*/
public class SpringSecurityRolePsiReference extends PsiReferenceBase<PsiElement> {
  private final String myRoleName;
  private final Module myModule;

  public SpringSecurityRolePsiReference(@NotNull PsiElement element,
                                        @NotNull String roleName,
                                        @NotNull Module module) {
    super(element, TextRange.from(element.getText().indexOf(roleName), roleName.length()));
    myRoleName = roleName;
    myModule = module;
  }

  public PsiElement resolve() {

    return SpringSecurityRolesFactory.getInstance(myModule).getOrCreateRole(myRoleName, getElement().getContainingFile());
  }

  public boolean isSoft() {
    return true;
  }

  public Object[] getVariants() {
    Set<SpringSecurityRole> roles = SpringSecurityRolesFactory.getInstance(myModule).getRoles();

    return roles.toArray(new SpringSecurityRole[roles.size()]);
  }
}
