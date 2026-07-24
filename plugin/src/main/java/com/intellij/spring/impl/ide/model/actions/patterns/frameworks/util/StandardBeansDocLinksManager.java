package com.intellij.spring.impl.ide.model.actions.patterns.frameworks.util;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.disposer.Disposable;
import consulo.ide.ServiceManager;
import consulo.project.Project;
import consulo.util.lang.Pair;
import consulo.util.xml.serializer.XmlSerializer;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
public class StandardBeansDocLinksManager implements Disposable {
  private final Map<String, Pair<String, String>> myDocLinks = new HashMap<String, Pair<String, String>>();


  @Nonnull
  public static StandardBeansDocLinksManager getInstance(@Nonnull Project project) {
    synchronized (project) {
      return ServiceManager.getService(project, StandardBeansDocLinksManager.class);
    }
  }

  @NonNls
  private static final String DOC_LINKS_RESOURCE_XML = "/resources/frameworks/docLinks.xml";

  public StandardBeansDocLinksManager() {
    StandardBeansDocLinks docLinks =
      XmlSerializer.deserialize(StandardBeansDocLinksManager.class.getResource(DOC_LINKS_RESOURCE_XML), StandardBeansDocLinks.class);

    assert docLinks != null;
    assert docLinks.getDocLinks() != null;

    for (StandardBeansDocLink docLink : docLinks.getDocLinks()) {
      myDocLinks.put(docLink.getBeanId(), new Pair<String, String>(docLink.getApiLink(), docLink.getReferenceLink()));
    }
  }

  @Nullable
  public String getApiLink(String beanId) {
    Pair<String, String> pair = myDocLinks.get(beanId);

    return pair == null ? null : pair.getFirst();
  }

  @Nullable
  public String getReferenceLink(String beanId) {
    Pair<String, String> pair = myDocLinks.get(beanId);

    return pair == null ? null : pair.getSecond();
  }

  public void dispose() {

  }
}
