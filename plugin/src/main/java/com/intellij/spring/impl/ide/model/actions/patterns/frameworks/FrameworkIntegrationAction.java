package com.intellij.spring.impl.ide.model.actions.patterns.frameworks;

import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBeanPointer;
import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.language.editor.LangDataKeys;
import consulo.language.editor.PlatformDataKeys;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Presentation;
import consulo.ui.image.Image;
import consulo.xml.language.psi.XmlFile;
import jakarta.annotation.Nullable;
import java.util.Collection;

public abstract class FrameworkIntegrationAction extends AnAction {
  @Override
  @RequiredUIAccess
  public void actionPerformed(AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();

    final consulo.module.Module module = getModule(dataContext);
    final Editor editor = getEditor(dataContext);
    final XmlFile xmlFile = getXmlFile(dataContext);

    if (module != null && editor != null && xmlFile != null) {
      generateSpringBeans(module, editor, xmlFile);
    }
  }

  public void update(final AnActionEvent event) {
    Presentation presentation = event.getPresentation();
    DataContext dataContext = event.getDataContext();

    XmlFile file = getXmlFile(dataContext);

    final boolean isSpringBeanFile = file != null && SpringManager.getInstance(getProject(dataContext)).isSpringBeans(file);

    final boolean enabled = isSpringBeanFile && accept(file);

    presentation.setEnabled(enabled);
    presentation.setVisible(enabled);
    if (enabled) {
      event.getPresentation().setText(getDescription());
      event.getPresentation().setIcon(getIcon());
    }
  }

  @Nullable
  protected static XmlFile getXmlFile(final DataContext dataContext) {
    return getXmlFile(getProject(dataContext), getEditor(dataContext));
  }

  @Nullable
  @RequiredReadAction
  protected static XmlFile getXmlFile(final Project project, final Editor editor) {
    if (project == null || editor == null) return null;

    final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    return psiFile instanceof XmlFile ? (XmlFile)psiFile : null;
  }

  @Nullable
  protected static Editor getEditor(final DataContext dataContext) {
    return dataContext.getData(PlatformDataKeys.EDITOR);
  }

  @Nullable
  protected static Project getProject(final DataContext dataContext) {
    return dataContext.getData(PlatformDataKeys.PROJECT);
  }

  @Nullable
  protected static consulo.module.Module getModule(final DataContext dataContext) {
    return dataContext.getData(LangDataKeys.MODULE);
  }

  protected boolean accept(final XmlFile file) {
    return acceptBeansByClassNames(file, getBeansClassNames());
  }

  protected String[] getBeansClassNames() {
    return new String[0];
  }

  private static boolean acceptBeansByClassNames(XmlFile file, String... classNames) {
    if (classNames.length == 0) return true;

    final SpringModel model = SpringManager.getInstance(file.getProject()).getSpringModelByFile(file);
    final Collection<? extends SpringBeanPointer> allBeans = model.getAllCommonBeans();
    for (String className : classNames) {
      if (SpringUtils.findBeansByClassName(allBeans, className).size() > 0) return false;
    }
    return true;
  }

  protected abstract void generateSpringBeans(final Module module, final Editor editor, final XmlFile xmlFile);

  @Nullable
  protected Image getIcon() {
    return null;
  }

  protected abstract LocalizeValue getDescription();
}
