package com.intellij.spring.impl.ide.model;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.component.extension.ExtensionPointName;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.editor.DomElementAnnotationHolder;

import jakarta.annotation.Nonnull;
import java.util.Collection;

@ExtensionAPI(ComponentScope.APPLICATION)
public abstract class SpringBeanEffectiveTypeProvider {
  public static final ExtensionPointName<SpringBeanEffectiveTypeProvider> EP_NAME =
    ExtensionPointName.create(SpringBeanEffectiveTypeProvider.class);

  public abstract void processEffectiveTypes(@Nonnull CommonSpringBean bean, Collection<PsiClass> result);

  public boolean createCustomProblem(@Nonnull CommonSpringBean bean,
                                     @Nonnull PsiType expectedType,
                                     DomElementAnnotationHolder holder,
                                     DomElement element) {
    return false;
  }

}
