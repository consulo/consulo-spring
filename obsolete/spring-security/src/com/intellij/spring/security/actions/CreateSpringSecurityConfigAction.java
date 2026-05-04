package com.intellij.spring.security.actions;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.actions.CreateElementActionBase;
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
import com.intellij.spring.security.SpringSecurityBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CreateSpringSecurityConfigAction extends CreateFileAction {

  public CreateSpringSecurityConfigAction() {
    super(SpringSecurityBundle.message("config.new.file"),
          SpringSecurityBundle.message("create.new.spring.configuration.file"),
          SpringIcons.CONFIG_FILE);
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
    CreateElementActionBase.MyInputValidator validator = new CreateElementActionBase.MyInputValidator(project, directory);
    Messages.showInputDialog(project, IdeBundle.message("prompt.enter.new.file.name"),
                             IdeBundle.message("title.new.file"), Messages.getQuestionIcon(), null, validator);
    return validator.getCreatedElements();
  }

   @NotNull
  protected PsiElement[] create(final String newName, final PsiDirectory directory) throws Exception {
    final FileTemplate template = getConfigTemplate();

    @NonNls final String fileName = FileUtil.getExtension(newName).length() == 0 ? newName + ".xml" : newName;

    final PsiElement psiElement = FileTemplateUtil.createFromTemplate(template, fileName, null, directory);

    return new PsiElement[] {psiElement};
  }

  public static FileTemplate getConfigTemplate() {
    return FileTemplateManager.getInstance().getJ2eeTemplate("spring.security.config.xml");
  }
}


