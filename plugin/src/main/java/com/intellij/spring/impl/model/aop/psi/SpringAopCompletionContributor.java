/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring.impl.model.aop.psi;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.aop.psi.AopCompletionData;
import com.intellij.aop.psi.AopPointcutExpressionFile;
import com.intellij.aop.psi.AopPointcutExpressionLanguage;
import com.intellij.spring.impl.ide.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.impl.ide.model.SpringUtils;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.util.ModuleUtilCore;
import consulo.language.util.ProcessingContext;
import consulo.module.Module;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import java.util.Set;

/**
 * @author peter
 */
@ExtensionImpl(order = "before aop")
public class SpringAopCompletionContributor extends CompletionContributor {
  public static final
  @NonNls
  String[] SPRING20_AOP_POINTCUTS = {"execution", "target", "this", "within", "@target", "@within", "@annotation", "args", "@args"};

  public SpringAopCompletionContributor() {
    extend(CompletionType.BASIC, AopCompletionData.POINTCUT_PATTERN, new CompletionProvider() {
      public void addCompletions(@Nonnull CompletionParameters parameters,
                                 ProcessingContext context,
                                 @Nonnull CompletionResultSet result) {
        AopPointcutExpressionFile file = (AopPointcutExpressionFile)parameters.getPosition().getContainingFile();
        AopAdvisedElementsSearcher searcher = file.getAopModel().getAdvisedElementsSearcher();
        boolean isSpring = searcher instanceof SpringAdvisedElementsSearcher;
        if (isSpring) {
          for (String pointcut : SPRING20_AOP_POINTCUTS) {
            result.addElement(AopCompletionData.createPointcutDesignatorElement(pointcut));
          }

          Module module = ModuleUtilCore.findModuleForPsiElement(parameters.getPosition());
          if (module != null && SpringUtils.isSpring25(module)) {
            result.addElement(AopCompletionData.createPointcutDesignatorElement("bean"));
          }
        }

        Set<String> designators = AopCompletionData.getAllPointcutDesignators();
        result.runRemainingContributors(parameters, r -> {
          LookupElement lookupElement = r.getLookupElement();
          if (!isSpring || !designators.contains(lookupElement.getLookupString())) {
            result.addElement(lookupElement);
          }
        });
      }
    });
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return AopPointcutExpressionLanguage.INSTANCE;
  }
}
