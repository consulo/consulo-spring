/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.facet;

import com.intellij.spring.impl.ide.SpringBundle;
import com.intellij.spring.impl.ide.constants.SpringConstants;
import com.intellij.xml.util.XmlUtil;
import consulo.fileTemplate.FileTemplate;
import consulo.fileTemplate.FileTemplateManager;
import consulo.xml.language.psi.XmlDocument;
import consulo.xml.language.psi.XmlFile;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public enum SpringSchemaVersion {

  Spring_1_DTD(SpringBundle.message("create.spring.context.file.dtd.1.0"), SpringConstants.BEANS_DTD_1,
               "spring-beans.1_0.xml"),
  Spring_2_DTD(SpringBundle.message("create.spring.context.file.dtd.2.0"), SpringConstants.BEANS_DTD_2,
               "spring-beans.2_0.xml"),
  Spring_2_Schema(SpringBundle.message("create.spring.context.file.schema.2.0"), SpringConstants.BEANS_XSD,
                  "spring-beans.schema.2_0.xml");

  private static final Set<SpringSchemaVersion> ALL_VERSIONS = EnumSet.allOf(SpringSchemaVersion.class);

  private final String myName;
  private final String myNamespace;
  private final String myTemplateName;

  SpringSchemaVersion(String name, @NonNls String namespace, @NonNls String templateName) {

    myName = name;
    myNamespace = namespace;
    myTemplateName = templateName;
  }

  public String getName() {
    return myName;
  }

  public String getTemplateName() {
    return myTemplateName;
  }

  public FileTemplate getTemplate() {
    return FileTemplateManager.getInstance().getJ2eeTemplate(myTemplateName);
  }

  @Nullable
  public static SpringSchemaVersion getVersion(XmlFile file) {
    XmlDocument document = file.getDocument();
    if (document != null) {
      String uri = XmlUtil.getDtdUri(document);
      if (uri != null) {
        for (SpringSchemaVersion version: ALL_VERSIONS) {
          if (version.myNamespace.equals(uri)) {
            return version; 
          }
        }
      }
    }
    return null;
  }

  public String toString() {
    return getName();
  }
}
