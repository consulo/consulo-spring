package com.intellij.spring.impl.ide.model.actions.create;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.SpringIcons;
import consulo.dataContext.DataContext;
import consulo.fileTemplate.FileTemplate;
import consulo.fileTemplate.FileTemplateUtil;
import consulo.ide.IdeBundle;
import consulo.ide.action.CreateFileAction;
import consulo.language.editor.LangDataKeys;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.ex.awt.Messages;
import consulo.util.io.FileUtil;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import java.util.Map;

public class CreateSpringConfigAction extends CreateFileAction {

  public CreateSpringConfigAction() {
    super(SpringBundle.message("config.new.file"),
        SpringBundle.message("create.new.spring.configuration.file"),
        SpringIcons.CONFIG_FILE);
  }

  protected boolean isAvailable(DataContext dataContext) {
    if (!super.isAvailable(dataContext)) {
      return false;
    }
    consulo.module.Module module = dataContext.getData(LangDataKeys.MODULE);
    return module != null && JavaPsiFacade.getInstance(module.getProject()).findPackage("org.springframework") != null;
  }

  @Nonnull
  protected PsiElement[] invokeDialog(Project project, PsiDirectory directory) {
    MyInputValidator validator = new MyInputValidator(project, directory);
    Messages.showInputDialog(project, IdeBundle.message("prompt.enter.new.file.name"),
        IdeBundle.message("title.new.file"), Messages.getQuestionIcon(), null, validator);
    return validator.getCreatedElements();
  }

  @Nonnull
  protected PsiElement[] create(String newName, PsiDirectory directory) throws Exception {
    Module module = ModuleUtilCore.findModuleForPsiElement(directory);
    FileTemplate template = null;///SpringFrameworkSupportProvider.chooseTemplate(module);
    @NonNls String fileName = FileUtil.getExtension(newName).length() == 0 ? newName + ".xml" : newName;
    PsiElement psiElement = FileTemplateUtil.createFromTemplate(template, fileName, (Map<String, Object>) null, directory);
    return new PsiElement[]{psiElement};
  }
}
