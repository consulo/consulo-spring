package com.intellij.spring.webflow.actions;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.webflow.resources.messages.WebflowBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


public class CreateWebflowFileAction extends CreateFileAction {

  public CreateWebflowFileAction() {
    super(WebflowBundle.message("config.new.file"),
          WebflowBundle.message("create.new.spring.configuration.file"),
          SpringIcons.SPRING_ICON);
  }

  protected boolean isAvailable(final DataContext dataContext) {
    if (!super.isAvailable(dataContext)) {
      return false;
    }
    final Module module = LangDataKeys.MODULE.getData(dataContext);
    return module != null && SpringFacet.getInstance(module) != null;
  }

  @NotNull
  protected PsiElement[] invokeDialog(final Project project, PsiDirectory directory) {
    MyInputValidator validator = new MyInputValidator(project, directory);
    Messages.showInputDialog(project, IdeBundle.message("prompt.enter.new.file.name"),
                             IdeBundle.message("title.new.file"), Messages.getQuestionIcon(), null, validator);
    return validator.getCreatedElements();
  }

  @NotNull
  protected PsiElement[] create(final String newName, final PsiDirectory directory) throws Exception {
    final FileTemplate template = getWebflowTemplate();

    @NonNls final String fileName = FileUtil.getExtension(newName).length() == 0 ? newName + ".xml" : newName;

    final PsiElement psiElement = FileTemplateUtil.createFromTemplate(template, fileName, null, directory);

    return new PsiElement[] {psiElement};
  }

  public static FileTemplate getWebflowTemplate() {
    return FileTemplateManager.getInstance().getJ2eeTemplate("webflow.2_0.xml");
  }
}

