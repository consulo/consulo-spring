package com.intellij.spring.security.model.xml.converters;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.spring.security.model.xml.converters.roles.SpringSecurityRolesFactory;
import com.intellij.spring.security.references.SpringSecurityRolePsiReference;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Serega.Vasiliev
 */
public class InterceptUrlAccessRolesConverter extends SpringSecurityRolesConverter {

  @Override
  protected SpringSecurityRolePsiReference createReference(PsiElement element, @NotNull final Module module, String roleName, final int i) {
    return new SpringSecurityRolePsiReference(element, roleName, module) {
      @Override
      public Object[] getVariants() {
        if (i > 0 ) return super.getVariants();

        Set<Object> variants = new HashSet<Object>();

        variants.addAll(SecurityExpressionRootMethodsConverter.getExpressionRootMethods(module));
        variants.addAll(SpringSecurityRolesFactory.getInstance(module).getRoles());

        return variants.toArray(new Object[variants.size()]);
      }
    };
  }

}
