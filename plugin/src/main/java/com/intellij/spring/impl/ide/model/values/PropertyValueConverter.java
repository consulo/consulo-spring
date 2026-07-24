/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.ide.model.values;

import com.intellij.java.impl.util.xml.converters.values.GenericDomValueConvertersRegistry;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.model.xml.beans.TypeHolder;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.project.Project;
import consulo.xml.language.psi.XmlAttribute;
import consulo.xml.language.psi.XmlElement;
import consulo.xml.dom.Converter;
import consulo.xml.dom.DomElement;
import consulo.xml.dom.GenericDomValue;
import consulo.xml.dom.WrappingConverter;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class PropertyValueConverter extends WrappingConverter {

  @Nonnull
  public List<? extends PsiType> getValueTypes(GenericDomValue element) {
    if (element instanceof TypeHolder) {
      List<? extends PsiType> psiTypes = ((TypeHolder)element).getRequiredTypes();
      if (!psiTypes.isEmpty())
      return psiTypes;
    }
    DomElement parent = element.getParent();
    return parent instanceof TypeHolder ? ((TypeHolder)parent).getRequiredTypes() : Collections.<PsiType>emptyList();
  }

  @Nonnull
  public List<Converter> getConverters(@Nonnull GenericDomValue element) {
    
    XmlElement xmlElement = element.getXmlElement();
    if (xmlElement instanceof XmlAttribute) {
      PsiLanguageInjectionHost host = (PsiLanguageInjectionHost)((XmlAttribute)xmlElement).getValueElement();
      if (host == null || InjectedLanguageManager.getInstance(xmlElement.getProject()).getInjectedPsiFiles(xmlElement) != null) {
        return Collections.emptyList();
      }
    }
    Project project = element.getManager().getProject();
    GenericDomValueConvertersRegistry registry = SpringManager.getInstance(project).getValueProvidersRegistry();
    List<? extends PsiType> types = getValueTypes(element);
    ArrayList<Converter> list = new ArrayList<Converter>(types.size());
    if (types.isEmpty()) {
      Converter converter = registry.getConverter(element, null);
      if (converter != null) {
        list.add(converter);
      }
    }
    for (PsiType type : types) {
      Converter converter = registry.getConverter(element, type);
      if (converter != null) {
        list.add(converter);
      } else {
        return Collections.emptyList();
      }
    }
    return list;
  }

  public Converter getConverter(@Nonnull GenericDomValue domElement) {
    List<Converter> converters = getConverters(domElement);
    return converters.isEmpty() ? null : converters.get(0);
  }
}
