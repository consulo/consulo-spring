package com.intellij.spring.impl.ide.factories.resolvers;

import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.jee.JndiLookup;
import com.intellij.spring.impl.ide.model.xml.jee.SpringJeeElement;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;

import java.util.*;

public class JndiObjectFactoryBeanTypeResolver extends AbstractJeeSchemaTypeResolver {
  private static final String FACTORY_CLASS = "org.springframework.jndi.JndiObjectFactoryBean";

  private static final String[] myProperties = new String[] {"proxyInterface","expectedType"};
  
  @Nonnull
  @Override
  protected Set<String> getJeeObjectType(CommonSpringBean context) {
    Set<String> result = new HashSet<>();
    if (context instanceof SpringJeeElement) {
      JndiLookup jndiLookup = (JndiLookup)context;
      if (StringUtil.isNotEmpty(jndiLookup.getProxyInterface().getStringValue())) {
        result.add(jndiLookup.getProxyInterface().getStringValue());
      } else if (StringUtil.isNotEmpty(jndiLookup.getExpectedType().getStringValue())) {
        result.add(jndiLookup.getExpectedType().getStringValue());
      }
    }
    return result;
  }

  @Override
  protected List<String> getProperties() {
    return Arrays.asList(myProperties);
  }

  @Override
  protected List<String> getFactoryClasses() {
    return Collections.singletonList(FACTORY_CLASS);  
  }
}
