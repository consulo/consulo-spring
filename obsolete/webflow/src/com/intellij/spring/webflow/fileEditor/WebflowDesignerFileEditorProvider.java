package com.intellij.spring.webflow.fileEditor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.webflow.model.xml.WebflowDomModelManager;
import com.intellij.util.xml.ui.PerspectiveFileEditor;
import com.intellij.util.xml.ui.PerspectiveFileEditorProvider;
import org.jetbrains.annotations.NotNull;

public class WebflowDesignerFileEditorProvider extends PerspectiveFileEditorProvider {

  public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
    final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

    return psiFile instanceof XmlFile &&  WebflowDomModelManager.getInstance(project).isWebflow((XmlFile)psiFile);
  }

  @NotNull
  public PerspectiveFileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
    return new WebflowDesignerFileEditor(project, file);
  }

  public double getWeight() {
    return 0;
  }
}