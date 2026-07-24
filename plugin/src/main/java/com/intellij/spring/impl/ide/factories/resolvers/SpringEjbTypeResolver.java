package com.intellij.spring.impl.ide.factories.resolvers;

import consulo.util.lang.StringUtil;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.jee.SpringEjb;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SpringEjbTypeResolver extends AbstractJeeSchemaTypeResolver {
  @NonNls private static final String[] myFactoryClasses = new String[]{"org.springframework.ejb.access.LocalStatelessSessionProxyFactoryBean",
    "org.springframework.ejb.access.SimpleRemoteStatelessSessionProxyFactoryBean"};
  @NonNls private static final String[] myProperties = new String[]{"businessInterface"};

  @Nonnull
  protected Set<String> getJeeObjectType(CommonSpringBean context) {
    if (context instanceof SpringEjb) {
      SpringEjb springEjb = (SpringEjb)context;
      if (StringUtil.isNotEmpty(springEjb.getBusinessInterface().getStringValue())) {
        return Collections.singleton(springEjb.getBusinessInterface().getStringValue());
      }
    }
    return Collections.emptySet();
  }

  protected List<String> getProperties() {
    return Arrays.asList(myProperties);
  }

  protected List<String> getFactoryClasses() {
    return Arrays.asList(myFactoryClasses);
  }
}
