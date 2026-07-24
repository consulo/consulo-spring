package com.intellij.spring.impl.ide.factories.resolvers;

import com.intellij.java.language.psi.PsiClassType;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * Resolves factory product type for a TransactionProxyFactoryBean.
 * <p/>
 * The approach mirrors the logic in {@link org.springframework.aop.framework.AbstractSingletonProxyFactoryBean} and
 * {@link org.springframework.aop.framework.DefaultAopProxyFactory}:
 * <ol>
 * <li>If "proxyTargetClass" or "optimize" is set to true, resolve to type of target property
 * <li>If "proxyInterfaces" is set, resolve to a type implementing the specified interfaces
 * <li>If target type is known, resolve to a type implementing all interfaces of target
 * <li>Otherwise, fall back to type of target
 * </ol>
 * <p/>
 * The "proxyInterfaces" property (of type Class[]) can be set in various ways:
 * <ul>
 * <li>One or more values, comma-separated (using {@link org.springframework.beans.propertyeditors.ClassArrayEditor})
 * <li>List or Set containing single string values (using {@link org.springframework.beans.propertyeditors.ClassEditor})
 * </ul>
 *
 * @author Taras Tielkes
 */
@SuppressWarnings({"UnnecessaryFullyQualifiedName"})
public class TransactionProxyFactoryBeanTypeResolver extends AbstractProxiedTypeResolver {
  @NonNls private static final String FACTORY_CLASS = "org.springframework.transaction.interceptor.TransactionProxyFactoryBean";

  @NonNls private static final String PROXY_INTERFACES_PROPERTY_NAME = "proxyInterfaces";
  @NonNls private static final String TARGET_PROPERTY_NAME = "target";

  @Nonnull
  public Set<String> getObjectType(@Nonnull CommonSpringBean context) {
    if (context instanceof SpringBean) {
      SpringBean springBean = (SpringBean) context;
      PsiClassType type = getTargetType(springBean);

      if (isCglibExplicitlyEnabled(springBean) && type != null) {
        return Collections.singleton(type.getCanonicalText());
      }

      Set<String> proxyInterfaceNames = getTypesFromClassArrayProperty(springBean, PROXY_INTERFACES_PROPERTY_NAME);
      if (!proxyInterfaceNames.isEmpty()) {
        return proxyInterfaceNames;
      }

      if (type != null) {
        Set<String> targetInterfaceNames = getAllInterfaceNames(type);
        if (!targetInterfaceNames.isEmpty()) {
          return targetInterfaceNames;
        } else {
          return Collections.singleton(type.getCanonicalText());
        }
      }
    }
    return Collections.emptySet();
  }

  @Nullable
  private static PsiClassType getTargetType(@Nonnull SpringBean context) {
    return getTypeFromProperty(context, TARGET_PROPERTY_NAME);
  }

  public boolean accept(@Nonnull String factoryClassName) {
    return FACTORY_CLASS.equals(factoryClassName);
  }
}
