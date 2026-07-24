package com.intellij.spring.impl.ide.factories.resolvers;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * Resolves factory product type for a ProxyFactoryBean.
 * <p/>
 * The approach mirrors the logic in {@link org.springframework.aop.framework.ProxyFactoryBean} and
 * {@link org.springframework.aop.framework.DefaultAopProxyFactory}:
 * <ol>
 * <li>If "proxyTargetClass" or "optimize" is set to true, resolve to type of target property
 * <li>If "proxyInterfaces" or "interfaces" is set, resolve to a type implementing the specified interfaces
 * <li>If target type is known, and "autodetectInterfaces" is not disabled, resolve to a type implementing
 * all interfaces of target
 * <li>Otherwise, fall back to type of target
 * </ol>
 *
 * @author Taras Tielkes
 */
@SuppressWarnings({"UnnecessaryFullyQualifiedName"})
public class ProxyFactoryBeanTypeResolver extends AbstractProxiedTypeResolver {
  @NonNls private static final String FACTORY_CLASS = "org.springframework.aop.framework.ProxyFactoryBean";

  @NonNls private static final String PROXY_INTERFACES_PROPERTY_NAME = "proxyInterfaces";
  @NonNls private static final String INTERFACES_PROPERTY_NAME = "interfaces";
  @NonNls private static final String AUTODETECT_INTERFACES_PROPERTY_NAME = "autodetectInterfaces";

  @NonNls private static final String TARGET_PROPERTY_NAME = "target";
  @NonNls private static final String TARGET_NAME_PROPERTY_NAME = "targetName";
  @NonNls private static final String TARGET_CLASS_PROPERTY_NAME = "targetClass";

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

      Set<String> interfaceNames = getTypesFromClassArrayProperty(springBean, INTERFACES_PROPERTY_NAME);
      if (!interfaceNames.isEmpty()) {
        return interfaceNames;
      }

      if (type != null) {
        Set<String> targetInterfaceNames = getAllInterfaceNames(type);
        if (!targetInterfaceNames.isEmpty() && isAutodetectInterfacesEnabled(springBean)) {
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
    PsiClassType fromTarget = getTypeFromProperty(context, TARGET_PROPERTY_NAME);
    if (fromTarget != null) {
      return fromTarget;
    }

    String targetName = getPropertyValue(context, TARGET_NAME_PROPERTY_NAME);
    if (targetName != null) {
      PsiClassType fromTargetName = getTypeFromBeanName(context, targetName);
      if (fromTargetName != null) {
        return fromTargetName;
      }
    }

    String targetClassName = getPropertyValue(context, TARGET_CLASS_PROPERTY_NAME);
    if (targetClassName != null) {
      Project project = context.getManager().getProject();
      PsiManager psiManager = PsiManager.getInstance(project);
      GlobalSearchScope scope = consulo.language.psi.scope.GlobalSearchScope.allScope(project);
      PsiClass targetClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(targetClassName, scope);
      if (targetClass != null) {
        return JavaPsiFacade.getInstance(psiManager.getProject()).getElementFactory().createType(targetClass);
      }
    }
    return null;
  }

  private static boolean isAutodetectInterfacesEnabled(@Nonnull SpringBean context) {
    return !isBooleanProperySetAndFalse(context, AUTODETECT_INTERFACES_PROPERTY_NAME);
  }

  public boolean accept(@Nonnull String factoryClassName) {
    return FACTORY_CLASS.equals(factoryClassName);
  }
}
