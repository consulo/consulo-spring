/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.schemas;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiJavaPackage;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.constants.SpringConstants;
import com.intellij.xml.DefaultXmlExtension;
import com.intellij.xml.impl.schema.XmlNSDescriptorImpl;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.language.psi.*;
import consulo.language.psi.meta.PsiMetaData;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.ModuleUtilCore;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.xml.descriptor.XmlSchemaProvider;
import consulo.xml.language.psi.XmlDocument;
import consulo.xml.language.psi.XmlFile;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Dmitry Avdeev
 */
@ExtensionImpl
public class SpringSchemaProvider extends XmlSchemaProvider implements DumbAware {
  private static final Logger LOG = Logger.getInstance(SpringSchemaProvider.class);

  private static final Map<String, String> FALLBACK_SCHEMALOCATIONS = new HashMap<String, String>(2);
  private static final Key<CachedValue<Map<String, VirtualFile>>> SCHEMAS_BUNDLE_KEY = Key.create("spring schemas");
  private static final CachedValueProvider.Result<Map<String, VirtualFile>> EMPTY_MAP_RESULT =
    new CachedValueProvider.Result<Map<String, VirtualFile>>(
      Collections.<String, VirtualFile>emptyMap(), PsiModificationTracker.MODIFICATION_COUNT);

  static {
    FALLBACK_SCHEMALOCATIONS.put(SpringConstants.BEANS_XSD, SpringConstants.BEANS_SCHEMALOCATION_FALLBACK);
    FALLBACK_SCHEMALOCATIONS.put(SpringConstants.TOOL_NAMESPACE, SpringConstants.TOOL_SCHEMALOCATION_FALLBACK);
  }

  @Nullable
  public XmlFile getSchema(@Nonnull @NonNls String url, @Nullable consulo.module.Module module, @Nonnull PsiFile baseFile) {
    String schemaLocation = FALLBACK_SCHEMALOCATIONS.get(url);
    if (schemaLocation != null) {
      return getSchema(schemaLocation, module, baseFile);
    }
    if (module == null) {
      PsiDirectory directory = baseFile.getParent();
      if (directory != null) {
        module = ModuleUtilCore.findModuleForPsiElement(directory);
      }
    }
    if (module == null) {
      return null;
    }
    Map<String, VirtualFile> schemas = getSchemas(module);
    Project project = module.getProject();
    VirtualFile file = schemas.get(url);
    if (file == null) return null;
    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    if (!(psiFile instanceof XmlFile)) return null;
    return (XmlFile)psiFile;
  }

  public boolean isAvailable(@Nonnull XmlFile file) {
    boolean isSpring = SpringManager.getInstance(file.getProject()).isSpringBeans(file);
    if (isSpring) {
      return true;
    }
    VirtualFile virtualFile = file.getVirtualFile();
    if (virtualFile == null) {
      return false;
    }
    String extension = virtualFile.getExtension();
    return extension != null && extension.equals("xsd");
  }

  @Nonnull
  public Set<String> getAvailableNamespaces(@Nonnull XmlFile file, String tagName) {
    consulo.module.Module module = ModuleUtilCore.findModuleForPsiElement(file);
    if (module == null) {
      return Collections.emptySet();
    }
    Map<String, VirtualFile> map = getSchemas(module);
    HashSet<String> strings = new HashSet<String>(map.size());
    for (VirtualFile virtualFile : map.values()) {
      String namespace = getNamespace(virtualFile, file.getProject());
      if (namespace != null) {
        strings.add(namespace);
      }
    }
    return DefaultXmlExtension.filterNamespaces(strings, tagName, file);
  }

  @Nullable
  private static String getNamespace(VirtualFile virtualFile, Project project) {
    PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
    if (psiFile instanceof XmlFile) {
      XmlDocument document = ((XmlFile)psiFile).getDocument();
      if (document != null) {
        PsiMetaData metaData = document.getMetaData();
        if (metaData instanceof XmlNSDescriptorImpl) {
          return ((XmlNSDescriptorImpl)metaData).getDefaultNamespace();
        }
      }
    }
    return null;
  }

  public String getDefaultPrefix(@Nonnull @NonNls String namespace, @Nonnull XmlFile context) {
    if (!SpringManager.getInstance(context.getProject()).isSpringBeans(context))
      return null;
    String[] strings = namespace.split("/");
    return strings[strings.length - 1];
  }

