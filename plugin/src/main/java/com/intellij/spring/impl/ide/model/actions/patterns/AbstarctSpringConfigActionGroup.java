package com.intellij.spring.impl.ide.model.actions.patterns;

import com.intellij.spring.impl.ide.SpringManager;
import consulo.codeEditor.Editor;
import consulo.document.util.TextRange;
import consulo.language.editor.PlatformDataKeys;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.ui.ex.action.Presentation;
import consulo.ui.image.Image;

import consulo.xml.language.psi.XmlDocument;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.language.psi.XmlTag;
import jakarta.annotation.Nullable;

public abstract class AbstarctSpringConfigActionGroup extends DefaultActionGroup {

  public AbstarctSpringConfigActionGroup() {
    setPopup(true);
  }

  public void update(AnActionEvent event) {
    Presentation presentation = event.getPresentation();

    Project project = event.getData(Project.KEY);
    Editor editor = event.getData(PlatformDataKeys.EDITOR);
    if (project == null || editor == null) {
      presentation.setVisible(false);
      return;
    }

    PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    boolean enabled =
      file instanceof XmlFile && SpringManager.getInstance(project).isSpringBeans((XmlFile)file) && isInsideRootTag(editor, (XmlFile)file);

    presentation.setEnabled(enabled);
    presentation.setVisible(enabled);
    if (enabled) {
      event.getPresentation().setText(getDescription());
      event.getPresentation().setIcon(getIcon());
    }
  }

  private static boolean isInsideRootTag(Editor editor, XmlFile xmlFile) {
    XmlDocument document = xmlFile.getDocument();
    if (document != null) {
      XmlTag tag = document.getRootTag();
      if (tag != null) {
        TextRange textRange = tag.getTextRange();
        if (textRange.contains(editor.getCaretModel().getOffset()))  {
          return true;
        }
      }
    }
     return false;
  }

  @Nullable
  protected Image getIcon() {
    return null;
  }

  protected abstract String getDescription();
}