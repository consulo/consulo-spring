package com.intellij.spring.web.mvc.tiles;

import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringModel;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.web.mvc.SpringMVCModel;
import com.intellij.struts.StrutsProjectComponent;
import com.intellij.struts.TilesModel;
import com.intellij.struts.TilesModelProvider;
import com.intellij.struts.StrutsPluginDomFactory;
import com.intellij.struts.dom.tiles.TilesDefinitions;
import com.intellij.struts.psi.TilesModelImpl;
import java.util.function.Consumer;
import com.intellij.util.xml.DomFileElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.ArrayList;

/**
 * @author Dmitry Avdeev
 */
public class MVCTilesModelProvider implements TilesModelProvider {

  @NotNull
  public Collection<TilesModel> computeModels(@NotNull Module module) {
    SpringFacet springFacet = SpringFacet.getInstance(module);
    if (springFacet == null) {
      return Collections.emptyList();
    }
    final StrutsPluginDomFactory<TilesDefinitions,TilesModel> factory =
      StrutsProjectComponent.getInstance(module.getProject()).getTilesFactory();
    Collection<WebFacet> webFacets = WebFacet.getInstances(module);
    final ArrayList<TilesModel> models = new ArrayList<TilesModel>();
    Consumer<Set<XmlFile>> consumer = new Consumer<Set<XmlFile>>() {
      public void consume(Set<XmlFile> definitions) {
        DomFileElement<TilesDefinitions> domFileElement = factory.createMergedModelRoot(definitions);
        if (domFileElement != null) {
          TilesModelImpl tilesModel = new TilesModelImpl(definitions, domFileElement, "mvcTilesModel");
          models.add(tilesModel);
        }
      }
    };
    for (WebFacet webFacet : webFacets) {
      SpringMVCModel springMVCModel = SpringMVCModel.getModel(webFacet, springFacet);
      if (springMVCModel != null) {
        Collection<SpringModel> servletModels = springMVCModel.getServletModels();
        for (SpringModel servletModel : servletModels) {
          TilesViewProvider.processTiles(servletModel, webFacet, consumer);

        }
      }
    }
    return models;
  }
}
