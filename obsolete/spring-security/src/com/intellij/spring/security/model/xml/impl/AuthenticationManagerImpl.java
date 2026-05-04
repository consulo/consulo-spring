package com.intellij.spring.security.model.xml.impl;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.spring.security.model.xml.AuthenticationManager;
import com.intellij.spring.security.model.ModelVersion;
import com.intellij.spring.security.model.SpringSecurityVersion;
import org.jetbrains.annotations.NotNull;

@ModelVersion(SpringSecurityVersion.SpringSecurity_2_0)
public abstract class AuthenticationManagerImpl extends DomSpringBeanImpl implements AuthenticationManager {


  @Override
  public String getBeanName() {
    return getAlias().getStringValue();
  }

  @Override
  public void setName(@NotNull String newName) {
     if (getBeanName() != null) {
      getAlias().setStringValue(newName);
    }
  }

  @NotNull
  public String getClassName() {
    return SpringSecurityClassesConstants.AUTHENTICATION_MANAGER;
  }
}