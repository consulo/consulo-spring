package com.intellij.spring.web.mvc;

import com.intellij.openapi.module.Module;
import consulo.ide.impl.idea.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.jsp.JspImplicitVariableImpl;
import com.intellij.psi.impl.source.jsp.el.impl.ELElementProcessor;
import com.intellij.psi.impl.source.jsp.el.impl.ElVariablesProvider;
import com.intellij.psi.jsp.el.ELExpressionHolder;
import com.intellij.spring.web.mvc.jam.SpringMVCJamModel;
import com.intellij.spring.web.mvc.jam.SpringMVCModelAttribute;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Dmitry Avdeev
 */
public class SpringMVCVariablesProvider extends ElVariablesProvider {

  public boolean processImplicitVariables(@NotNull final PsiElement element, @NotNull final ELExpressionHolder containingFile, @NotNull final ELElementProcessor processor) {
    final Module module = ModuleUtil.findModuleForPsiElement(containingFile);
    if (module != null) {
      final Collection<SpringMVCModelAttribute> attributes = SpringMVCJamModel.getModel(module).getModelAttributes();
      for (SpringMVCModelAttribute attribute : attributes) {
        final PsiType type = attribute.getType();
        if (type == null) {
          continue;
        }
        final String name = attribute.getName();
        if (name != null) {
          processor.processVariable(new JspImplicitVariableImpl(containingFile, name, type, attribute.getAnnotation(), JspImplicitVariableImpl.NESTED_RANGE));
        }
      }
    }
    return true;
  }

}
