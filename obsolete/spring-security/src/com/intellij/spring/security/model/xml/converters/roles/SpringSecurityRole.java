package com.intellij.spring.security.model.xml.converters.roles;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.RenameableFakePsiElement;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SpringSecurityRole extends RenameableFakePsiElement {
  private String myRoleName;

  public SpringSecurityRole(@NotNull final String roleName, final PsiFile containingFile) {
    super(containingFile);
    myRoleName = roleName;
  }

  @NotNull
  public String getName() {
    return myRoleName;
  }

  public PsiElement getParent() {
    return getContainingFile();
  }

  public String getTypeName() {
    return SpringBundle.message("security.role.type");
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    final PsiElement res = super.setName(name);
    myRoleName = name;
    return res;
  }

  public Icon getIcon() {
    return SpringIcons.SPRING_ICON;
  }
}