package com.intellij.spring.impl.ide.model.actions.generate;

import consulo.codeEditor.Editor;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringInjection;
import com.intellij.spring.impl.ide.model.xml.beans.SpringProperty;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomElementNavigationProvider;
import consulo.xml.util.xml.actions.generate.AbstractDomGenerateProvider;
import consulo.util.lang.Pair;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

public class SpringSetterDependenciesGenerateProvider extends AbstractDomGenerateProvider<SpringProperty> {
  public SpringSetterDependenciesGenerateProvider() {
    super(SpringBundle.message("spring.generate.setter.dependencies"), SpringProperty.class);
  }

  protected DomElement getParentDomElement(Project project, Editor editor, PsiFile file) {
    return SpringUtils.getSpringBeanForCurrentCaretPosition(editor, file);
  }

  public SpringProperty generate(@Nullable DomElement parent, Editor editor) {

    if (!(parent instanceof SpringBean)) return null;
    List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> list =
      GenerateSpringBeanDependenciesUtil.generateDependenciesFor((SpringBean)parent, true);

    SpringGenerateTemplatesHolder merged = new SpringGenerateTemplatesHolder(editor.getProject() );
    for (Pair<SpringInjection, SpringGenerateTemplatesHolder> pair : list) {
      merged.addAll(pair.getSecond());
    }
    merged.runTemplates();

    return list.size() > 0 ? (SpringProperty)list.get(0).getFirst() : null;
  }

  protected void doNavigate(DomElementNavigationProvider navigateProvider, DomElement element) {
  }

  public boolean isAvailableForElement(@Nonnull DomElement contextElement) {
    return contextElement instanceof SpringBean && GenerateSpringBeanDependenciesUtil.acceptBean((SpringBean)contextElement, true);
  }
}
