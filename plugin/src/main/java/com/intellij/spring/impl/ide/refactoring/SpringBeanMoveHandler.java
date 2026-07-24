package com.intellij.spring.impl.ide.refactoring;

import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.model.SpringUtils;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.SpringValueHolder;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.refactoring.move.MoveHandlerDelegate;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.project.Project;
import consulo.ui.ex.awt.Messages;

import jakarta.annotation.Nullable;

/**
 * @author Dmitry Avdeev
 */
@ExtensionImpl
public class SpringBeanMoveHandler extends MoveHandlerDelegate {
  @Override
  public boolean canMove(PsiElement[] elements, @Nullable PsiElement targetContainer) {
    for (PsiElement element : elements) {
      if (SpringUtils.findBeanFromPsiElement(element) == null) return false;
    }
    return super.canMove(elements, targetContainer);
  }


  public boolean isValidTarget(PsiElement psiElement) {
    SpringBean springBean = SpringUtils.findBeanFromPsiElement(psiElement);
    return springBean != null && (springBean.getParent() instanceof Beans || springBean.getParent() instanceof SpringValueHolder);
  }

  public boolean tryToMove(PsiElement element,
                           final Project project,
                           DataContext dataContext,
                           @Nullable PsiReference reference,
                           final Editor editor) {
    final SpringBean springBean = SpringUtils.findBeanFromPsiElement(element);
    if (springBean == null) {
      return false;
    }
    final PsiFile psiFile = element.getContainingFile();
    if (springBean.getParent() instanceof Beans) {  // top level
      new SpringBeanMoveDialog(project, springBean).show();
    }
    else {
      if (Messages.showYesNoDialog(project,
                                   SpringBundle.message("do.you.want.to.move.bean.to.the.top.level"),
                                   SpringBundle.message("move.bean.to.the.top.level"),
                                   Messages.getQuestionIcon()) == 0) {
        new WriteCommandAction.Simple(project, SpringBundle.message("move.bean.to.the.top.level"), psiFile) {
          protected void run() throws Throwable {
            SpringIntroduceBeanIntention.moveToTheTopLevel(project, editor, springBean);
          }
        }.execute();
      }
    }
    return true;
  }
}
