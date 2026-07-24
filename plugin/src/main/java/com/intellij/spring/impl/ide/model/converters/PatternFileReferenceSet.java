/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.spring.impl.ide.SpringBundle;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementResolveResult;
import consulo.language.psi.PsiFileSystemItem;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.path.FileReference;
import consulo.language.psi.path.FileReferenceSet;
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

  public FileReference createFileReference(TextRange range, int index, String text) {
    if (!isAntPattern(text)) return super.createFileReference(range, index, text);

    return new PatternFileReference(this, range, index, text);
  }

  // @see org.springframework.util.AntPathMatcher#isPattern
  static boolean isAntPattern(String str) {
    return (str.indexOf('*') != -1 || str.indexOf('?') != -1);
  }

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

    protected void innerResolveInContext(@Nonnull String text,
										 @Nonnull PsiFileSystemItem context,
										 Collection<ResolveResult> result, boolean caseSensitive) {


      if (text.equals("**")) {
        addDirectoryResolves(context, result);
      }
      else {
        String patternText = FileUtil.convertAntToRegexp(text);
        Pattern pattern = Pattern.compile(patternText);

        PsiElement[] psiElements = context.getChildren();
        for (PsiElement psiElement : psiElements) {
          if (psiElement instanceof PsiFileSystemItem) {
            if (pattern.matcher(((PsiFileSystemItem)psiElement).getName()).matches()) {
              result.add(new PsiElementResolveResult(psiElement));
            }
          }
        }
      }
    }

    private static void addDirectoryResolves(PsiElement context, Collection<ResolveResult> result) {
      if (context instanceof PsiFileSystemItem && ((PsiFileSystemItem)context).isDirectory()) {
        result.add(new PsiElementResolveResult(context));
        for (PsiElement psiElement : context.getChildren()) {
           addDirectoryResolves(psiElement, result);
        }
      }
    }

    public String getUnresolvedMessagePattern() {
      return SpringBundle.message("spring.resource.ant.style.reference.error.message");
    }
  }
}
