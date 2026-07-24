/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.*;
import consulo.language.psi.PsiManager;
import jakarta.annotation.Nonnull;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author peter
 */
public class PsiClassTypePattern extends AopPsiTypePattern {
  private final String myText;
  private final Pattern myPattern;
  private final String myRegex;

  public PsiClassTypePattern(String pattern) {
    assert !"*".equals(pattern);
    myText = pattern;
    myRegex = "*".equals(pattern) ? ".*" : pattern.
      replaceAll("([\\[\\]\\^\\(\\)\\{\\}\\-])", "\\\\$1").
      replaceAll("\\*", "\\[\\^\\\\.]\\*").
      replaceAll("\\.", "\\\\.").
      replaceAll("\\\\.\\\\.", "\\(\\\\.\\|\\\\.\\.\\*\\\\.\\)");
    myPattern = Pattern.compile(myRegex);
  }

  public String getText() {
    return myText;
  }

  public String getRegex() {
    return myRegex;
  }

  @Override
  public String toString() {
    return "PsiClassTypePattern:" + myText;
  }

  @Override
  public boolean accepts(@Nonnull String qualifiedName) {
    return myPattern.matcher(qualifiedName).matches();
  }

  @Override
  public boolean processPackages(PsiManager manager, Predicate<PsiJavaPackage> processor) {
    int asterisk = myText.indexOf('*');
    if (asterisk < 0) asterisk = Integer.MAX_VALUE;

    int i = 0;
    while (true) {
      String prefix = myText.substring(0, i);
      PsiJavaPackage psiPackage = JavaPsiFacade.getInstance(manager.getProject()).findPackage(prefix);
      if (psiPackage == null) return true;

      int i1 = myText.indexOf('.', i + 1);
      if (i1 == i + 1) return processSubPackages(psiPackage, processor);

      if (!processor.test(psiPackage)) return false;

      if (i1 > asterisk) return true;
      if (i == myText.length()) return true;

      i = i1 < 0 ? myText.length() : i1;
    }
  }

  @Override
  public boolean accepts(@Nonnull PsiType type) {
    if (type instanceof PsiClassType) {
      PsiClassType classType = (PsiClassType)type;
      PsiClass psiClass = classType.resolve();
      if (psiClass != null) {
        String qName = psiClass.getQualifiedName();
        if (qName != null && accepts(qName)) {
          return true;
        }
      }
    }
    return false;
  }
}
