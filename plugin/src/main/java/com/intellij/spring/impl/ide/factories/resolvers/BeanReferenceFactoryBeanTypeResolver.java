package com.intellij.spring.impl.ide.factories.resolvers;

import com.intellij.java.language.psi.PsiClassType;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import jakarta.annotation.Nonnull;

import java.util.Collections;
import java.util.Set;

/**
 * @author Taras Tielkes
 */
public class BeanReferenceFactoryBeanTypeResolver extends AbstractTypeResolver {
  private static final String FACTORY_CLASS = "org.springframework.beans.factory.config.BeanReferenceFactoryBean";
  private static final String TARGET_NAME_PROPERTY_NAME = "targetBeanName";

  @Nonnull
  @Override
  public Set<String> getObjectType(@Nonnull CommonSpringBean context) {
    if (context instanceof SpringBean bean) {
      String targetBeanName = getPropertyValue(context, TARGET_NAME_PROPERTY_NAME);
      if (targetBeanName != null) {
        PsiClassType fromTargetName = getTypeFromBeanName(bean, targetBeanName);
        if (fromTargetName != null) {
          return Collections.singleton(fromTargetName.getCanonicalText());
        }
      }
    }
    return Collections.emptySet();
  }

  @Override
  public boolean accept(@Nonnull String factoryClassName) {
    return FACTORY_CLASS.equals(factoryClassName);
  }
}
