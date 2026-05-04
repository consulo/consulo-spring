package com.intellij.spring.security.model.extenders;

import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.security.constants.SpringSecurityConstants;
import com.intellij.spring.security.model.xml.FilterChainMap;
import com.intellij.spring.security.model.xml.InterceptMethods;
import com.intellij.util.xml.XmlName;
import com.intellij.util.xml.reflect.DomExtender;
import com.intellij.util.xml.reflect.DomExtensionsRegistrar;
import org.jetbrains.annotations.NotNull;

public class SpringSecurityBeanDomExtender extends DomExtender<SpringBean> {

    public void registerExtensions(@NotNull final SpringBean element, @NotNull final DomExtensionsRegistrar registrar) {
      registrar.registerCollectionChildrenExtension(new XmlName("filter-chain-map", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), FilterChainMap.class);
      registrar.registerCollectionChildrenExtension(new XmlName("intercept-methods", SpringSecurityConstants.SECURITY_NAMESPACE_KEY), InterceptMethods.class);
    }
}