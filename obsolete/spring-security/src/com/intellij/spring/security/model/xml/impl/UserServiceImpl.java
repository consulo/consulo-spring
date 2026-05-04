package com.intellij.spring.security.model.xml.impl;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import com.intellij.spring.security.model.xml.UserService;
import org.jetbrains.annotations.NotNull;

public abstract class UserServiceImpl extends DomSpringBeanImpl implements UserService {

  @NotNull
  public String getClassName() {
    return SpringSecurityClassesConstants.USER_DETAILS_SERVICE; //todo correct ??
  }
}