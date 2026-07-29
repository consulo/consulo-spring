/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.impl.psi.impl.source.resolve.reference.impl.providers.FilePathReferenceProvider;
import com.intellij.spring.impl.ide.model.xml.beans.ListOrSet;
import com.intellij.spring.impl.ide.model.xml.beans.SpringProperty;
import com.intellij.spring.impl.ide.model.xml.beans.SpringValue;
import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.language.content.LanguageContentFolderScopes;
import consulo.language.psi.*;
import consulo.language.psi.path.FileReference;
import consulo.language.psi.path.FileReferenceSet;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.ProjectRootManager;
import consulo.module.content.layer.ContentFolder;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.text.StringTokenizer;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.xml.dom.*;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.util.xml.impl.ConvertContextImpl;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class ResourceResolverUtils {
  private static final String CLASSPATH_PREFIX = "classpath:";
  private static final String CLASSPATH_PREFIX_ASTERISK = "classpath*:";
  private static final String FILE_PREFIX = "file:";
  private static final String HTTP_PREFIX = "http:";

  public static final Predicate<PsiFileSystemItem> FILE_FILTER = item -> item instanceof PsiFile;
  public static final Predicate<PsiFileSystemItem> DIRECTORY_FILTER = item -> item instanceof PsiDirectory;

  private static final FilePathReferenceProvider ourFilePathReferenceProvider = new FilePathReferenceProvider() {
    @Override
    protected FileReference createFileReference(FileReferenceSet referenceSet,
                                                TextRange range,
                                                int index,
                                                String text) {
      if (!PatternFileReferenceSet.isAntPattern(text)) return new FileReference(referenceSet, range, index, text);

      return new PatternFileReferenceSet.PatternFileReference(referenceSet, range, index, text);
    }
  };

  private ResourceResolverUtils() {
  }

  public static boolean processSpringValues(SpringProperty property, BiPredicate<GenericDomValue, String> processor) {
    {
      GenericAttributeValue<String> valueAttr = property.getValueAttr();
      XmlAttribute valueAttrElement = valueAttr.getXmlAttribute();
      String valueAttrString = valueAttr.getStringValue();
      if (valueAttrElement != null && valueAttrString != null && !processor.test(valueAttr, valueAttrString)) {
        return false;
      }
    }
    {
      SpringValue value = property.getValue();
      XmlTag valueElement = value.getXmlTag();
      String valueString = value.getStringValue();
      if (valueElement != null && valueString != null && !processor.test(value, valueString)) {
        return false;
      }
    }
    {
      ListOrSet listOrSet = property.getList();
      for (SpringValue springValue : listOrSet.getValues()) {
        XmlTag element = springValue.getXmlTag();
        String string = springValue.getStringValue();
        if (element != null && string != null && !processor.test(springValue, string)) {
          return false;
        }
      }
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  public static <V extends PsiFileSystemItem, T extends Collection<V>> T getResourceItems(@Nonnull SpringProperty property, T result, Predicate<PsiFileSystemItem> filter) {
    processSpringValues(property, (domValue, s) -> {
      if (domValue.getValue() instanceof Collection collection) {
        for (Object o : collection) {
          if (o instanceof PsiFileSystemItem && filter.test((PsiFileSystemItem)o)) {
            result.add((V)o);
          }
        }
      }
      return true;
    });
    return result;
  }

  @RequiredReadAction
  public static <V extends PsiFileSystemItem, T extends Collection<V>> T addResourceFilesFrom(@Nonnull PsiElement element, @Nonnull String s,
                                                                                              T result, Predicate<PsiFileSystemItem> filter) {
    PsiReference[] references = getReferences(element, s, false, false);
    return addResourceItems(result, references, filter);
  }

  @RequiredReadAction
  public static <V, T extends Collection<V>> T addResourceFilesFrom(
    @Nonnull PsiElement element,
    @Nonnull String s,
    String delimiter,
    T result,
    Predicate<PsiFileSystemItem> filter
  ) {
    List<PsiReference> references = new ArrayList<>();
    int startInElement = ElementManipulators.getOffsetInElement(element);

    processSeparatedString(s, delimiter, (s1, offset) -> {
      PsiReference[] psiReferences = getReferences(element, s1, false, false, offset + startInElement, true);
      references.addAll(Arrays.asList(psiReferences));
      return true;
    });

    return addResourceItems(result, references.toArray(new PsiReference[references.size()]) , filter);
  }

  @RequiredReadAction
  public static <V extends PsiFileSystemItem, T extends Collection<V>> T addResourceFilesFrom(@Nonnull GenericDomValue element, @Nonnull String s,
                                                                                              T result,
                                                                                              Predicate<PsiFileSystemItem> filter) {
    Converter converter = WrappingConverter.getDeepestConverter(element.getConverter(), element);
    if (converter instanceof CustomReferenceConverter customRefConverter) {
      PsiReference[] references = customRefConverter.createReferences(element, element.getXmlElement(), new ConvertContextImpl(element));
      return addResourceItems(result, references, filter);
    }
    return result;
  }

  @RequiredReadAction
  @SuppressWarnings("unchecked")
  private static <V, T extends Collection<V>> T addResourceItems(T result, PsiReference[] references, Predicate<PsiFileSystemItem> filter) {
    for (PsiReference reference : references) {
      if (reference instanceof PsiPolyVariantReference polyVariantRef) {
        for (ResolveResult resolveResult : polyVariantRef.multiResolve(false)) {
          if (resolveResult.getElement() instanceof PsiFileSystemItem fsItem && filter.test(fsItem)) {
            result.add((V) fsItem);
          }
        }
      }
      else if (reference.resolve() instanceof PsiFileSystemItem fsItem && filter.test(fsItem)) {
        result.add((V) fsItem);
      }
    }
    return result;
  }

  public static PsiReference[] getReferences(@Nonnull PsiElement element,
                                             @Nullable String s,
                                             boolean fromRoot,
                                             boolean fromCurrent) {
    int offset = ElementManipulators.getOffsetInElement(element);
    return getReferences(element, s, fromRoot, fromCurrent, offset);
  }

  public static PsiReference[] getReferences(@Nonnull PsiElement element,
                                             @Nullable String s,
                                             boolean fromRoot,
                                             boolean fromCurrent,
                                             int offset) {

    return getReferences(element, s, fromRoot, fromCurrent, offset, true);
  }

  public static PsiReference[] getReferences(final @Nonnull PsiElement element,
																  final @Nullable String s,
																  boolean fromRoot,
																  boolean fromCurrent,
																  final int offset,
																  final boolean soft) {

    if (s == null || StringUtil.isEmptyOrSpaces(s) || s.startsWith(HTTP_PREFIX)) return PsiReference.EMPTY_ARRAY;

    FileReferenceSet set;
    if (s.startsWith(CLASSPATH_PREFIX)) {
      return getClassPathReferences(element, s.substring(CLASSPATH_PREFIX.length()), CLASSPATH_PREFIX.length() + offset, soft);
    } else if (s.startsWith(CLASSPATH_PREFIX_ASTERISK)) {
      return getClassPathReferences(element, s.substring(CLASSPATH_PREFIX_ASTERISK.length()), CLASSPATH_PREFIX_ASTERISK.length() + offset,
                                    soft);
    }  else if (s.startsWith(FILE_PREFIX)) {
      final String str = s.substring(FILE_PREFIX.length());
      if (str.startsWith("/") || new File(str).isAbsolute()) {
        return PsiReference.EMPTY_ARRAY;
      }

      set = new PatternFileReferenceSet(str, element, FILE_PREFIX.length() + offset) {
        @Override
        protected boolean isSoft() {
          return soft;
        }
      };

      set.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, FileReferenceSet.ABSOLUTE_TOP_LEVEL);
    }
    else {
      set = new PatternFileReferenceSet(s, element, offset) {
        @Override
        protected boolean isSoft() {
          return soft;
        }
      };
      if (fromCurrent) {
        set.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION,
                             file -> ContainerUtil.<PsiFileSystemItem>createMaybeSingletonList(file.getContainingDirectory()));
      }
      else if (fromRoot) {
        set.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, FileReferenceSet.ABSOLUTE_TOP_LEVEL);
      }
    }
    return set.getAllReferences();
  }

  public static PsiReference[] getClassPathReferences(PsiElement element, String s, int offset, boolean soft) {
    return ourFilePathReferenceProvider.getReferencesByElement(element, s, offset, soft);
  }

  public static boolean processSeparatedString(String str, String delimiter, BiPredicate<String, Integer> processor) {
    if (str == null || StringUtil.isEmptyOrSpaces(str)) return true;

    StringTokenizer tokenizer = new StringTokenizer(str, delimiter);
    while (tokenizer.hasMoreElements()) {
      String s = tokenizer.nextElement().trim();
      if (s.length() == 0) continue;
      if (!processor.test(s, str.indexOf(s))) return false;
    }

    return true;
  }

  @Nullable
  @RequiredReadAction
  public static String getResourceFileReferenceString(PsiFile resourceFile) {
    VirtualFile virtualFile = resourceFile == null ? null : resourceFile.getVirtualFile();
    if (virtualFile == null) return null;

    Module moduleForFile = resourceFile.getModule();
    if (moduleForFile != null) {
      for (ContentFolder folder : ModuleRootManager.getInstance(moduleForFile)
                                                   .getContentFolders(LanguageContentFolderScopes.production())) {
        VirtualFile file = folder.getFile();
        if (file == null) {
          continue;
        }

        if (VirtualFileUtil.isAncestor(file, virtualFile, false)) {
          return CLASSPATH_PREFIX + VirtualFileUtil.getRelativePath(virtualFile, file, '/');
        }
      }
    }

    ProjectFileIndex index = ProjectRootManager.getInstance(resourceFile.getProject()).getFileIndex();
    VirtualFile contentRoot = index.getContentRootForFile(virtualFile);
    if (contentRoot != null) {
      return FILE_PREFIX + VirtualFileUtil.getRelativePath(virtualFile, contentRoot, '/');
    }
    else {
      return FILE_PREFIX + virtualFile.getPath();
    }
  }
}
