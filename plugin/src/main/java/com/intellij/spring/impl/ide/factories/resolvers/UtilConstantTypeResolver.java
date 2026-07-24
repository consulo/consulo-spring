package com.intellij.spring.impl.ide.factories.resolvers;

import com.intellij.java.language.psi.*;
import com.intellij.spring.impl.ide.factories.ObjectTypeResolver;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.util.SpringConstant;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.util.lang.StringUtil;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * @author Taras Tielkes
 */
public class UtilConstantTypeResolver implements ObjectTypeResolver {
  @NonNls private static final String FACTORY_CLASS = "org.springframework.beans.factory.config.FieldRetrievingFactoryBean";
  @NonNls private static final char SEPARATOR = '.';

  @Nonnull
  public Set<String> getObjectType(@Nonnull CommonSpringBean context) {
    if (context instanceof SpringConstant) {
      SpringConstant constant = (SpringConstant)context;
      String staticField = StringUtil.notNullize(constant.getStaticField().getStringValue());

      int lastDotIndex = staticField.lastIndexOf(SEPARATOR);
      if (lastDotIndex != -1) {
        String className = staticField.substring(0, lastDotIndex);
        String fieldName = staticField.substring(lastDotIndex + 1);

        PsiClass psiClass = findClassByExternalName(context, className);
        if (psiClass != null) {
          PsiField psiField = psiClass.findFieldByName(fieldName, true);
          if (psiField != null) {
            PsiType type = psiField.getType();
            if (type instanceof PsiPrimitiveType) {
              String boxedTypeName = ((PsiPrimitiveType)type).getBoxedTypeName();
              return Collections.singleton(boxedTypeName);
            }
            if (type instanceof PsiClassType) {
              PsiClass typeClass = ((PsiClassType)type).resolve();
              if (typeClass != null) {
                String qualifiedName = typeClass.getQualifiedName();
                if (qualifiedName != null) {
                  return Collections.singleton(qualifiedName);
                }
              }
            }
          }
        }
      }
    }

    return Collections.emptySet();
  }

  @Nullable
  private static PsiClass findClassByExternalName(@Nonnull CommonSpringBean context, @Nonnull String externalName) {
    Module module = context.getModule();
    if (module != null) {
      GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false);
      PsiManager psiManager = context.getPsiManager();
      String className = externalName.replace('$', '.');
      return JavaPsiFacade.getInstance(psiManager.getProject()).findClass(className, scope);
    }
    return null;
  }

  public boolean accept(@Nonnull String factoryClassName) {
    return factoryClassName.equals(FACTORY_CLASS);
  }
}
