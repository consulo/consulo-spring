package com.intellij.spring.webflow.impl;

import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.webflow.model.xml.Flow;
import com.intellij.spring.webflow.model.xml.WebflowModel;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.model.impl.DomModelImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * User: plt
 */
public class WebflowModelImpl extends DomModelImpl<Flow> implements WebflowModel {

  public WebflowModelImpl(@NotNull DomFileElement<Flow> mergedModel, @NotNull Set<XmlFile> configFiles) {
    super(mergedModel, configFiles);
  }

  public Flow getFlow() {
    return getMergedModel();
  }
}
