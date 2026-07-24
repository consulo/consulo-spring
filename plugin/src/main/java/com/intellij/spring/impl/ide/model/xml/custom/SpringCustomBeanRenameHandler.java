package com.intellij.spring.impl.ide.model.xml.custom;

import com.intellij.spring.impl.ide.SpringBundle;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.language.editor.LangDataKeys;
import consulo.language.editor.refactoring.rename.PsiElementRenameHandler;
import consulo.language.editor.refactoring.rename.RenameHandler;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.Messages;

import consulo.xml.language.psi.XmlAttribute;
import jakarta.annotation.Nonnull;

@ExtensionImpl
public class SpringCustomBeanRenameHandler implements RenameHandler {
    @Nonnull
    @Override
    public LocalizeValue getActionTitleValue() {
        return LocalizeValue.localizeTODO("Spring Bean Rename...");
    }

    @Override
    public boolean isAvailableOnDataContext(DataContext dataContext) {
        return false;
    }

    @Override
    public boolean isRenaming(DataContext dataContext) {
        PsiElement element = dataContext.getData(LangDataKeys.PSI_ELEMENT);
        return element instanceof CustomBeanFakePsiElement;
    }

    @RequiredUIAccess
    @Override
    public void invoke(@Nonnull Project project, Editor editor, PsiFile file, DataContext dataContext) {
        PsiElement element = dataContext.getData(LangDataKeys.PSI_ELEMENT);
        doInvoke(project, editor, element);
    }

    private static void doInvoke(Project project, Editor editor, PsiElement element) {
        XmlAttribute idAttribute = ((CustomBeanFakePsiElement) element).getBean().getIdAttribute();
        if (idAttribute == null) {
            int i = Messages
                .showOkCancelDialog(project,
                    SpringBundle.message("custom.bean.no.id"),
                    SpringBundle.message("custom.bean.no.id.title"),
                    Messages.getWarningIcon());
            if (i != 0) {
                return;
            }
        }

        PsiElementRenameHandler.invoke(element, project, element, editor);
    }

    @RequiredUIAccess
    @Override
    public void invoke(@Nonnull Project project, @Nonnull PsiElement[] elements, DataContext dataContext) {
    }

}