  public Set<String> getLocations(@Nonnull @NonNls String namespace, @Nonnull XmlFile context) {
    consulo.module.Module module = ModuleUtilCore.findModuleForPsiElement(context);
    if (module == null) {
      return null;
    }
    Map<String, VirtualFile> schemas = getSchemas(module);
    for (Map.Entry<String, VirtualFile> entry : schemas.entrySet()) {
      String s = getNamespace(entry.getValue(), context.getProject());
      if (s != null && s.equals(namespace)) {
        return Collections.singleton(entry.getKey());
      }
    }
    return null;
  }

  @Nonnull
  public static Map<String, VirtualFile> getSchemas(@Nonnull final Module module) {
    Project project = module.getProject();
    CachedValuesManager manager = CachedValuesManager.getManager(project);
    Map<String, VirtualFile> bundle =
      manager.getCachedValue(module, SCHEMAS_BUNDLE_KEY, new CachedValueProvider<Map<String, VirtualFile>>() {
        public Result<Map<String, VirtualFile>> compute() {
          return computeSchemas(module);
        }
      }, false);
    return bundle == null ? Collections.<String, VirtualFile>emptyMap() : bundle;
  }

  @Nonnull
  public static Map<String, String> getHandlers(@Nonnull consulo.module.Module module) {
    return computeHandlers(module);
  }

  @Nonnull
  private static CachedValueProvider.Result<Map<String, VirtualFile>> computeSchemas(@Nonnull consulo.module.Module module) {
    PsiJavaPackage psiPackage =
      JavaPsiFacade.getInstance(module.getProject()).findPackage("META-INF");
    if (psiPackage != null) {
      PsiDirectory[] directories =
        psiPackage.getDirectories(GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false));
      Map<String, VirtualFile> map = new HashMap<String, VirtualFile>();
      ArrayList<Object> dependencies = new ArrayList<Object>();
      dependencies.add(ProjectRootManager.getInstance(module.getProject()));
      for (PsiDirectory directory : directories) {
        PsiFile psiFile = directory.findFile("spring.schemas");
        if (psiFile != null) {
          VirtualFile schemasFile = psiFile.getVirtualFile();
          assert schemasFile != null;
          dependencies.add(psiFile);
          PsiDirectory parent = directory.getParent();
          assert parent != null;
          String root = parent.getVirtualFile().getUrl();
          if (!root.endsWith("/")) {
            root += "/";
          }
          InputStream inputStream = null;
          try {
            inputStream = schemasFile.getInputStream();
            PropertyResourceBundle bundle = new PropertyResourceBundle(inputStream);
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
              String key = keys.nextElement();
              String location = (String)bundle.handleGetObject(key);
              String schemaUrl = root + location;
              VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(schemaUrl);
              if (file != null) {
                map.put(key, file);
              }
            }
          }
          catch (IOException e) {
            LOG.error(e);
            return EMPTY_MAP_RESULT;
          }
          finally {
            if (inputStream != null) {
              try {
                inputStream.close();
              }
              catch (IOException e) {
                LOG.error(e);
              }
            }
          }
        }
      }
      return new CachedValueProvider.Result<Map<String, VirtualFile>>(map, dependencies.toArray());
    }
    return EMPTY_MAP_RESULT;
  }

  @Nonnull
  private static Map<String, String> computeHandlers(@Nonnull consulo.module.Module module) {
    Project project = module.getProject();
    PsiManager psiManager = PsiManager.getInstance(project);
    PsiPackage psiPackage = JavaPsiFacade.getInstance(psiManager.getProject()).findPackage("META-INF");
    if (psiPackage != null) {
      GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false);
      PsiDirectory[] directories = psiPackage.getDirectories(scope);
      Map<String, String> map = new HashMap<String, String>();
      for (PsiDirectory directory : directories) {
        PsiFile psiFile = directory.findFile("spring.handlers");
        if (psiFile != null) {
          VirtualFile handlersFile = psiFile.getVirtualFile();
          assert handlersFile != null;
          PsiDirectory parent = directory.getParent();
          assert parent != null;
          String root = parent.getVirtualFile().getUrl();
          if (!root.endsWith("/")) {
            root += "/";
          }
          InputStream inputStream = null;
          try {
            inputStream = handlersFile.getInputStream();
            PropertyResourceBundle bundle = new PropertyResourceBundle(inputStream);
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
              String key = keys.nextElement();
              map.put(key, (String)bundle.handleGetObject(key));
            }
          }
          catch (IOException e) {
            LOG.error(e);
            return Collections.emptyMap();
          }
          finally {
            if (inputStream != null) {
              try {
                inputStream.close();
              }
              catch (IOException e) {
                LOG.error(e);
              }
            }
          }
        }
      }
      return map;
    }
    return Collections.emptyMap();
  }
}
