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
 * Resolves factory product type for a ScopedProxyFactoryBean.
 *
 * @author Taras Tielkes
 */
@SuppressWarnings({"UnnecessaryFullyQualifiedName"})
public class ScopedProxyFactoryBeanTypeResolver extends AbstractProxiedTypeResolver {
  @NonNls private static final String FACTORY_CLASS = "org.springframework.aop.scope.ScopedProxyFactoryBean";

  @NonNls private static final String TARGET_BEAN_NAME_PROPERTY_NAME = "targetBeanName";

  @Nonnull
  public Set<String> getObjectType(@Nonnull CommonSpringBean context) {
    if (context instanceof SpringBean) {
      SpringBean springBean = (SpringBean) context;
      PsiClassType type = getTargetType(springBean);

      if (type != null) {
        if (isBooleanProperySetAndFalse(springBean, PROXY_CLASS_FLAG_PROPERTY_NAME)) {
          Set<String> targetInterfaceNames = getAllInterfaceNames(type);
          if (!targetInterfaceNames.isEmpty()) {
            return targetInterfaceNames;
          }
        }
        return Collections.singleton(type.getCanonicalText());
      }
    }
    return Collections.emptySet();
  }

  @Nullable
  private static PsiClassType getTargetType(@Nonnull SpringBean context) {
    String targetBeanName = getPropertyValue(context, TARGET_BEAN_NAME_PROPERTY_NAME);
    if (targetBeanName != null) {
      PsiClassType fromTargetName = getTypeFromBeanName(context, targetBeanName);
      if (fromTargetName != null) {
        return fromTargetName;
      }
    }
    return null;
  }

  public boolean accept(@Nonnull String factoryClassName) {
    return FACTORY_CLASS.equals(factoryClassName);
  }
}
