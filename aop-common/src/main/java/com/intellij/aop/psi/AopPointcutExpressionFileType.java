/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author peter
 */
public class AopPointcutExpressionFileType extends LanguageFileType {
  public static final AopPointcutExpressionFileType INSTANCE = new AopPointcutExpressionFileType();
  
  private AopPointcutExpressionFileType() {
    super(AopPointcutExpressionLanguage.INSTANCE);
  }

  @Nonnull
  public   @Override
  String getId() {
    return "Pointcut Expression";
  }

  @Nonnull
  @Override
  public LocalizeValue getDescription() {
    return LocalizeValue.localizeTODO("Pointcut Expression");
  }

  @Nonnull
  @Override
  public String getDefaultExtension() {
    return "pointcutExpression";
  }

  @Nullable
  @Override
  public Image getIcon() {
    return null;
  }
}
