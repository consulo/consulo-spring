package com.intellij.spring.impl.ide.model.actions.patterns.frameworks.util;

import consulo.util.xml.serializer.annotation.Attribute;
import consulo.util.xml.serializer.annotation.Tag;

@Tag("docLink")
public class StandardBeansDocLink {

  @Attribute("beanId")
  public String myBeanId;

  @Attribute("api")
  public String myApiLink;

  @Attribute("reference")
  public String myReferenceLink;

  public String getBeanId() {
    return myBeanId;
  }

  public String getApiLink() {
    return myApiLink;
  }

  public String getReferenceLink() {
    return myReferenceLink;
  }
}

