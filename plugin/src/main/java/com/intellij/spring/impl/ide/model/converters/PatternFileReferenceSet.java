/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.converters;

import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementResolveResult;
import consulo.language.psi.PsiFileSystemItem;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.path.FileReference;
import consulo.language.psi.path.FileReferenceSet;
import consulo.spring.localize.SpringLocalize;
import consulo.util.io.FileUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * @author Dmitry Avdeev
*/
public class PatternFileReferenceSet extends FileReferenceSet
{
  public PatternFileReferenceSet(String str, PsiElement element, int offset) {
    super(str, element, offset, null, true);
  }

  @Override
  public FileReference createFileReference(TextRange range, int index, String text) {
    if (!isAntPattern(text)) return super.createFileReference(range, index, text);

    return new PatternFileReference(this, range, index, text);
  }

  // @see org.springframework.util.AntPathMatcher#isPattern
  static boolean isAntPattern(String str) {
    return (str.indexOf('*') != -1 || str.indexOf('?') != -1);
  }

  @Override
  protected boolean isSoft() {
    return true;
  }

  /**
 * @author Dmitry Avdeev
  */
  public static class PatternFileReference extends FileReference
  {
    public PatternFileReference(FileReferenceSet referenceSet, TextRange range, int index, String text) {
      super(referenceSet, range, index, text);
    }

    @Override
    @RequiredReadAction
    protected void innerResolveInContext(
      @Nonnull String text,
      @Nonnull PsiFileSystemItem context,
      Collection<ResolveResult> result,
      boolean caseSensitive
    ) {
      if (text.equals("**")) {
        addDirectoryResolves(context, result);
      }
      else {
        String patternText = FileUtil.convertAntToRegexp(text);
        Pattern pattern = Pattern.compile(patternText);

        PsiElement[] psiElements = context.getChildren();
        for (PsiElement psiElement : psiElements) {
          if (psiElement instanceof PsiFileSystemItem fsItem && pattern.matcher(fsItem.getName()).matches()) {
            result.add(new PsiElementResolveResult(fsItem));
          }
        }
      }
    }

    @RequiredReadAction
    private static void addDirectoryResolves(PsiElement context, Collection<ResolveResult> result) {
      if (context instanceof PsiFileSystemItem fsItem && fsItem.isDirectory()) {
        result.add(new PsiElementResolveResult(context));
        for (PsiElement psiElement : context.getChildren()) {
           addDirectoryResolves(psiElement, result);
        }
      }
    }

    public String getUnresolvedMessagePattern() {
      return SpringLocalize.springResourceAntStyleReferenceErrorMessage().get();
    }
  }
}
