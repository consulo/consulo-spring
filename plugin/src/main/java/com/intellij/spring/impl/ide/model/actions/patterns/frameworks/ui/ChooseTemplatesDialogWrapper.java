package com.intellij.spring.impl.ide.model.actions.patterns.frameworks.ui;

import consulo.language.editor.template.Template;
import consulo.java.ex.facet.LibrariesValidationComponent;
import consulo.project.Project;
import consulo.ui.ex.awt.DialogWrapper;
import consulo.disposer.Disposer;
import com.intellij.spring.impl.ide.SpringBundle;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

public class ChooseTemplatesDialogWrapper extends DialogWrapper {
  private final ChooseTemplatesForm myTemplatesForm;

  public ChooseTemplatesDialogWrapper(Project project, List<TemplateInfo> infos, LibrariesInfo libInfo, String frameworkTitle) {
    super(project, true);

    myTemplatesForm = new ChooseTemplatesForm(infos, libInfo);
    myTemplatesForm.getLibrariesValidationComponent().addValidityListener(new LibrariesValidationComponent.ValidityListener() {
      public void valididyChanged(boolean isValid) {
        setOKActionEnabled(isValid);
      }
    });
    setOKActionEnabled(myTemplatesForm.getLibrariesValidationComponent().isValid());
    setTitle(SpringBundle.message("spring.choose.bean.templates.dialog.title", frameworkTitle));

    init();
  }

  protected Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

  protected JComponent createCenterPanel() {
    return myTemplatesForm.getComponent();
  }

  public JComponent getPreferredFocusedComponent() {
    return myTemplatesForm.getComponent();
  }

  public ChooseTemplatesForm getTemplatesForm() {
    return myTemplatesForm;
  }

  public List<Template> getSelectedTemplates() {
    List<Template> templates = new LinkedList<Template>();
    for (TemplateInfo info : myTemplatesForm.getTemplateInfos()) {
      if (info.isAccepted()) {
        templates.add(info.getTemplate());
      }
    }
    return templates;
  }

  protected void dispose() {
    Disposer.dispose(myTemplatesForm);
    super.dispose();
  }


}
