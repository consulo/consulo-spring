package com.intellij.spring.security.model.extenders;

import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.security.constants.SpringSecurityConstants;
import com.intellij.spring.security.model.xml.*;
import com.intellij.util.xml.XmlName;
import com.intellij.util.xml.reflect.DomExtender;
import com.intellij.util.xml.reflect.DomExtensionsRegistrar;
import org.jetbrains.annotations.NotNull;

public class SpringSecurityBeansDomExtender extends DomExtender<Beans> {

    public void registerExtensions(@NotNull final Beans element, @NotNull final DomExtensionsRegistrar registrar) {

      registrar.registerCollectionChildrenExtension(new XmlName("authentication-manager", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), AuthenticationManager.class);
      registrar.registerCollectionChildrenExtension(new XmlName("authentication-provider", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), AuthenticationProvider.class);
      //registrar.registerCollectionChildrenExtension(new XmlName("custom-after-invocation-provider", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), CAIP.class);
      //registrar.registerCollectionChildrenExtension(new XmlName("custom-authentication-provider", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), CAP.class);
      registrar.registerCollectionChildrenExtension(new XmlName("custom-filter", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), CustomFilter.class);
      registrar.registerCollectionChildrenExtension(new XmlName("expression-handler", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), ExpressionHandler.class);
      registrar.registerCollectionChildrenExtension(new XmlName("filter-chain-map", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), FilterChainMap.class);
      registrar.registerCollectionChildrenExtension(new XmlName("filter-invocation-definition-source", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), FilterInvocationDefinitionSource.class);
      registrar.registerCollectionChildrenExtension(new XmlName("global-method-security", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), GlobalMethodSecurity.class);
      registrar.registerCollectionChildrenExtension(new XmlName("http", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), Http.class);
      registrar.registerCollectionChildrenExtension(new XmlName("intercept-methods", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), InterceptMethods.class);
      registrar.registerCollectionChildrenExtension(new XmlName("jdbc-user-service", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), JdbcUserService.class);
      registrar.registerCollectionChildrenExtension(new XmlName("ldap-authentication-provider", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), LdapAuthenticationProvider.class);
      registrar.registerCollectionChildrenExtension(new XmlName("ldap-server", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), LdapServer.class);
      registrar.registerCollectionChildrenExtension(new XmlName("ldap-user-service", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), LdapUserService.class);
      registrar.registerCollectionChildrenExtension(new XmlName("openid-login", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), OpenidLogin.class);
      registrar.registerCollectionChildrenExtension(new XmlName("port-mapping", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), PortMapping.class);
      registrar.registerCollectionChildrenExtension(new XmlName("user", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), User.class);
      registrar.registerCollectionChildrenExtension(new XmlName("user-service", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), UserService.class);
    }
}
