package com.intellij.spring.web.mvc.tiles;

import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.jsp.WebDirectoryElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.PsiElementPointer;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.beans.SpringPropertyDefinition;
import com.intellij.spring.web.mvc.MVCViewProvider;
import com.intellij.struts.dom.tiles.Definition;
import com.intellij.struts.dom.tiles.TilesDefinitions;
import java.util.function.Consumer;
import com.intellij.util.containers.hash.HashSet;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public class TilesViewProvider implements MVCViewProvider {
  @NonNls private static final String TILES_CONFIGURER = "org.springframework.web.servlet.view.tiles.TilesConfigurer";
  @NonNls private static final String TILES2_CONFIGURER = "org.springframework.web.servlet.view.tiles2.TilesConfigurer";

  public List<Pair<String, PsiElementPointer>> getViews(SpringModel model, WebFacet facet) {

    final ArrayList<Pair<String, PsiElementPointer>> list = new ArrayList<Pair<String, PsiElementPointer>>();
    processTiles(model, facet, new Consumer<Set<XmlFile>>() {

      public void consume(Set<XmlFile> files) {
        for (XmlFile file : files) {
          DomFileElement<TilesDefinitions> fileElement =
            DomManager.getDomManager(file.getProject()).getFileElement(file, TilesDefinitions.class);
          if (fileElement != null) {
            for (final Definition definition : fileElement.getRootElement().getDefinitions()) {
              String name = definition.getName().getValue();
              PsiElementPointer elementPointer = new PsiElementPointer() {
                public PsiElement getPsiElement() {
                  return definition.getXmlElement();
                }
              };
              list.add(Pair.create(name, elementPointer));
            }
          }

        }
      }
    });
    return list;
  }

  static void processTiles(SpringModel model, WebFacet facet, Consumer<Set<XmlFile>> consumer) {
    Module module = model.getModule();
    if (module == null) {
      return;
    }
    Project project = module.getProject();
    JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
    PsiClass tilesConfigurer = facade.findClass(TILES_CONFIGURER, GlobalSearchScope.allScope(project));
    if (tilesConfigurer != null) {
      processTilesConfigurer(model, facet, consumer, tilesConfigurer);
    }
    tilesConfigurer = facade.findClass(TILES2_CONFIGURER, GlobalSearchScope.allScope(project));
    if (tilesConfigurer != null) {
      processTilesConfigurer(model, facet, consumer, tilesConfigurer);
    }
  }

  private static void processTilesConfigurer(SpringModel model, WebFacet facet, Consumer<Set<XmlFile>> consumer, PsiClass tilesConfigurer) {
    List<SpringBaseBeanPointer> pointers = model.findBeansByEffectivePsiClassWithInheritance(tilesConfigurer);
    for (SpringBaseBeanPointer pointer : pointers) {
        SpringPropertyDefinition property = SpringUtils.findPropertyByName(pointer.getSpringBean(), "definitions");
        if (property != null) {
          Set<String> paths = SpringUtils.getListOrSetValues(property);
          Set<XmlFile> files = new HashSet<XmlFile>(paths.size());
          for (String path : paths) {
            WebDirectoryElement element = WebUtil.getWebUtil().findWebDirectoryElement(path, facet);
            if (element != null) {
              PsiFile file = element.getOriginalFile();
              if (file instanceof XmlFile) {
                files.add((XmlFile)file);
              }
            }
          }
          consumer.consume(files);
        }
      }
  }
}
