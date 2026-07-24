/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.java.ex.jsp;

import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.OrderEnumerator;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.archive.ArchiveFileSystem;
import consulo.virtualFileSystem.archive.ArchiveVfsUtil;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nullable;
import java.util.function.Consumer;

/**
 * @author peter
 */
public abstract class JspSpiUtil {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.jsp.tagLibrary.JspTagInfoImpl");
  @NonNls private static final String JAR_EXTENSION = "jar";

  /*@Nullable
  private static JspSpiUtil getJspSpiUtil() {
    return ServiceManager.getService(JspSpiUtil.class);
  }

  public static int escapeCharsInJspContext(JspFile file, int offset, String toEscape) throws IncorrectOperationException {
    final JspSpiUtil util = getJspSpiUtil();
    return util != null ? util._escapeCharsInJspContext(file, offset, toEscape) : 0;
  }

  protected abstract int _escapeCharsInJspContext(JspFile file, int offset, String toEscape) throws IncorrectOperationException;

  public static void visitAllIncludedFilesRecursively(BaseJspFile jspFile, Processor<BaseJspFile> visitor) {
    final JspSpiUtil util = getJspSpiUtil();
    if (util != null) {
      util._visitAllIncludedFilesRecursively(jspFile, visitor);
    }
  }

  protected abstract void _visitAllIncludedFilesRecursively(BaseJspFile jspFile, Processor<BaseJspFile> visitor);

  @Nullable
  public static PsiElement resolveMethodPropertyReference(@NotNull PsiReference reference, @Nullable PsiClass resolvedClass, boolean readable) {
    final JspSpiUtil util = getJspSpiUtil();
    return util == null ? null : util._resolveMethodPropertyReference(reference, resolvedClass, readable);
  }

  @Nullable
  protected abstract PsiElement _resolveMethodPropertyReference(@NotNull PsiReference reference, @Nullable PsiClass resolvedClass, boolean readable);

  @NotNull
  public static Object[] getMethodPropertyReferenceVariants(@NotNull PsiReference reference, @Nullable PsiClass resolvedClass, boolean readable) {
    final JspSpiUtil util = getJspSpiUtil();
    return util == null ? ArrayUtil.EMPTY_OBJECT_ARRAY : util._getMethodPropertyReferenceVariants(reference, resolvedClass, readable);
  }

  protected abstract Object[] _getMethodPropertyReferenceVariants(@NotNull PsiReference reference, @Nullable PsiClass resolvedClass, boolean readable);

  public static boolean isIncludedOrIncludesSomething(@NotNull JspFile file) {
    return isIncludingAnything(file) || isIncluded(file);
  }

  public static boolean isIncluded(@NotNull JspFile jspFile) {
    final JspSpiUtil util = getJspSpiUtil();
    return util != null && util._isIncluded(jspFile);
  }

  public abstract boolean _isIncluded(@NotNull final JspFile jspFile);

  public static boolean isIncludingAnything(@NotNull JspFile jspFile) {
    final JspSpiUtil util = getJspSpiUtil();
    return util != null && util._isIncludingAnything(jspFile);
  }

  protected abstract boolean _isIncludingAnything(@NotNull final JspFile jspFile);

  public static PsiFile[] getIncludedFiles(@NotNull JspFile jspFile) {
    final JspSpiUtil util = getJspSpiUtil();
    return util == null ? PsiFile.EMPTY_ARRAY : util._getIncludedFiles(jspFile);
  }

  public static PsiFile[] getIncludingFiles(@NotNull JspFile jspFile) {
    final JspSpiUtil util = getJspSpiUtil();
    return util == null ? PsiFile.EMPTY_ARRAY : util._getIncludingFiles(jspFile);
  }

  protected abstract PsiFile[] _getIncludingFiles(@NotNull PsiFile file);

  @NotNull
  protected abstract PsiFile[] _getIncludedFiles(@NotNull final JspFile jspFile);

  public static boolean isJavaContext(PsiElement position) {
    if(PsiTreeUtil.getContextOfType(position, JspClass.class, false) != null) return true;
    return false;
  }*/

  public static boolean isJarFile(@Nullable VirtualFile file) {
    if (file != null){
      String ext = file.getExtension();
      if(ext != null && ext.equalsIgnoreCase(JAR_EXTENSION)) {
        return true;
      }
    }

    return false;
  }

  /*public static List<URL> buildUrls(@Nullable final VirtualFile virtualFile, @Nullable final Module module) {
    return buildUrls(virtualFile, module, true);
  }

  public static List<URL> buildUrls(@Nullable final VirtualFile virtualFile, @Nullable final Module module, boolean includeModuleOutput) {
    final List<URL> urls = new ArrayList<>();
    processClassPathItems(virtualFile, module, file -> addUrl(urls, file), includeModuleOutput);
    return urls;
  } */

  public static void processClassPathItems(VirtualFile virtualFile, Module module, Consumer<VirtualFile> consumer) {
    processClassPathItems(virtualFile, module, consumer, true);
  }

  public static void processClassPathItems(VirtualFile virtualFile, Module module, Consumer<VirtualFile> consumer,
                                           boolean includeModuleOutput) {
    if (isJarFile(virtualFile)){
      consumer.accept(virtualFile);
    }

    if (module != null) {
      OrderEnumerator enumerator = ModuleRootManager.getInstance(module).orderEntries().recursively();
      if (!includeModuleOutput) {
        enumerator = enumerator.withoutModuleSourceEntries();
      }
      for (VirtualFile root : enumerator.getClassesRoots()) {
        VirtualFile file;
        if (root.getFileSystem() instanceof ArchiveFileSystem) {
          file = ArchiveVfsUtil.getVirtualFileForJar(root);
        }
        else {
          file = root;
        }
        consumer.accept(file);
      }
    }
  }

 /* private static void addUrl(List<URL> urls, VirtualFile file) {
    if (file == null || !file.isValid()) return;
    final URL url = getUrl(file);
    if (url != null) {
      urls.add(url);
    }
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  @Nullable
  private static URL getUrl(VirtualFile file) {
    if (file.getFileSystem() instanceof JarFileSystem && file.getParent() != null) return null;

    String path = file.getPath();
    if (path.endsWith(JarFileSystem.JAR_SEPARATOR)) {
      path = path.substring(0, path.length() - 2);
    }

    String url;
    if (SystemInfo.isWindows) {
      url = "file:/" + path;
    }
    else {
      url = "file://" + path;
    }

    if (file.isDirectory() && !(file.getFileSystem() instanceof JarFileSystem)) url += "/";


    try {
      return new URL(url);
    }
    catch (MalformedURLException e) {
      LOG.error(e);
      return null;
    }
  }

  @Nullable
  public static IElementType getJspElementType(@NotNull final JspElementType.Kind kind) {
    final JspSpiUtil spiUtil = getJspSpiUtil();
    return spiUtil != null ? spiUtil._getJspElementType(kind) : null;
  }

  @Nullable
  public static IElementType getJspScriptletType() {
    return getJspElementType(JspElementType.Kind.JSP_SCRIPTLET);
  }

  @Nullable
  public static IElementType getJspExpressionType() {
    return getJspElementType(JspElementType.Kind.JSP_EXPRESSION);
  }

  protected abstract IElementType _getJspElementType(@NotNull final JspElementType.Kind kind);  */
}