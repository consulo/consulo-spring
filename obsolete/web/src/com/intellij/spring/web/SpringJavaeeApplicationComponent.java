/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.web;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameInputValidator;
import com.intellij.refactoring.rename.RenameInputValidatorRegistry;
import com.intellij.spring.model.actions.patterns.dataAccess.GenerateDataAccessPatternsGroup;
import com.intellij.spring.model.actions.patterns.dataAccess.JpaActionGroup;
import com.intellij.spring.model.actions.patterns.dataAccess.JpaPatternAction;
import com.intellij.spring.web.mvc.jam.SpringMVCRequestMapping;
import consulo.ide.impl.idea.util.NullableFunction;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.ElementPresentationManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SpringJavaeeApplicationComponent implements ApplicationComponent {
  @NonNls
  @NotNull
  public String getComponentName() {
    return "spring.javaee.application.component";
  }

  public void initComponent() {
    addCustomActions();
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        return o instanceof SpringMVCRequestMapping ? ((SpringMVCRequestMapping)o).getName() : null;
      }
    });

    RenameInputValidatorRegistry.getInstance().registerInputValidator(PsiJavaPatterns.psiAnnotation().qName(SpringMVCRequestMapping.REQUEST_MAPPING), new RenameInputValidator() {
      public boolean isInputValid(final String newName, final PsiElement element, final ProcessingContext context) {
        return true;
      }
    });
  }

  private static void addCustomActions() {
    final DefaultActionGroup group = (DefaultActionGroup)ActionManager.getInstance().getAction("Spring.Template.Beans.ActionGroup");
    final AnAction[] actions = group.getChildren(null);
    for (AnAction action : actions) {
      if (action instanceof GenerateDataAccessPatternsGroup) {
        try {
          ((DefaultActionGroup)action).addAll(new JpaActionGroup());
        } catch (Exception e) {
        }
        break;
      }
    }
    final DefaultActionGroup patternsGroup = (DefaultActionGroup)ActionManager.getInstance().getAction("Spring.Patterns.ActionGroup");
    if (patternsGroup != null) {
      patternsGroup.addSeparator();
      patternsGroup.add(new JpaPatternAction());
    }
  }

  public void disposeComponent() {
  }
}
