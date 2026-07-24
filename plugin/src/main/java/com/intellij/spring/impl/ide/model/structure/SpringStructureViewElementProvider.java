package com.intellij.spring.impl.ide.model.structure;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import consulo.annotation.component.ExtensionImpl;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.xml.ide.structureView.xml.XmlStructureViewElementProvider;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.DomManager;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class SpringStructureViewElementProvider implements XmlStructureViewElementProvider {

  @Nullable
  public StructureViewTreeElement createCustomXmlTagTreeElement(@Nonnull XmlTag tag) {
    PsiFile psiFile = tag.getContainingFile();
    Project project = tag.getProject();
    if (psiFile instanceof XmlFile && SpringManager.getInstance(project).isSpringBeans((XmlFile)psiFile)) {
      DomElement domElement = DomManager.getDomManager(project).getDomElement(tag);
      if (domElement instanceof Beans) {
        return new SpringModelTreeElement((XmlFile)psiFile, false);
      }
    }

    return null;
  }
}
