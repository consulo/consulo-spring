package com.intellij.spring.impl.ide.model.actions;

import com.intellij.spring.impl.ide.SpringManager;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.util.xml.ui.actions.generate.GenerateDomElementAction;
import consulo.xml.util.xml.ui.actions.generate.GenerateDomElementProvider;

public class GenerateSpringDomElementAction extends GenerateDomElementAction {

  public GenerateSpringDomElementAction(GenerateDomElementProvider provider) {
    super(provider);
  }
  
  public GenerateSpringDomElementAction(GenerateDomElementProvider provider, Image icon) {
    super(provider);
    getTemplatePresentation().setIcon(icon);
  }

  public boolean isValidForFile(Project project, Editor editor, PsiFile file) {
    return super.isValidForFile(project, editor, file) &&
           file instanceof XmlFile &&
           SpringManager.getInstance(project).isSpringBeans((XmlFile)file);
  }
}