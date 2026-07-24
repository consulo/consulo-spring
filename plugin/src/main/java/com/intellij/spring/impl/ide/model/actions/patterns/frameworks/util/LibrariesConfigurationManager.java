package com.intellij.spring.impl.ide.model.actions.patterns.frameworks.util;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.disposer.Disposable;
import consulo.ide.ServiceManager;
import consulo.java.ex.facet.LibraryInfo;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.XmlSerializer;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
public class LibrariesConfigurationManager implements Disposable {
  private final Map<String, List<LibraryInfo>> myLibraries = new HashMap<String, List<LibraryInfo>>();
  private static final String REQUIRED_CLASSES_DELIMITER = ",";

  @Nonnull
  public static LibrariesConfigurationManager getInstance(@Nonnull Project project) {
    synchronized (project) {
      return ServiceManager.getService(project, LibrariesConfigurationManager.class);
    }
  }

  @NonNls
  private static final String DOC_LINKS_RESOURCE_XML = "/resources/frameworks/libraries.xml";

  public LibrariesConfigurationManager() {
    LibrariesConfigurationInfo libs =
      XmlSerializer.deserialize(LibrariesConfigurationInfo.class.getResource(DOC_LINKS_RESOURCE_XML), LibrariesConfigurationInfo.class);

    assert libs != null;
    assert libs.getLibraryConfigurationInfos() != null;

    for (LibraryConfigurationInfo libInfo : libs.getLibraryConfigurationInfos()) {
      String frameworkId = libInfo.getFrameworkId();
      LibraryInfo info = new LibraryInfo(libInfo.getJarName(), libInfo.getVersion(), libInfo.getDownloadUrl(),
                                               libInfo.getPresentationdUrl(), getRequredClasses(libInfo.getRequiredClasses()));

      if (myLibraries.get(frameworkId) == null) myLibraries.put(frameworkId, new ArrayList<LibraryInfo>());

      myLibraries.get(frameworkId).add(info);
    }
  }

  private static String[] getRequredClasses(String requiredClasses) {
    List<String> strings = StringUtil.split(requiredClasses, REQUIRED_CLASSES_DELIMITER);
    return ArrayUtil.toStringArray(strings);
  }

  @Nullable
  public LibraryInfo[] getLibraryInfos(String frameworkId) {
    List<LibraryInfo> libraryInfos = myLibraries.get(frameworkId);

    return libraryInfos == null ? LibraryInfo.EMPTY_ARRAY : libraryInfos.toArray(new LibraryInfo[libraryInfos.size()]);
  }

  public void dispose() {

  }
}
