/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.converters;

import com.intellij.java.impl.psi.impl.source.resolve.reference.impl.providers.FilePathReferenceProvider;
import com.intellij.spring.impl.ide.model.xml.beans.ListOrSet;
import com.intellij.spring.impl.ide.model.xml.beans.SpringProperty;
import com.intellij.spring.impl.ide.model.xml.beans.SpringValue;
import consulo.document.util.TextRange;
import consulo.language.content.LanguageContentFolderScopes;
import consulo.language.psi.*;
import consulo.language.psi.path.FileReference;
import consulo.language.psi.path.FileReferenceSet;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.ProjectRootManager;
import consulo.module.content.layer.ContentFolder;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.function.Condition;
import consulo.util.lang.function.PairProcessor;
import consulo.util.lang.text.StringTokenizer;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlTag;
import consulo.xml.dom.*;
import consulo.xml.util.xml.impl.ConvertContextImpl;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ResourceResolverUtils {
  @NonNls private static final String CLASSPATH_PREFIX = "classpath:";
  @NonNls private static final String CLASSPATH_PREFIX_ASTERISK = "classpath*:";
  @NonNls private static final String FILE_PREFIX = "file:";
  @NonNls private static final String HTTP_PREFIX = "http:";

  public static final Condition<PsiFileSystemItem> FILE_FILTER = new Condition<PsiFileSystemItem>() {
    public boolean value(PsiFileSystemItem item) {
      return item instanceof PsiFile;
    }
  };
  public static final Condition<PsiFileSystemItem> DIRECTORY_FILTER = new Condition<PsiFileSystemItem>() {
    public boolean value(PsiFileSystemItem item) {
      return item instanceof PsiDirectory;
    }
  };

  private static final FilePathReferenceProvider ourFilePathReferenceProvider = new FilePathReferenceProvider() {
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

  public static boolean processSpringValues(SpringProperty property, PairProcessor<GenericDomValue, String> processor) {
    {
      GenericAttributeValue<String> valueAttr = property.getValueAttr();
      XmlAttribute valueAttrElement = valueAttr.getXmlAttribute();
      String valueAttrString = valueAttr.getStringValue();
      if (valueAttrElement != null && valueAttrString != null && !processor.process(valueAttr, valueAttrString)) {
        return false;
      }
    }
    {
      SpringValue value = property.getValue();
      XmlTag valueElement = value.getXmlTag();
      String valueString = value.getStringValue();
      if (valueElement != null && valueString != null && !processor.process(value, valueString)) {
        return false;
      }
    }
    {
      ListOrSet listOrSet = property.getList();
      for (SpringValue springValue : listOrSet.getValues()) {
        XmlTag element = springValue.getXmlTag();
        String string = springValue.getStringValue();
        if (element != null && string != null && !processor.process(springValue, string)) {
          return false;
        }
      }
    }
    return true;
  }

  public static <V extends PsiFileSystemItem, T extends Collection<V>> T getResourceItems(@Nonnull SpringProperty property, final T result, final Condition<PsiFileSystemItem> filter) {
    processSpringValues(property, new PairProcessor<GenericDomValue, String>() {
      public boolean process(GenericDomValue domValue, String s) {
        Object value = domValue.getValue();
        if (value instanceof Collection) {
          for (Object o : (Collection)value) {
            if (o instanceof PsiFileSystemItem && filter.value((PsiFileSystemItem)o)) {
              result.add((V)o);
            }
          }
        }
        return true;
      }
    });
    return result;
  }

  public static <V extends PsiFileSystemItem, T extends Collection<V>> T addResourceFilesFrom(@Nonnull PsiElement element, @Nonnull String s,
                                                                                              T result, Condition<PsiFileSystemItem> filter) {
    PsiReference[] references = getReferences(element, s, false, false);
    return addResourceItems(result, references, filter);
  }

  public static <V, T extends Collection<V>> T addResourceFilesFrom(final @Nonnull PsiElement element, @Nonnull String s, String delimiter,
																	T result, Condition<PsiFileSystemItem> filter) {
    final ArrayList<PsiReference> references = new ArrayList<PsiReference>();
    final int startInElement = ElementManipulators.getOffsetInElement(element);

    processSeparatedString(s, delimiter, new PairProcessor<String, Integer>() {
      public boolean process(String s, Integer offset) {
        PsiReference[] psiReferences = getReferences(element, s, false, false, offset.intValue() + startInElement, true);
        references.addAll(Arrays.asList(psiReferences));
        return true;
      }
    });

    return addResourceItems(result, references.toArray(new PsiReference[references.size()]) , filter);
  }

  public static <V extends PsiFileSystemItem, T extends Collection<V>> T addResourceFilesFrom(@Nonnull GenericDomValue element, @Nonnull String s,
                                                                                              T result,
                                                                                              Condition<PsiFileSystemItem> filter) {
    Converter converter = WrappingConverter.getDeepestConverter(element.getConverter(), element);
    if (converter instanceof CustomReferenceConverter) {
      PsiReference[] references = ((CustomReferenceConverter)converter).createReferences(element, element.getXmlElement(),
                                                                                               new ConvertContextImpl(element));
      return addResourceItems(result, references, filter);
    }
    return result;
  }

  private static <V, T extends Collection<V>> T addResourceItems(T result, PsiReference[] references, Condition<PsiFileSystemItem> filter) {
    for (PsiReference reference : references) {
      if (reference instanceof PsiPolyVariantReference) {
        ResolveResult[] resolveResults = ((PsiPolyVariantReference)reference).multiResolve(false);
        for (ResolveResult resolveResult : resolveResults) {
          PsiElement psiElement = resolveResult.getElement();
          if (psiElement instanceof PsiFileSystemItem && filter.value((PsiFileSystemItem)psiElement)) {
            result.add((V)psiElement);
          }
        }
      }
      else {
        PsiElement psiElement = reference.resolve();
        if (psiElement instanceof PsiFileSystemItem && filter.value((PsiFileSystemItem)psiElement)) {
          result.add((V)psiElement);
        }
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

  public static boolean processSeparatedString(String str, String delimiter, PairProcessor<String, Integer> processor) {
    if (str == null || StringUtil.isEmptyOrSpaces(str)) return true;

    StringTokenizer tokenizer = new StringTokenizer(str, delimiter);
    while (tokenizer.hasMoreElements()) {
      @NonNls String s = tokenizer.nextElement().trim();
      if (s.length() == 0) continue;
      if (!processor.process(s, str.indexOf(s))) return false;
    }

    return true;
  }

  @Nullable
  public static String getResourceFileReferenceString(PsiFile resourceFile) {
    VirtualFile virtualFile = resourceFile == null ? null : resourceFile.getVirtualFile();
    if (virtualFile == null) return null;

    Module moduleForFile = ModuleUtilCore.findModuleForFile(resourceFile);
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
