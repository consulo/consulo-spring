/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.impl.model;

import com.intellij.spring.impl.ide.CustomBeanInfo;
import com.intellij.spring.impl.ide.CustomBeanRegistry;
import com.intellij.spring.impl.ide.SpringToolDomExtender;
import com.intellij.spring.impl.ide.constants.SpringConstants;
import com.intellij.spring.impl.ide.model.xml.CustomBean;
import com.intellij.spring.impl.ide.model.xml.CustomBeanWrapper;
import com.intellij.spring.impl.ide.model.xml.custom.CustomNamespaceSpringBean;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.util.XmlUtil;
import consulo.application.util.NotNullLazyValue;
import consulo.language.psi.PsiElement;
import consulo.module.Module;
import consulo.xml.psi.xml.XmlTag;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author peter
 */
public abstract class CustomBeanWrapperImpl extends DomSpringBeanImpl implements CustomBeanWrapper {

  private final NotNullLazyValue<List<CustomBean>> myBeans = new NotNullLazyValue<List<CustomBean>>() {
    @Nonnull
    protected List<CustomBean> compute() {
      final XmlTag tag = getXmlTag();
      final List<CustomBeanInfo> infos = CustomBeanRegistry.getInstance(getPsiManager().getProject()).getParseResult(tag);
      if (infos == null || infos.isEmpty()) {
        CustomBean bean = getExportingBean(tag);
        return bean != null ? Collections.singletonList(bean) : Collections.<CustomBean>emptyList();
      }

      final ArrayList<CustomBean> result = new ArrayList<CustomBean>(infos.size());
      final Module module = getModule();
      for (final CustomBeanInfo info : infos) {
        result.add(new CustomNamespaceSpringBean(info, module, CustomBeanWrapperImpl.this));
      }
      return result;
    }
  };

  @Nullable
  private CustomBean getExportingBean(final XmlTag tag) {
    final XmlElementDescriptor descriptor = tag.getDescriptor();
    if (descriptor == null) return null;

    final PsiElement declaration = descriptor.getDeclaration();
    if (!(declaration instanceof XmlTag)) return null;

    XmlTag declTag = (XmlTag) declaration;
    XmlTag annotationTag = SpringToolDomExtender.getToolAnnotationTag(declTag, true);
    if (annotationTag == null && "element".equals(declTag.getLocalName())) {
      XmlTag[] type = declTag.findSubTags("simpleType", XmlUtil.XML_SCHEMA_URI);
      if (type.length > 0) annotationTag = SpringToolDomExtender.getToolAnnotationTag(type[0], true);

      if (annotationTag == null) {
        type = declTag.findSubTags("complexType", XmlUtil.XML_SCHEMA_URI);
        if (type.length > 0) annotationTag = SpringToolDomExtender.getToolAnnotationTag(type[0], true);
      }
    }
    if (annotationTag == null) return null;

    final XmlTag[] exports = annotationTag.findSubTags("exports", SpringConstants.TOOL_NAMESPACE);
    if (exports.length == 0) return null;

    final CustomBeanInfo info = new CustomBeanInfo();
    info.beanClassName = exports[0].getAttributeValue("type", SpringConstants.TOOL_NAMESPACE);
    final String idPtr = exports[0].getAttributeValue("identifier", SpringConstants.TOOL_NAMESPACE);
    if (idPtr == null) {
      info.idAttribute = "id";
    }
    else if (idPtr.startsWith("@") && !idPtr.contains("/") && !idPtr.contains("[")) {
      info.idAttribute = idPtr.substring(1);
    }
    if (info.idAttribute != null) {
      info.beanName = tag.getAttributeValue(info.idAttribute);
    }
    return new CustomNamespaceSpringBean(info, getModule(), this);
  }

  @Nonnull
  private synchronized List<CustomBean> getCachedValue() {
    return myBeans.getValue();
  }

  @Nullable
  public String getBeanName() {
    if (!isParsed()) {
      return super.getBeanName();
    }

    return null;
  }

  @Nonnull
  public List<CustomBean> getCustomBeans() {
    return getCachedValue();
  }

  public boolean isDummy() {
    return getCachedValue().isEmpty();
  }

  public boolean isParsed() {
    final List<CustomBean> customBeans = getCachedValue();
    return !customBeans.isEmpty() || customBeans != Collections.<CustomBean>emptyList();
  }

  @Nullable
  public String getClassName() {
    return null;
  }

}



