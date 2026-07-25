package com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui;

import consulo.disposer.Disposer;
import consulo.language.editor.template.Template;
import consulo.project.Project;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.DialogWrapper;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

public class ChooseTemplatesDialogWrapper extends DialogWrapper {
  private final ChooseTemplatesForm myTemplatesForm;

  public ChooseTemplatesDialogWrapper(Project project, List<TemplateInfo> infos, LibrariesInfo libInfo, String frameworkTitle) {
    super(project, true);

    myTemplatesForm = new ChooseTemplatesForm(infos, libInfo);
    myTemplatesForm.getLibrariesValidationComponent().addValidityListener(this::setOKActionEnabled);
    setOKActionEnabled(myTemplatesForm.getLibrariesValidationComponent().isValid());
    setTitle(SpringLocalize.springChooseBeanTemplatesDialogTitle(frameworkTitle));

    init();
  }

  @Override
  protected Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

  @Override
  protected JComponent createCenterPanel() {
    return myTemplatesForm.getComponent();
  }

  @Override
  @RequiredUIAccess
  public JComponent getPreferredFocusedComponent() {
    return myTemplatesForm.getComponent();
  }

  public ChooseTemplatesForm getTemplatesForm() {
    return myTemplatesForm;
  }

  public List<Template> getSelectedTemplates() {
    List<Template> templates = new LinkedList<>();
    for (TemplateInfo info : myTemplatesForm.getTemplateInfos()) {
      if (info.isAccepted()) {
        templates.add(info.getTemplate());
      }
    }
    return templates;
  }

  @Override
  protected void dispose() {
    Disposer.dispose(myTemplatesForm);
    super.dispose();
  }
}
