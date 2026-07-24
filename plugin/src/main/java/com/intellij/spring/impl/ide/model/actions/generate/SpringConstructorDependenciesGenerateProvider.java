package com.intellij.spring.impl.ide.model.actions.generate;

import consulo.codeEditor.Editor;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.util.lang.Pair;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.ConstructorArg;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringInjection;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomElementNavigationProvider;
import consulo.xml.util.xml.actions.generate.AbstractDomGenerateProvider;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

public class SpringConstructorDependenciesGenerateProvider extends AbstractDomGenerateProvider<ConstructorArg> {
  public SpringConstructorDependenciesGenerateProvider() {
    super(SpringBundle.message("spring.generate.constructor.dependencies"), ConstructorArg.class);
  }

  protected DomElement getParentDomElement(Project project, Editor editor, PsiFile file) {
    return SpringUtils.getSpringBeanForCurrentCaretPosition(editor, file);
  }

  public ConstructorArg generate(@Nullable DomElement parent, Editor editor) {
    if ( !(parent instanceof SpringBean)) return null;
    List<Pair<SpringInjection,SpringGenerateTemplatesHolder>> list = GenerateSpringBeanDependenciesUtil.generateDependenciesFor((SpringBean)parent, false);

    for (Pair<SpringInjection, SpringGenerateTemplatesHolder> pair : list) {
      pair.getSecond().runTemplates();
    }
    return  list.size() > 0 ? (ConstructorArg)list.get(0).getFirst() : null;
  }

  protected void doNavigate(DomElementNavigationProvider navigateProvider, DomElement element) {
   //super.doNavigate(navigateProvider, element);
  }
  
  public boolean isAvailableForElement(@Nonnull DomElement contextElement) {
    return contextElement instanceof SpringBean && GenerateSpringBeanDependenciesUtil.acceptBean((SpringBean)contextElement, false);
  }

}
