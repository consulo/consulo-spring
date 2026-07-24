package com.intellij.spring.impl.ide.factories.resolvers;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Taras Tielkes
 */
public abstract class AbstractProxiedTypeResolver extends AbstractTypeResolver {

  @NonNls protected static final String PROXY_CLASS_FLAG_PROPERTY_NAME = "proxyTargetClass";
  @NonNls protected static final String OPTIMIZE_PROPERTY_NAME = "optimize";

  // @see org.springframework.util.ClassUtils#getAllInterfacesForClass(Class clazz)
  @Nonnull
  protected static Set<PsiClass> getAllInterfaces(@Nonnull PsiClassType classType) {

    PsiClass psiClass = classType.resolve();
    if (psiClass != null) {

      if (psiClass.isInterface()) {
        return Collections.singleton(psiClass);
      }

      Set<PsiClass> interfaces = new HashSet<PsiClass>();
      while (psiClass != null) {
        interfaces.addAll(Arrays.asList(psiClass.getInterfaces()));
        psiClass = psiClass.getSuperClass();
      }
      return interfaces;
    }
    return Collections.emptySet();
  }

  @Nonnull
  protected static Set<String> getAllInterfaceNames(@Nonnull PsiClassType type) {
    Set<PsiClass> interfaces = getAllInterfaces(type);
    Set<String> names = new HashSet<String>(interfaces.size());
    for (PsiClass anInterface : interfaces) {
      names.add(anInterface.getQualifiedName());
    }
    return names;
  }

  protected static boolean isCglibExplicitlyEnabled(@Nonnull SpringBean context) {
    return isBooleanProperySetAndTrue(context, PROXY_CLASS_FLAG_PROPERTY_NAME) ||
        isBooleanProperySetAndTrue(context, OPTIMIZE_PROPERTY_NAME);
  }
}
