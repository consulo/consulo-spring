package com.intellij.spring.security.model.xml.impl;

import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.security.model.xml.LdapServer;
import com.intellij.spring.security.constants.SpringSecurityClassesConstants;
import org.jetbrains.annotations.NotNull;

public abstract class LdapServerImpl extends DomSpringBeanImpl implements LdapServer{

  @NotNull
  public String getClassName() {
    return SpringSecurityClassesConstants.LDAP_CONTEXT_SOURCE;
  }
}
