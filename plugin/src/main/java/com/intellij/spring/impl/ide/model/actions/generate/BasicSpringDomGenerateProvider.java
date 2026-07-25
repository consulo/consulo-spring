package com.intellij.spring.impl.ide.model.actions.generate;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.spring.impl.DomSpringModel;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.dom.DomElement;
import consulo.xml.util.xml.actions.generate.AbstractDomGenerateProvider;

/**
 * @author Sergey.Vasiliev
 */
public abstract class BasicSpringDomGenerateProvider<T extends DomElement> extends AbstractDomGenerateProvider<T> {
  protected BasicSpringDomGenerateProvider(LocalizeValue description, Class<T> tClass) {
    super(description.get(), tClass);
  }

  protected BasicSpringDomGenerateProvider(LocalizeValue description, Class<T> tClass, String mappingId) {
    super(description.get(), tClass, mappingId);
  }

  @Override
  protected DomElement getParentDomElement(Project project, Editor editor, PsiFile file) {
    SpringModel springModel = SpringManager.getInstance(project).getLocalSpringModel((XmlFile)file);
    if(springModel instanceof DomSpringModel domSpringModel) {
      return domSpringModel.getDomModel().getMergedModel();
    }
    return null;
  }
}
