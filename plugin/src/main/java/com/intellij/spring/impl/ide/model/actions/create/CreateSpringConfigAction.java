package com.intellij.spring.impl.ide.model.actions.create;

import com.intellij.java.language.psi.JavaPsiFacade;
import consulo.annotation.access.RequiredReadAction;
import consulo.dataContext.DataContext;
import consulo.fileTemplate.FileTemplate;
import consulo.fileTemplate.FileTemplateUtil;
import consulo.ide.action.CreateFileAction;
import consulo.ide.localize.IdeLocalize;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.module.Module;
import consulo.project.Project;
import consulo.spring.impl.icon.SpringImplIconGroup;
import consulo.spring.localize.SpringLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.awt.UIUtil;
import consulo.util.io.FileUtil;
import jakarta.annotation.Nonnull;

import java.util.Map;

public class CreateSpringConfigAction extends CreateFileAction {
  public CreateSpringConfigAction() {
    super(
      SpringLocalize.configNewFile(),
      SpringLocalize.createNewSpringConfigurationFile(),
      SpringImplIconGroup.springconfig()
    );
  }

  @Override
  @RequiredReadAction
  protected boolean isAvailable(DataContext dataContext) {
    if (!super.isAvailable(dataContext)) {
      return false;
    }
    consulo.module.Module module = dataContext.getData(Module.KEY);
    return module != null && JavaPsiFacade.getInstance(module.getProject()).findPackage("org.springframework") != null;
  }

  @Nonnull
  @RequiredUIAccess
  protected PsiElement[] invokeDialog(Project project, PsiDirectory directory) {
    MyInputValidator validator = new MyInputValidator(project, directory);
    Messages.showInputDialog(
      project,
      IdeLocalize.promptEnterNewFileName().get(),
      IdeLocalize.titleNewFile().get(),
      UIUtil.getQuestionIcon(),
      null,
      validator
    );
    return validator.getCreatedElements();
  }

  @Nonnull
  @Override
  @RequiredUIAccess
  protected PsiElement[] create(String newName, PsiDirectory directory) throws Exception {
    Module module = directory.getModule();
    FileTemplate template = null;///SpringFrameworkSupportProvider.chooseTemplate(module);
    String fileName = FileUtil.getExtension(newName).length() == 0 ? newName + ".xml" : newName;
    PsiElement psiElement = FileTemplateUtil.createFromTemplate(template, fileName, (Map<String, Object>) null, directory);
    return new PsiElement[]{psiElement};
  }
}
