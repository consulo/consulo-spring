package com.intellij.spring.impl.ide.factories.resolvers;

import com.intellij.spring.impl.ide.factories.ObjectTypeResolver;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.jee.SpringJeeElement;
import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.Set;

public abstract class AbstractJeeSchemaTypeResolver implements ObjectTypeResolver {
  private FactoryPropertiesDependentTypeResolver myPropertyDependentResolver;

  @Nonnull
  @Override
  public Set<String> getObjectType(@Nonnull CommonSpringBean context) {
    if (context instanceof SpringJeeElement) {
      return getJeeObjectType(context);
    }
    else {
      return getPropertyDependentResolver().getObjectType(context);
    }
  }

  @Override
  public boolean accept(@Nonnull String factoryClassName) {
    return getFactoryClasses().contains(factoryClassName);
  }

  public FactoryPropertiesDependentTypeResolver getPropertyDependentResolver() {
    if (myPropertyDependentResolver == null) {
      myPropertyDependentResolver = new FactoryPropertiesDependentTypeResolver(getProperties());
    }
    return myPropertyDependentResolver;
  }

  protected abstract Set<String> getJeeObjectType(CommonSpringBean context);

  protected abstract List<String> getProperties();

  protected abstract List<String> getFactoryClasses();
}
