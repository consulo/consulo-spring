/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl;

import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.model.context.ComponentScan;
import com.intellij.spring.impl.ide.model.xml.CommonSpringBean;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import com.intellij.spring.impl.ide.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.impl.ide.model.xml.context.DomComponentScan;
import consulo.module.Module;
import consulo.spring.impl.DomSpringModel;
import consulo.spring.impl.model.BaseSpringModel;
import consulo.util.collection.ContainerUtil;
import consulo.xml.language.psi.XmlFile;
import consulo.xml.dom.DomFileElement;
import consulo.xml.util.xml.model.impl.DomModelImpl;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public class DomSpringModelImpl2 extends BaseSpringModel implements DomSpringModel {
  public static class MyDomModelImpl extends DomModelImpl<Beans> {

    private final DomSpringModel mySpringModel;

    public MyDomModelImpl(DomFileElement<Beans> mergedModel, @Nonnull Set<XmlFile> configFiles, DomSpringModel springModel) {
      super(mergedModel, configFiles);
      mySpringModel = springModel;
    }

    public DomSpringModel getSpringModel() {
      return mySpringModel;
    }
  }

  private final MyDomModelImpl myDomModel;

  public DomSpringModelImpl2(@Nonnull DomFileElement<Beans> mergedModel,
                             @Nonnull Set<XmlFile> configFiles,
                             Module module,
                             SpringFileSet fileSet) {
    super(module, fileSet);
    myDomModel = new MyDomModelImpl(mergedModel, configFiles, this);
  }

  @Nonnull
  @Override
  public Set<XmlFile> getConfigFiles() {
    return myDomModel.getConfigFiles();
  }

  @Nonnull
  @Override
  public List<DomFileElement<Beans>> getRoots() {
    return myDomModel.getRoots();
  }

  @Override
  public DomModelImpl<Beans> getDomModel() {
    return myDomModel;
  }

  @Override
  public List<? extends ComponentScan> getComponentScans() {
    return getComponentScans(getAllDomBeans());
  }

  private static List<DomComponentScan> getComponentScans(Collection<SpringBaseBeanPointer> allDomBeans) {
    return ContainerUtil.mapNotNull(allDomBeans, domSpringBeanPointer -> {
      CommonSpringBean domSpringBean = domSpringBeanPointer.getSpringBean();
      if (domSpringBean instanceof DomComponentScan) {
        return (DomComponentScan)domSpringBean;
      }
      return null;
    });
  }
}
