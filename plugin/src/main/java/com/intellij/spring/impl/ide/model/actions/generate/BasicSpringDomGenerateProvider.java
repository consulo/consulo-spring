package com.intellij.spring.impl.ide.model.actions.generate;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.spring.impl.DomSpringModel;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.dom.DomElement;
import consulo.xml.util.xml.actions.generate.AbstractDomGenerateProvider;

/**
 * User: Sergey.Vasiliev
 */
public abstract class BasicSpringDomGenerateProvider<T extends DomElement> extends AbstractDomGenerateProvider<T> {

  protected BasicSpringDomGenerateProvider(String description, Class<T> tClass) {
    super(description, tClass);
  }

  protected BasicSpringDomGenerateProvider(String description, Class<T> tClass, String mappingId) {
    super(description, tClass, mappingId);
  }

  @Override
  protected DomElement getParentDomElement(Project project, Editor editor, PsiFile file) {
    SpringModel springModel = SpringManager.getInstance(project).getLocalSpringModel((XmlFile)file);
    if(springModel instanceof DomSpringModel) {
      return ((DomSpringModel) springModel).getDomModel().getMergedModel();
    }
    return null;
  }
}
