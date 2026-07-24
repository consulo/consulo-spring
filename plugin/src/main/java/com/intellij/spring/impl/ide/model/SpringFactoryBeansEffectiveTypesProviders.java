package com.intellij.spring.impl.ide.model;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.factories.SpringFactoryBeansManager;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import consulo.annotation.component.ExtensionImpl;

import jakarta.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

@ExtensionImpl(order = "first")
public class SpringFactoryBeansEffectiveTypesProviders extends SpringBeanEffectiveTypeProvider {

  public void processEffectiveTypes(@Nonnull CommonSpringBean bean, Collection<PsiClass> result) {
    PsiClass beanClass = bean.getBeanClass();
    if (beanClass == null || !SpringFactoryBeansManager.isBeanFactory(beanClass)) return;

    result.clear();

    result.addAll(Arrays.asList(SpringFactoryBeansManager.getInstance().getProductTypes(beanClass, bean)));
  }
}
