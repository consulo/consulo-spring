package com.intellij.spring.security.constants;

import org.jetbrains.annotations.NonNls;

public interface SpringSecurityClassesConstants {
  @NonNls String AUTHENTICATION_ENTRY_POINT = "org.springframework.security.web.AuthenticationEntryPoint";
  @NonNls String AUTHENTICATION_FAILURE_HANDLER = "org.springframework.security.web.authentication.AuthenticationFailureHandler";
  @NonNls String AUTHENTICATION_MANAGER = "org.springframework.security.authentication.AuthenticationManager";
  @NonNls String AUTHENTICATION_SUCCESS_HANDLER = "org.springframework.security.web.authentication.AuthenticationSuccessHandler";

  @NonNls String ACCESS_DECISION_MANAGER = "org.springframework.security.access.AccessDecisionManager";
  @NonNls String DATA_SOURCE = "javax.sql.DataSource";
  @NonNls String PERSISTENT_TOKEN_REPOSITORY = "org.springframework.security.web.authentication.rememberme.PersistentTokenRepository";
  @NonNls String REMEMBER_ME_SERVICES = "org.springframework.security.web.authentication.RememberMeServices";
  @NonNls String RUNS_AS_MANAGER = "org.springframework.security.access.intercept.RunAsManager";
  @NonNls String SECURITY_CONTEXT_REPOSITORY = "org.springframework.security.web.context.SecurityContextRepository";
  @NonNls String SESSION_REGISTRY = "org.springframework.security.authentication.concurrent.SessionRegistry";
  @NonNls String USER_DETAILS_SERVICE = "org.springframework.security.core.userdetails.UserDetailsService";

  @NonNls String LDAP_CONTEXT_SOURCE = "org.springframework.ldap.core.support.LdapContextSource";
  @NonNls String DELEGATING_FILTER_PROXY= "org.springframework.web.filter.DelegatingFilterProxy";
  @NonNls String FILTER_CHAIN_PROXY= "org.springframework.security.web.FilterChainProxy";
  @NonNls String JAVAX_SEVLET_FILTER= "javax.servlet.Filter";

  @NonNls String GRANTED_AUTHORITY= "org.springframework.security.core.authority.GrantedAuthorityImpl";
  @NonNls String SECURITY_EXPRESSION_ROOT = "org.springframework.security.access.expression.SecurityExpressionRoot";

  @NonNls String METHOD_SECURITY_EXPRESSION_HANDLER = "org.springframework.security.access.expression.method.MethodSecurityExpressionHandler";
}